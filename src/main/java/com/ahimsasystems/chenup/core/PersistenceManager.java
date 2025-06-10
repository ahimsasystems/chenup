package com.ahimsasystems.chenup.core;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public interface PersistenceManager{
    // These are the usual user-facing methods
    void dirty(PersistenceCapable pc);


    public Object create(@NotNull Class interfaceClass);

    public <T extends PersistenceCapable> T read(UUID id, Class interfaceClass) ;

    // These are the methods used more by the framework itself, not by the user.
    // I feel like they should be pulled from this interface and put into an extension interface.
    void registerMapper(Class theClass, Supplier<?> mapperConstructor);

    void registerType(Class theClass, Supplier<?> typeConstructor);

    Supplier<?> getMapper(Class theClass);

    // This is a placeholder for being able to find an object by a SQL query.
    // The query string itself should return a set of IDs, which will then be used to fetch the objects.
    // The IDS should be UUIDs, and the method should return a set of objects that match the query.
        // Example: SQL query could be "SELECT id FROM person WHERE name = 'John Smith'"

        // public Set<T> find(String sql, Class interfaceClass);

        // Here's another example, to find all employments for a given person:
    // Example : SQL query could be "SELECT id FROM employment WHERE person_id = ?"


}
