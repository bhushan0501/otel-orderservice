package com.orderservice.orderservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

    // Constructors
    public Order() {
        this.createdAt = LocalDateTime.now();
        this.status = "PROCESSING";
    }

    public Order(String status) {
        this();
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProcessingTimeMs(Integer processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
}