package com.orderservice.orderservice.repository;

import com.orderservice.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(String status);

    long countByStatus(String status);
}