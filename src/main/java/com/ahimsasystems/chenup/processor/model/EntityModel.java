package com.ahimsasystems.chenup.processor.model;

import java.util.Map;

public class EntityModel extends MutableElementModel {

    public EntityModel(String packageName, String name, Map<String, FieldModel> fields) {
        super(packageName, name, fields);
    }

    /// /    @Override
    /// /    public String toString() {
    /// /        return "EntityModel{" +
    /// /                "packageName='" + getPackageName() + '\'' +
    /// /                ", name='" + getName() + '\'' +
    /// /                ", fields=" + getFields() +
    /// /                '}';
//    }
    @Override
    public String toString() {

        var sb = new StringBuilder();
        sb
                .append("\n")
                .append("(EntityModel\n")
                .append("  :packageName \"").append(getPackageName()).append("\"\n")
                .append("  :name \"").append(getName()).append("\"\n");

        sb.append("  :fields\n");
        for (Map.Entry<String, FieldModel> entry : getFields().entrySet()) {
            sb.append("    (:").append(entry.getKey()).append(" ")
                    .append(entry.getValue().toString()).append(")\n");
        }

        sb.append(")");


        return sb.toString();
    }

    public String toSummaryString() {
        return new StringBuilder()
                .append("(EntityModel ")
                .append(":packageName \"").append(getPackageName()).append("\" ")
                .append(":name \"").append(getName()).append("\")")
                .toString();
    }


}