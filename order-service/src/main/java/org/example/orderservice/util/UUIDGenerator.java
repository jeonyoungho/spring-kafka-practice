package org.example.orderservice.util;

import java.util.UUID;

public final class UUIDGenerator {
    private UUIDGenerator() {}

    public static String generate() {
        return UUID.randomUUID().toString();
    }
}
