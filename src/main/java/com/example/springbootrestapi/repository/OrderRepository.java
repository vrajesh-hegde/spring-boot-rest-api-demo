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

    // BAD: raw SQL with magic status strings instead of constants/enum
    @Query(value = "SELECT * FROM orders WHERE stat != 'X' AND user_id = ?1", nativeQuery = true)
    List<Order> findActiveOrdersByUserId(Long userId);

    // BAD: more native SQL with hardcoded status strings 'P', 'C', 'S'
    @Query(value = "SELECT * FROM orders WHERE stat IN ('P', 'C', 'S') ORDER BY id DESC", nativeQuery = true)
    List<Order> findPendingConfirmedOrShipped();

    @Query(value = "SELECT * FROM orders WHERE stat = 'D' AND user_id = ?1", nativeQuery = true)
    List<Order> findDeliveredByUser(Long userId);

    @Query(value = "SELECT * FROM orders WHERE stat != 'X'", nativeQuery = true)
    List<Order> findAllNotCancelled();

    // BAD: no pagination - could return huge result set
    List<Order> findByBookId(Long bookId);
}
