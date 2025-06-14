package com.ahimsasystems.chenup.processor.model;

import java.util.Map;

public class RelationshipModel extends MutableElementModel {

    private final Map<String, EntityModel> entities = new java.util.HashMap<>();

    public RelationshipModel(String packageName, String name, Map<String, FieldModel> fields, Map<String, EntityModel> entities) {

        super(packageName, name, fields);

        this.entities.putAll(entities);
    }

    public Map<String, EntityModel> getEntities() {
        return entities;
    }

//    @Override
//    public String toString() {
//        return "RelationshipModel{" +
//                "packageName='" + getPackageName() + '\'' +
//                ", name='" + getName() + '\'' +
//                "fields=" + getFields() +
//                "entities=" + entities +
//                '}';
//    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb
                .append("\n")
                .append("(RelationshipModel\n")
                .append("  :packageName \"")
                .append(getPackageName())
                .append("\"\n")
                .append("  :name \"")
                .append(getName())
                .append("\"\n");


        sb.append("  :fields\n");
        for (Map.Entry<String, FieldModel> entry : getFields().entrySet()) {
            sb
                    .append("    (:")
                    .append(entry.getKey())
                    .append(" ")
                    .append(entry.getValue().toString())
                    .append(")\n");
        }

        sb.append(")");

//        for (Map.Entry<String, EntityModel> entry : entities.entrySet()) {
//            sb.append("    (:").append(entry.getKey()).append(" ")
//                    .append(entry.getValue().toSummaryString()).append(")\n");
//        }


        return sb.toString();

    }


}