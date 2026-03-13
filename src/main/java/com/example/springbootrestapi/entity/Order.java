package com.example.springbootrestapi.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "orders")
public class Order {

    @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

  // BAD: inconsistent indentation (2, 4, 6 spaces mixed)
    private Long userId;
  private Long bookId;

    // BAD: vague name - should be "quantity"
  private int q;

    // BAD: vague name - should be "totalPrice"
  private double tp;

  // BAD: vague status codes - magic strings, no enum
    private String stat;   // "P"=pending, "C"=confirmed, "S"=shipped, "D"=delivered, "X"=cancelled

    // BAD: using java.util.Date instead of java.time.LocalDateTime
  private Date d;  // order date

    // BAD: unused field leftover from old design
  private String tmp;

  public Order() {}

    public Order(Long userId, Long bookId, int q, double tp) {
      this.userId = userId;
        this.bookId = bookId;
  this.q = q;
        this.tp = tp;
      this.stat = "P";
    this.d = new Date();
    }

    public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }

  public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

  public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }

    public int getQ() { return q; }
  public void setQ(int q) { this.q = q; }

  public double getTp() { return tp; }
    public void setTp(double tp) { this.tp = tp; }

  public String getStat() { return stat; }
    public void setStat(String stat) { this.stat = stat; }

    public Date getD() { return d; }
  public void setD(Date d) { this.d = d; }

  public String getTmp() { return tmp; }
    public void setTmp(String tmp) { this.tmp = tmp; }
}
