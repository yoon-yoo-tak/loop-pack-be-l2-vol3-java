package com.loopers.domain.order;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(Long userId, List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목은 비어있을 수 없습니다.");
        }

        Order order = new Order(userId, orderItems);
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 주문입니다."));
    }

    @Transactional(readOnly = true)
    public Page<Order> getAll(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(Long userId, ZonedDateTime startAt, ZonedDateTime endAt) {
        return orderRepository.findAllByUserIdAndCreatedAtBetween(userId, startAt, endAt);
    }
}
