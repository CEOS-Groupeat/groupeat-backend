package com.groupeat.domain.search.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.groupeat.domain.search.cursor.StoreSearchCursorCodec;
import com.groupeat.domain.search.dto.request.StoreSearchCondition;
import com.groupeat.domain.search.repository.SearchRepository;
import com.groupeat.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.hibernate.Session;
import com.groupeat.domain.search.controller.SearchController;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = SearchPlanTransactionTest.ApplicationConfig.class,
        properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false",
        "spring.jpa.open-in-view=true", "spring.datasource.hikari.maximum-pool-size=1"})
@AutoConfigureMockMvc(addFilters = false)
@Import({SearchService.class, SearchRepository.class, QueryDslConfig.class,
        StoreSearchCursorCodec.class, SearchPlanTransactionTest.JsonConfig.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SearchPlanTransactionTest {
    @Configuration
    // 외부 OAuth 자격 증명 없이 실제 JPA·MVC·OSIV 자동 구성을 사용한다.
    @EnableAutoConfiguration(excludeName =
            "org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration")
    @EntityScan("com.groupeat")
    @EnableJpaRepositories("com.groupeat")
    @Import(SearchController.class)
    static class ApplicationConfig {}

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void database(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @TestConfiguration
    static class JsonConfig {
        @Bean ObjectMapper objectMapper() { return new ObjectMapper(); }
    }

    @Autowired SearchService service;
    @Autowired EntityManager entityManager;
    @Autowired DataSource dataSource;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired MockMvc mockMvc;
    @Autowired QueryDslConfig queryDslConfig;
    @MockitoSpyBean SearchRepository repository;

    record State(String mode, int pid) {}

    private State state(Connection connection) throws SQLException {
        try (var statement = connection.createStatement();
             var rows = statement.executeQuery("SELECT current_setting('plan_cache_mode'), pg_backend_pid()")) {
            rows.next();
            return new State(rows.getString(1), rows.getInt(2));
        }
    }

    private State pooledState() throws SQLException {
        try (var connection = dataSource.getConnection()) { return state(connection); }
    }

    private void assertSearchConnection(State baseline) {
        Object[] row = (Object[]) entityManager.createNativeQuery(
                "SELECT current_setting('plan_cache_mode'), pg_backend_pid(), current_setting('transaction_read_only')")
                .getSingleResult();
        assertThat(row[0]).isEqualTo("force_custom_plan");
        assertThat(((Number) row[1]).intValue()).isEqualTo(baseline.pid());
        assertThat(row[2]).isEqualTo("on");
    }

    private void observeQueries(State baseline) {
        doAnswer(invocation -> {
            assertJpaTransaction();
            return invocation.callRealMethod();
        }).when(repository).forceCustomPlanForCurrentTransaction();
        doAnswer(invocation -> {
            assertSearchConnection(baseline);
            Object result = invocation.callRealMethod();
            assertSearchConnection(baseline);
            return result;
        }).when(repository).searchStores(any(), any());
        doAnswer(invocation -> {
            assertSearchConnection(baseline);
            Object result = invocation.callRealMethod();
            assertSearchConnection(baseline);
            return result;
        }).when(repository).countStores(any());
    }

    private EntityManager repositoryEntityManager() {
        Object target = AopTestUtils.getUltimateTargetObject(repository);
        return (EntityManager) ReflectionTestUtils.getField(target, "entityManager");
    }

    private void assertJpaTransaction() {
        EntityManager repositoryEm = repositoryEntityManager();
        EntityManager queryDslEm = (EntityManager) ReflectionTestUtils.getField(queryDslConfig, "entityManager");
        assertThat(transactionManager).isInstanceOf(JpaTransactionManager.class);
        assertThat(((JpaTransactionManager) transactionManager).getEntityManagerFactory())
                .isSameAs(repositoryEm.getEntityManagerFactory());
        assertThat(queryDslEm.getEntityManagerFactory()).isSameAs(repositoryEm.getEntityManagerFactory());
        assertThat(queryDslEm.getDelegate()).isSameAs(repositoryEm.getDelegate());
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
        assertThat(((Session) repositoryEm.getDelegate()).isJoinedToTransaction()).isTrue();
    }

    private StoreSearchCondition condition() {
        return new StoreSearchCondition(null, null, LocalDate.now().plusDays(7), null,
                1, null, null, null, null, null);
    }

    @Test
    void readOnlySearchUsesSameConnectionAndRestoresAfterCommit() throws SQLException {
        State baseline = pooledState();
        assertThat(baseline.mode()).isEqualTo("auto");
        observeQueries(baseline);
        service.searchStores(condition());
        verify(repository, times(1)).searchStores(any(), any());
        verify(repository, times(1)).countStores(any());
        assertThat(pooledState()).isEqualTo(baseline);
    }

    @Test
    void participatingTransactionKeepsSettingUntilRollbackThenRestores() throws SQLException {
        State baseline = pooledState();
        observeQueries(baseline);
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setReadOnly(true);
        transaction.executeWithoutResult(status -> {
            service.searchStores(condition());
            assertSearchConnection(baseline);
            status.setRollbackOnly();
        });
        verify(repository, times(1)).searchStores(any(), any());
        verify(repository, times(1)).countStores(any());
        assertThat(pooledState()).isEqualTo(baseline);
    }

    @Test
    void httpSearchUsesRequestEntityManagerAndRestoresAfterCommit() throws Exception {
        State baseline = pooledState();
        observeQueries(baseline);
        // OSIV 프록시의 false와 실제 Hibernate Session의 true를 함께 검증한다.
        // 테스트/Controller가 별도 트랜잭션을 열지 않고 Service 프록시가 경계를 만든다.
        doAnswer(invocation -> {
            assertJpaTransaction();
            assertThat(repositoryEntityManager().isJoinedToTransaction()).isFalse();
            return invocation.callRealMethod();
        }).when(repository).forceCustomPlanForCurrentTransaction();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/search/stores").param("pickupDate", LocalDate.now().plusDays(7).toString())
                        .param("quantity", "1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.isSuccess").value(true));
        verify(repository, times(1)).searchStores(any(), any());
        verify(repository, times(1)).countStores(any());
        assertThat(pooledState()).isEqualTo(baseline);
    }

    @Test
    void httpSearchFailureRestoresSettingAfterServiceRollback() throws Exception {
        State baseline = pooledState();
        observeQueries(baseline);
        doAnswer(invocation -> {
            assertSearchConnection(baseline);
            throw new IllegalStateException("count failure for rollback verification");
        }).when(repository).countStores(any());
        assertThatThrownBy(() -> mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/api/search/stores").param("pickupDate", LocalDate.now().plusDays(7).toString())
                .param("quantity", "1")))
                .hasRootCauseMessage("count failure for rollback verification");
        verify(repository, times(1)).searchStores(any(), any());
        verify(repository, times(1)).countStores(any());
        assertThat(pooledState()).isEqualTo(baseline);
    }

    @Test
    void rejectsSettingOutsideTransaction() {
        assertThatThrownBy(repository::forceCustomPlanForCurrentTransaction)
                .hasRootCauseInstanceOf(IllegalStateException.class)
                .hasMessageContaining("transactionActive=false, joinedToTransaction=false");
    }
}
