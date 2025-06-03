package com.ahimsasystems.chenup.core;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.
public abstract class AbstractPersistenceCapable implements PersistenceCapable {

    private UUID id = UUIDv7Generator.generateUUIDv7(); // Default to a new UUID if not set



    public UUID getId() {
        return id;
    }

    public void setId(@NotNull UUID id) {
        this.id = id;
    }

    final private MetaData metaData = new MetaData(); // Initialize with default MetaData

    // This approach should probably be replaced or augmented to use the database's transaction time. Possibly just need to override this method in subclasses that require a different behavior.
    public MetaData getMetaData() {
        if (metaData.getCreationDateTime() == null) {
            metaData.setCreationDateTime(java.time.Instant.now());
        }
        return metaData;
    }

    public void dirty() {

        if (persistenceManager != null) {
            persistenceManager.dirty(this);
            metaData.incrementVersion();
        }
    }

        PersistenceManager persistenceManager;
        public void setPersistenceManager(PersistenceManager persistenceManager) {
            this.persistenceManager = persistenceManager;
        }
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    // Additional common methods can be added here if needed.
}

