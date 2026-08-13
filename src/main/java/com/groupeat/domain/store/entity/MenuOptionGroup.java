package com.groupeat.domain.store.entity;

import com.groupeat.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "menu_option_group")
public class MenuOptionGroup extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_option_group_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired; // 필수 옵션 여부

    @Column(name = "is_multiple", nullable = false)
    private Boolean isMultiple; // 다중 선택 가능 여부

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "optionGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuOption> options = new ArrayList<>();

    public void addOption(MenuOption option) {
        this.options.add(option);
    }
}
