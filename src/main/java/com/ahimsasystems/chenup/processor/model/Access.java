package com.ahimsasystems.chenup.processor.model;

public enum Access {
    READ_ONLY,
    WRITE_ONLY,
    READ_WRITE,
    NONE // optional, for truly internal fields
}
