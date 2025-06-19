package com.ahimsasystems.chenup.core;

import jakarta.json.bind.annotation.JsonbTransient;
import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.
public abstract class AbstractPersistenceCapable implements PersistenceCapable {

    // TODO: This probably should be explicitly set instead of defaulting to a new UUID.
    private UUID id = UUIDv7Generator.generateUUIDv7(); // Default to a new UUID if not set

    // Use a default clock for timestamps, can be overridden if needed
    @JsonbTransient
    private Clock clock = Clock.systemUTC();

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public Instant getCurrentTime() {
        return clock.instant();
    }

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
// This is now all in the persistence manager.
//    public void dirty() {
//
//        if (persistenceManager != null) {
//            persistenceManager.dirty(this);
//            metaData.incrementVersion();
//        }
//    }

    // Persistence manager knows about the persistence capable objects, not vice versa.
    // This keeps the persistence capable objects very simple and focused on their own data and behavior, without needing to know about the persistence layer.
//        PersistenceManager persistenceManager;
//        public void setPersistenceManager(PersistenceManager persistenceManager) {
//            this.persistenceManager = persistenceManager;
//        }
//    public PersistenceManager getPersistenceManager() {
//        return persistenceManager;
//    }

    // Additional common methods can be added here if needed.
}

