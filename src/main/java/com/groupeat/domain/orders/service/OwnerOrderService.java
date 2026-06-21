package com.groupeat.domain.orders.service;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.orders.converter.OwnerOrderDetailConverter;
import com.groupeat.domain.orders.converter.OwnerOrderListConverter;
import com.groupeat.domain.orders.dto.response.OwnerOrderDetailResponse;
import com.groupeat.domain.orders.dto.response.OwnerOrderListResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.enums.OrderTab;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderItemRepository;
import com.groupeat.domain.orders.repository.OrderQueryRepository;
import com.groupeat.domain.orders.repository.OrderRepository;
import com.groupeat.domain.payment.entity.Payment;
import com.groupeat.domain.payment.repository.PaymentRepository;
import com.groupeat.domain.signup.exception.SignupErrorStatus;
import com.groupeat.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerOrderService {

    private final OrderQueryRepository orderQueryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public OwnerOrderListResponse.OwnerOrderListDTO getOwnerOrderListByTab(
            Long ownerId, OrderTab tab, Long lastOrderId, int size
    ) {
        validateOwner(ownerId);

        // OrderTab에서 탭에 맞는 상태 리스트와 날짜 조건 추출
        List<OrderStatus> statuses = tab.getStatuses();
        LocalDate pickupDate = tab.isConfirmedTab() ? LocalDate.now() : null;

        // 무한 스크롤용 데이터 목록 및 탭에 해당하는 전체 개수 조회
        List<Order> orders = orderQueryRepository.findOwnerOrdersByCursorWithDate(
                ownerId, statuses, pickupDate, lastOrderId, size
        );
        long totalElements = orderQueryRepository.countOwnerOrdersWithDate(
                ownerId, statuses, pickupDate
        );

        // 다음 페이지 존재 여부 계산 로직
        boolean hasNext = orders.size() > size;
        if (hasNext) {
            orders.remove(size);
        }
        // 리스트가 비어있지 않다면 마지막 주문의 ID를 다음 커서로 지정
        Long nextCursor = orders.isEmpty() ? null : orders.get(orders.size() - 1).getId();

        List<Long> orderIds = orders.stream().map(Order::getId).toList();
        List<OrderItem> allItems = orderItemRepository.findByOrderIdIn(orderIds);
        Map<Long, List<OrderItem>> itemsByOrderId = allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getOrder().getId()));

        // 재주문 여부 확인을 위한 MemberId 일괄 조회
        List<Long> memberIds = orders.stream()
                .map(Order::getMemberId)
                .distinct()
                .toList();

        Set<Long> reorderMemberIds = orderQueryRepository.findReorderMemberIds(ownerId, memberIds, orderIds);

        return OwnerOrderListConverter.toOwnerOrderListDTO(
                orders, totalElements, hasNext, nextCursor, itemsByOrderId, reorderMemberIds, tab.isConfirmedTab()
        );
    }

    public OwnerOrderDetailResponse.OrderDetailDTO getOwnerOrderDetail(Long ownerId, Long orderId) {
        // 사장님 계정 유효성 검증
        validateOwner(ownerId);

        // 주문 데이터 조회
        Order order = orderRepository.findByIdAndOwnerIdWithItems(orderId, ownerId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        Payment payment = paymentRepository.findFirstByOrderId(order.getOrderId()).orElse(null);

        return OwnerOrderDetailConverter.toOrderDetailDTO(order, payment);
    }

    private void validateOwner(Long ownerId) {
        // 해당 ID를 가진 회원이 DB에 존재하는지 확인
        Member member = memberRepository.findById(ownerId)
                .orElseThrow(() -> new GeneralException(SignupErrorStatus.MEMBER_NOT_FOUND));

        // 그 회원이 BUSINESS 권한을 가진 계정인지 확인
        if (member.getMemberType() != MemberType.BUSINESS) {
            throw new GeneralException(SignupErrorStatus.NOT_BUSINESS_MEMBER);
        }
    }
}