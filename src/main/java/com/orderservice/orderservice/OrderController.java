package com.orderservice.orderservice;
import com.orderservice.orderservice.entity.Order;
import com.orderservice.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Random;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;

@RestController
public class OrderController {

    private final Random random = new Random();
    private final Tracer tracer = GlobalOpenTelemetry.getTracer("order-service");

    @Autowired
    private OrderRepository orderRepository;

    @GetMapping("/createOrder")
    public ResponseEntity<String> createOrder() throws InterruptedException {
        long startTime = System.currentTimeMillis();

        // Simulate DB processing delay
        int processingDelay = random.nextInt(200) + 100;
        Thread.sleep(processingDelay);

        // Create custom span for database processing
        Span dbSpan = tracer.spanBuilder("db_process_order")
                .setSpanKind(io.opentelemetry.api.trace.SpanKind.INTERNAL)
                .setAttribute("db.operation", "insert")
                .setAttribute("db.table", "orders")
                .setAttribute("processing.delay.ms", processingDelay)
                .startSpan();

        try {
            // Make the custom span current
            try (var scope = dbSpan.makeCurrent()) {
                // Create order entity
                Order order = new Order();
                order.setProcessingTimeMs(processingDelay);

                // Generate random number 0-9. If < 9 (90% chance), succeed. Otherwise fail.
                if (random.nextInt(10) < 9) {
                    // SUCCESS CASE - Save to database
                    order.setStatus("SUCCESS");
                    Order savedOrder = orderRepository.save(order);

                    // Add success event to the custom span
                    dbSpan.addEvent("order_creation_succeeded",
                            Attributes.of(
                                    AttributeKey.stringKey("order.id"), savedOrder.getId().toString(),
                                    AttributeKey.stringKey("order.status"), "SUCCESS"
                            ));

                    dbSpan.setAttribute("order.result", "success");
                    dbSpan.setAttribute("order.id", savedOrder.getId().toString());

                    return ResponseEntity.ok("Order created successfully with ID: " + savedOrder.getId());
                } else {
                    // FAILURE CASE - Still save to database but with FAILED status
                    order.setStatus("FAILED");
                    Order savedOrder = orderRepository.save(order);

                    // Add failure event to the custom span
                    dbSpan.addEvent("order_creation_failed",
                            Attributes.of(
                                    AttributeKey.stringKey("order.id"), savedOrder.getId().toString(),
                                    AttributeKey.stringKey("order.status"), "FAILED",
                                    AttributeKey.stringKey("failure.reason"), "simulated_random_failure"
                            ));

                    dbSpan.setAttribute("order.result", "failure");
                    dbSpan.setAttribute("order.id", savedOrder.getId().toString());
                    dbSpan.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, "Order creation failed");

                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to create order due to internal error. Order ID: " + savedOrder.getId());
                }
            }
        } catch (Exception e) {
            // Database error case
            dbSpan.addEvent("database_error",
                    Attributes.of(
                            AttributeKey.stringKey("error.message"), e.getMessage(),
                            AttributeKey.stringKey("error.type"), e.getClass().getSimpleName()
                    ));

            dbSpan.setAttribute("order.result", "database_error");
            dbSpan.setStatus(io.opentelemetry.api.trace.StatusCode.ERROR, "Database error: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Database error occurred: " + e.getMessage());
        } finally {
            // Always end the span
            dbSpan.end();
        }
    }

    @GetMapping("/checkInventory")
    public String checkInventory() throws InterruptedException {
        // Random delay between 200ms and 800ms
        int delay = 200 + random.nextInt(600);
        Thread.sleep(delay);

        try {
            // Query database to get order counts (adds DB span to trace)
            long successCount = orderRepository.countByStatus("SUCCESS");
            long failedCount = orderRepository.countByStatus("FAILED");

            return String.format("Inventory check completed in %dms. Orders: %d successful, %d failed",
                    delay, successCount, failedCount);
        } catch (Exception e) {
            return "Inventory check completed in " + delay + "ms. Database unavailable.";
        }
    }
}