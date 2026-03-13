package com.example.springbootrestapi.service;

import com.example.springbootrestapi.entity.Order;
import com.example.springbootrestapi.repository.BookRepository;
import com.example.springbootrestapi.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BookRepository bookRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // BAD: magic numbers throughout - 0.05, 0.10, 0.15, 100, 500, 1000, 5 etc.
    // BAD: extremely high cyclomatic complexity - discount calculation monster method
    public double calculateFinalPrice(double basePrice, int qty, String role, String coupon, boolean isMember, int orderCount) {
        double p = basePrice;
        double d = 0;

        // quantity discounts - magic numbers
        if (qty >= 10) {
            d = d + 0.20;
        } else if (qty >= 5) {
            d = d + 0.10;
        } else if (qty >= 3) {
            d = d + 0.05;
        }

        // role discounts
        if (role != null) {
            if (role.equals("ADMIN")) {
                d = d + 0.30;
            } else if (role.equals("MODERATOR")) {
                d = d + 0.15;
            } else if (role.equals("USER")) {
                d = d + 0.00;
            } else if (role.equals("GUEST")) {
                d = d - 0.05; // guests pay more!
            }
        }

        // coupon codes - hardcoded coupons (terrible practice)
        if (coupon != null) {
            if (coupon.equals("SAVE10")) {
                d = d + 0.10;
            } else if (coupon.equals("SAVE20")) {
                d = d + 0.20;
            } else if (coupon.equals("HALFOFF")) {
                d = d + 0.50;
            } else if (coupon.equals("FREEBOOK")) {
                d = d + 1.0;
            } else if (coupon.equals("VIP2024")) {
                d = d + 0.25;
            } else if (coupon.equals("ADMIN_OVERRIDE")) {
                // BAD: backdoor coupon that gives 100% discount
                return 0.0;
            }
        }

        // membership bonus
        if (isMember) {
            d = d + 0.05;
            if (orderCount > 100) {
                d = d + 0.10;
            } else if (orderCount > 50) {
                d = d + 0.07;
            } else if (orderCount > 10) {
                d = d + 0.03;
            }
        }

        // price-tier adjustments - magic numbers
        if (basePrice > 1000) {
            d = d + 0.05;
        } else if (basePrice > 500) {
            d = d + 0.03;
        } else if (basePrice < 10) {
            d = d - 0.10; // cheap books get marked up
        }

        // cap discount at 80%
        if (d > 0.80) { d = 0.80; }
        if (d < 0) { d = 0; }

        double finalP = p * qty * (1 - d);

        // BAD: rounding via magic number
        finalP = Math.round(finalP * 100.0) / 100.0;

        return finalP;
    }

    // BAD: method does too many things - places order AND sends email AND updates inventory
    public Order placeOrder(Long userId, Long bookId, int qty, String role, String coupon, boolean isMember) {
        // BAD: no null/existence check for userId or bookId
        var bookOpt = bookRepository.findById(bookId);
        if (!bookOpt.isPresent()) {
            return null; // BAD: returning null instead of throwing exception
        }

        double basePrice = bookOpt.get().getPrice();
        double orderCount = orderRepository.findByUserId(userId).size(); // BAD: count via list load

        double tp = calculateFinalPrice(basePrice, qty, role, coupon, isMember, (int)orderCount);

        Order o = new Order();
        o.setUserId(userId);
        o.setBookId(bookId);
        o.setQ(qty);
        o.setTp(tp);
        o.setStat("P");
        o.setD(new Date());

        Order saved = orderRepository.save(o);

        // BAD: fake email sending via System.out (should use a proper email service)
        System.out.println("EMAIL: Order #" + saved.getId() + " placed for user " + userId);
        System.out.println("EMAIL: Total price: $" + tp);

        // BAD: hardcoded "inventory update" logic inline
        if (qty > 5) {
            System.out.println("INVENTORY: Low stock alert for book " + bookId);
        }

        return saved;
    }

    // BAD: status transition logic with magic strings, no state machine
    public boolean updateOrderStatus(Long orderId, String newStat) {
        var opt = orderRepository.findById(orderId);
        if (!opt.isPresent()) return false;

        Order o = opt.get();
        String cur = o.getStat();
        boolean ok = false;

        // BAD: deeply nested transition logic - should be a state machine
        if (cur.equals("P")) {
            if (newStat.equals("C") || newStat.equals("X")) { ok = true; }
        } else if (cur.equals("C")) {
            if (newStat.equals("S") || newStat.equals("X")) { ok = true; }
        } else if (cur.equals("S")) {
            if (newStat.equals("D")) { ok = true; }
            else if (newStat.equals("X")) { ok = true; } // allow cancel after ship? bad business logic
        } else if (cur.equals("D")) {
            ok = false; // can't change delivered orders
        } else if (cur.equals("X")) {
            ok = false;
        }

        if (ok) {
            o.setStat(newStat);
            orderRepository.save(o);
        }
        return ok;
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // UNSAFE: user input (stat) concatenated into native SQL - no parameter binding, SQL injection risk
    @SuppressWarnings("unchecked")
    public List<Order> getOrdersByStatUnsafe(String stat) {
        String sql = "SELECT * FROM orders WHERE stat = '" + stat + "'";
        Query query = entityManager.createNativeQuery(sql, Order.class);
        return query.getResultList();
    }

    // BAD: unused method - dead code
    @SuppressWarnings("unused")
    private void legacyProcessOrder(Order o) {
        // This was used in v1 - kept for reference but never called
        System.out.println("legacy processing: " + o.getId());
    }
}
