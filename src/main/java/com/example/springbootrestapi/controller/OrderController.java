package com.example.springbootrestapi.controller;

import com.example.springbootrestapi.dto.OrderSummary;
import com.example.springbootrestapi.entity.Order;
import com.example.springbootrestapi.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // BAD: all parameters come in via Map - no type safety, no validation
    @PostMapping
    public ResponseEntity<?> placeOrder(@RequestBody Map<String, Object> b) {
        try {
            // BAD: vague variable names, unchecked casts
            Long x = Long.parseLong(b.get("userId").toString());
            Long y = Long.parseLong(b.get("bookId").toString());
            int q = Integer.parseInt(b.get("qty").toString());
            String r = b.get("role") != null ? b.get("role").toString() : "USER";
            String c = b.get("coupon") != null ? b.get("coupon").toString() : null;
            boolean m = b.get("member") != null && Boolean.parseBoolean(b.get("member").toString());

            Order o = orderService.placeOrder(x, y, q, r, c, m);
            if (o == null) {
                return ResponseEntity.badRequest().body("Order failed");
            }
            return ResponseEntity.ok(o);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid number in request: userId, bookId or qty must be valid numbers");
        } catch (NullPointerException e) {
            return ResponseEntity.badRequest().body("Missing required fields: userId, bookId and qty are required");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Something went wrong");
        }
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getOrderSummary(@PathVariable Long id) {
        try {
            OrderSummary summaryOpt = orderService.getOrderSummary(id);
            if (summaryOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            OrderSummary summary = summaryOpt.get();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error loading order summary");
        }
    }

    @GetMapping("/user/{uid}")
    public ResponseEntity<List<Order>> getOrdersForUser(@PathVariable Long uid) {
        // BAD: no authorization - any user can see any other user's orders
        List<Order> data = orderService.getOrdersByUser(uid);
        return ResponseEntity.ok(data);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        // BAD: no pagination, no auth - dumps entire orders table
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // BAD: status update uses single-letter param with no docs
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String s) {
        boolean ok = orderService.updateOrderStatus(id, s);
        if(!ok) {
            return ResponseEntity.badRequest().body("Invalid transition or order not found");
        }
        return ResponseEntity.ok("Status updated to: " + s);
    }
}
