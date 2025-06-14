package com.ahimsasystems.chenup.postgresdb;

import com.ahimsasystems.chenup.core.AbstractPersistenceCapable;
import com.ahimsasystems.chenup.core.MetaData;
import com.ahimsasystems.chenup.core.PersistenceCapable;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.
public abstract class PostgresAbstractPersistenceCapable extends AbstractPersistenceCapable {
    private PostgresPersistenceManager persistenceManager;

    public PostgresPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public void setPersistenceManager(PostgresPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }




}
