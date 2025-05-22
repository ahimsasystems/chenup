package com.ahimsasystems.chenup.processor.template;

sealed interface TemplateToken permits TextToken, PlaceholderToken {}

record TextToken(String text) implements TemplateToken {}
record PlaceholderToken(String key) implements TemplateToken {}

