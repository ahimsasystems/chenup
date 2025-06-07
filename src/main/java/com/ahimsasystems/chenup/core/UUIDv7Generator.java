package com.ahimsasystems.chenup.core;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.


import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

public class UUIDv7Generator {

    private static final SecureRandom random = new SecureRandom();

    public static UUID generateUUIDv7() {
        long timestamp = Instant.now().toEpochMilli(); // 48 bits needed
        byte[] rndBytes = new byte[10]; // 80 bits of randomness
        random.nextBytes(rndBytes);

        long msb = 0;
        long lsb = 0;

        // 48 bits timestamp
        msb |= (timestamp & 0xFFFFFFFFFFFFL) << 16;

        // 4 bits version (0111 for version 7)
        msb |= 0x7000L;

        // 12 bits from random
        msb |= ((rndBytes[0] & 0xFFL) << 4) | ((rndBytes[1] & 0xF0L) >> 4);

        // 4 bits variant (10xx)
        lsb |= (0x80L | ((rndBytes[1] & 0x0FL))) << 56;

        // Remaining 56 bits of randomness
        for (int i = 2; i < 10; i++) {
            lsb |= (rndBytes[i] & 0xFFL) << (56 - 8 * (i - 2));
        }

        return new UUID(msb, lsb);
    }


}
