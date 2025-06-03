package com.ahimsasystems.chenup.processor.model;

import java.util.Map;

public class RelationshipModel extends MutableElementModel{

    public Map<String, EntityModel> getEntities() {
        return entities;
    }

    private final Map<String, EntityModel> entities = new java.util.HashMap<>();



    public RelationshipModel(String packageName, String name, Map<String, FieldModel> fields, Map<String, EntityModel> entities)  {

        super(packageName, name, fields);

        this.entities.putAll(entities);
    }

    @Override
    public String toString() {
        return "RelationshipModel{" +
                "packageName='" + getPackageName() + '\'' +
                ", name='" + getName() + '\'' +
                "fields=" + getFields() +
                "entities=" + entities +
                '}';
    }
}