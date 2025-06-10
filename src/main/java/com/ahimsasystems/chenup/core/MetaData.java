package com.ahimsasystems.chenup.core;

import java.time.Instant;
import java.util.UUID;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.

//**
/* This class is primarily a design placeholder at this point, and for release 0.1, may not be directly populated since it can be easily updated as needed through direct SQL queries. Also for the initial release, a great deal of this data will be set automatically by the system, such as creation and last update times. And currently all this metadata is stored in the Thing table.

*/
public class MetaData {

    private UUID id;
    private Instant creationDateTime;
    private Instant lastUpdateDateTime;
    private String createdBy;
    private String lastUpdatedBy;
    private int version = -1;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(Instant creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public Instant getLastUpdateDateTime() {
        return lastUpdateDateTime;
    }

    public void setLastUpdateDateTime(Instant lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public int getVersion() {
        if (version < 0) {
            // If version is not set, initialize it to 0
            version = 0;
        }
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    public void incrementVersion() {
        this.version++;
        this.lastUpdateDateTime = Instant.now(); // Update last update time when version is incremented
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    private boolean deleted = false;


}
