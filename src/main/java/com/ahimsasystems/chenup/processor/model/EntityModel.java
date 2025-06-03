package com.ahimsasystems.chenup.processor.model;

import java.util.Map;

public class EntityModel extends MutableElementModel {

    public EntityModel(String packageName, String name, Map<String, FieldModel> fields) {
        super(packageName, name, fields);
    }

}