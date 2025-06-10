package com.ahimsasystems.chenup.core.exceptions;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.
public class DeletedObjectAccessException extends RuntimeException {
    private final Object id;

    public DeletedObjectAccessException(Object id) {
        super("Attempt to access deleted object with ID: " + id);
        this.id = id;
    }

    public Object getId() {
        return id;
    }
}

