package org.mainshop.config;

import java.util.UUID;

public final class TestUsers {

    private TestUsers() {
    }

    public static UUID randomUserId() {
        return UUID.randomUUID();
    }
}
