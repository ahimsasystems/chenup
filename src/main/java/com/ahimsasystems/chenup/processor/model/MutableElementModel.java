package com.ahimsasystems.chenup.processor.model;

import java.util.Map;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.
public class MutableElementModel {
    private String packageName;
    private String name;
    private Map<String, FieldModel> fields;

    public MutableElementModel(String packageName, String name, Map<String, FieldModel> fields) {
        this.packageName = packageName;
        this.name = name;
        this.fields = fields;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, FieldModel> getFields() {
        return fields;
    }

    public void setFields(Map<String, FieldModel> fields) {
        this.fields = fields;
    }

    public String getFullName() {
        return packageName + "." + name;
    }

    @Override
    public String toString() {
        return "MutableElementModel{" +
                "packageName='" + packageName + '\'' +
                ", name='" + name + '\'' +
                ", fields=" + fields +
                '}';
    }
}
