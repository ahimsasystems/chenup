package com.ahimsasystems.chenup.core;

import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.UUID;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.

/** A mapper is a mechanism for moving data between in-memory objects and a persistent store.
 * The Mapper defines the interfaces that all mappers must implement. It is not specific to any
 * particular persistence technology, allowing for flexibility in implementation.
 *
 */
public interface Mapper {

    /** Right now this guy is checking the persistent store to see if the object already exists in the PersistenceManage.
     * But i believe the PersistenceManager should be able to check this itself before calling retrieve.
     */
    public Object read(@NotNull UUID id);


    /** insert and update make assumptions that need to be checked before they are called.
     * Insert assumes that the object is not already in the persistent store and will do a straight insert
     * without checking for existence, which could lead to a duplicate key error.
     * Update assumes that the object is already in the persistent store and will do an update without checking for existence, which could lead to an error if the object does not exist.
     * These are implemented this way so the caller can decide how to handle these cases andto avoid unnecessary database queries, for instance, if the PersistenceManager already knows that the object is new or existing.
     * @param object
     * @throws SQLException
     */
    public void insert(@NotNull Object object) throws SQLException;

    public void update(@NotNull Object object) throws SQLException;

    // This shouldn't be necessary at this level because it is implementation-specific.
    // public void setConnection(@NotNull java.sql.Connection connection);

    public void setPersistenceManager(@NotNull PersistenceManager persistenceManager);

}
