package com.ahimsasystems.chenup.postgresdb;

import com.ahimsasystems.chenup.core.PersistenceContext;

import java.sql.Connection;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.
public final class PostgresContext implements PersistenceContext {
    private final Connection connection;
    private final PostgresPersistenceManager persistenceManager;
    public PostgresContext(Connection connection, PostgresPersistenceManager persistenceManager) {
        this.connection = connection;
        this.persistenceManager = persistenceManager;
    }
    public Connection getConnection() {
        return connection;
    }
    public PostgresPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }
}
