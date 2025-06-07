package com.ahimsasystems.chenup.postgresdb;

import com.ahimsasystems.chenup.core.Mapper;
import com.ahimsasystems.chenup.core.PersistenceManager;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.

/* This is an abstract class that provides a template for mapping database records to Java objects.
 * It uses a DataSource to read connections and execute SQL queries.
 * The `read` method fetches a record by its UUID and maps the result set to a newly created Java object.
 * Subclasses must implement the `mapFields` method for the specific type they are mapping.
 * This design follows the Template Method pattern, allowing subclasses to define specific mapping logic while reusing the common retrieval logic.
 */
public abstract class PostgresAbstractMapper implements Mapper {
    private PostgresPersistenceManager persistenceManager; // <-- Can this just be made a PersistenceManager? Or does it need to be a PostgresPersistenceManager?


    public PostgresPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = (PostgresPersistenceManager) persistenceManager;
    }

//    public void setConnection(@NotNull Connection connection) {
//        this.persistenceManager.setConnection(connection);
//    }

    public Connection getConnection() {
        return this.getPersistenceManager().getConnection();
    }














}
