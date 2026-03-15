package com.example.springbootrestapi.repository;

import com.example.springbootrestapi.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStat(String stat);

    // Raw SQL with no user input - static filter only (exclude cancelled)
    @Query(value = "SELECT * FROM orders WHERE stat != 'X'", nativeQuery = true)
    List<Order> findAllActiveOrders();

    // BAD: raw SQL with magic status strings instead of constants/enum
    @Query(value = "SELECT * FROM orders WHERE stat != 'X' AND user_id = ?1", nativeQuery = true)
    List<Order> findActiveOrdersByUserId(Long userId);

    // BAD: no pagination - could return huge result set
    List<Order> findByBookId(Long bookId);
}
