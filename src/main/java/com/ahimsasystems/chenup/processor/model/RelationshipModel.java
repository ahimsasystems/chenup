package com.ahimsasystems.chenup.processor.model;

import java.util.Map;

public class RelationshipModel extends MutableElementModel{

    private final Map<String, EntityModel> entities = new java.util.HashMap<>();



    public RelationshipModel(String packageName, String name, Map<String, FieldModel> fields, Map<String, EntityModel> entities)  {

        super(packageName, name, fields);

        this.entities.putAll(entities);
    }


}