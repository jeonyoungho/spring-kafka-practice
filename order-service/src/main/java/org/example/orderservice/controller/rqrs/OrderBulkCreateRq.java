package org.example.orderservice.controller.rqrs;

import java.util.List;

public record OrderBulkCreateRq(List<OrderCreateRq> orders) {}
