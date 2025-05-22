package com.ahimsasystems.chenup.processor.model;

import java.util.Map;

public record EntityModel (
        String packageName, String name, Map<String, FieldModel> fields) {
}
