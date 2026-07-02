package com.groupeat.domain.orders.service;

import com.groupeat.domain.member.entity.Member;
import com.groupeat.domain.member.enums.MemberStatus;
import com.groupeat.domain.member.enums.MemberType;
import com.groupeat.domain.member.exceptoin.MemberErrorStatus;
import com.groupeat.domain.member.repository.MemberRepository;
import com.groupeat.domain.orders.converter.OwnerOrderDetailConverter;
import com.groupeat.domain.orders.converter.OwnerOrderListConverter;
import com.groupeat.domain.orders.dto.response.OwnerOrderDetailResponse;
import com.groupeat.domain.orders.dto.response.OwnerOrderListResponse;
import com.groupeat.domain.orders.entity.Order;
import com.groupeat.domain.orders.entity.OrderItem;
import com.groupeat.domain.orders.entity.OrderItemOption;
import com.groupeat.domain.orders.enums.OrderStatus;
import com.groupeat.domain.orders.enums.OrderTab;
import com.groupeat.domain.orders.exception.OrderErrorStatus;
import com.groupeat.domain.orders.repository.OrderItemOptionRepository;
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
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public OwnerOrderListResponse.OwnerOrderListDTO getOwnerOrderListByTab(
            Long ownerId, OrderTab tab, LocalDate filterDate, Long lastOrderId, int size
    ) {
        validateOwner(ownerId);

        // OrderTab에서 탭에 맞는 상태 리스트와 날짜 조건 추출
        List<OrderStatus> statuses = tab.getStatuses();

        LocalDate pickupDate = filterDate;

        // 레포지토리 호출 시 tab 객체를 넘김 (정렬 분기를 위함)
        int fetchSize = tab.isConfirmedTab() ? 300 : size;

        List<Order> orders = orderQueryRepository.findOwnerOrdersByCursorAndTab(
                ownerId, statuses, pickupDate, lastOrderId, fetchSize, tab
        );

        long totalElements = orderQueryRepository.countOwnerOrdersWithDate(
                ownerId, statuses, pickupDate
        );

        // 커서 및 다음 페이지 로직 변경
        boolean hasNext = false;
        Long nextCursor = null;

        if (tab.isConfirmedTab()) {
            hasNext = false;
            nextCursor = null;
        } else {
            // 대기 중, 지난 주문 탭은 기존 무한 스크롤 방식 유지
            if (orders.size() > size) {
                hasNext = true;
                orders = orders.subList(0, size);
            }
            nextCursor = orders.isEmpty() ? null : orders.get(orders.size() - 1).getId();
        }

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
                orders, totalElements, hasNext, nextCursor, itemsByOrderId, reorderMemberIds, tab
        );
    }

    public OwnerOrderDetailResponse.OrderDetailDTO getOwnerOrderDetail(Long ownerId, Long orderId) {
        // 사장님 계정 유효성 검증
        validateOwner(ownerId);

        // 주문 데이터 조회
        Order order = orderRepository.findByIdAndOwnerIdWithItems(orderId, ownerId)
                .orElseThrow(() -> new GeneralException(OrderErrorStatus.ORDER_NOT_FOUND));

        Payment payment = paymentRepository.findFirstByOrderId(order.getOrderId()).orElse(null);

        List<Long> orderItemIds = order.getOrderItems().stream()
                .map(OrderItem::getId)
                .collect(Collectors.toList());

        // IN 쿼리로 연관된 옵션들을 한 방에 조회 후 Map으로 그룹화
        List<OrderItemOption> allOptions = orderItemOptionRepository.findByOrderItemIdIn(orderItemIds);
        Map<Long, List<OrderItemOption>> optionsByOrderItemId = allOptions.stream()
                .collect(Collectors.groupingBy(opt -> opt.getOrderItem().getId()));

        return OwnerOrderDetailConverter.toOrderDetailDTO(order, payment, optionsByOrderItemId);
    }

    private void validateOwner(Long ownerId) {
        // 해당 ID를 가진 회원이 DB에 존재하는지 확인
        Member member = memberRepository.findById(ownerId)
                .orElseThrow(() -> new GeneralException(SignupErrorStatus.MEMBER_NOT_FOUND));

        // 해당 회원 계정이 사용 가능 계정인지 확인
        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new GeneralException(MemberErrorStatus.MEMBER_NOT_ACTIVE);
        }

        // 그 회원이 BUSINESS 권한을 가진 계정인지 확인
        if (member.getMemberType() != MemberType.BUSINESS) {
            throw new GeneralException(SignupErrorStatus.NOT_BUSINESS_MEMBER);
        }
    }
}