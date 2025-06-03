package com.ahimsasystems.chenup.processor.model;

public class FieldModel {
    private String name;
    private String type;
    private Access access;
    private boolean hasDefaultReader;
    private boolean hasDefaultWriter;

    public FieldModel(String name, String type, Access access, boolean hasDefaultReader, boolean hasDefaultWriter) {
        this.name = name;
        this.type = type;
        this.access = access;
        this.hasDefaultReader = hasDefaultReader;
        this.hasDefaultWriter = hasDefaultWriter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public boolean getHasDefaultReader() {
        return hasDefaultReader;
    }

    public void setHasDefaultReader(boolean hasDefaultReader) {
        this.hasDefaultReader = hasDefaultReader;
    }

    public boolean getHasDefaultWriter() {
        return hasDefaultWriter;
    }

    public void setHasDefaultWriter(boolean hasDefaultWriter) {
        this.hasDefaultWriter = hasDefaultWriter;
    }

    @Override
    public String toString() {
        return "FieldModel{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", access=" + access +
                ", getHasDefaultReader=" + hasDefaultReader +
                ", hasDefaultWriter=" + hasDefaultWriter +
                '}';
    }
}