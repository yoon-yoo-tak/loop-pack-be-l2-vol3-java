package com.loopers.domain.order;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Transactional
    public Order createOrder(Long userId, List<OrderItemCommand> itemCommands) {
        if (itemCommands == null || itemCommands.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "주문 항목은 비어있을 수 없습니다.");
        }

        List<OrderItem> orderItems = itemCommands.stream()
            .map(command -> {
                Product product = productService.getById(command.productId());
                product.decrementStock(command.quantity());
                return new OrderItem(
                    product.getId(),
                    command.quantity(),
                    product.getName(),
                    product.getPrice(),
                    product.getBrand().getName()
                );
            })
            .toList();

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

    public record OrderItemCommand(Long productId, int quantity) {}
}
