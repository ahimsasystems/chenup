package com.ahimsasystems.chenup.postgresdb;

import com.ahimsasystems.chenup.core.PersistenceCapable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.
public class PersistenceState {

    final private Map<UUID, PersistenceCapable> persistentNew = new HashMap<>();
    final private Map<UUID, PersistenceCapable> persistentDirty = new HashMap<>();


    public void setDirty(@NotNull PersistenceCapable object) {
        // Mark the object as dirty, meaning it has been modified and needs to be persisted.
        // This could involve updating its state in the persistentDirty collection or similar.
        UUID id = object.getId();
        if (id != null ) {
            persistentDirty.put(id, object);
        }
    }

    public boolean isDirty(@NotNull PersistenceCapable object) {
        // Check if the object is dirty, meaning it has been modified and needs to be persisted.
        UUID id = object.getId();
        return id != null && persistentDirty.containsKey(id);
    }

    public void clearDirty(@NotNull PersistenceCapable object) {
        // Clear the dirty state of the object, meaning it has been persisted and no longer needs to be updated.
        // This could involve removing it from the persistentDirty collection.
        UUID id = object.getId();
        if (id != null) {
            persistentDirty.remove(id);
        }
    }

    public void setNew(@NotNull PersistenceCapable object) {
        // Mark the object as new, meaning it has been created but not yet persisted to the database.
        // This could involve adding it to the persistentNew collection.
        UUID id = object.getId();
        if (id != null) {
            persistentNew.put(id, object);
        }
    }

    public boolean isNew(@NotNull PersistenceCapable object) {
        // Check if the object is new, meaning it has been created but not yet persisted to the database.
        UUID id = object.getId();
        return id != null && persistentNew.containsKey(id);
    }

    public void clearNew(@NotNull PersistenceCapable object) {
        // Clear the new state of the object, meaning it has been persisted and no longer needs to be created.
        // This could involve removing it from the persistentNew collection.
        UUID id = object.getId();
        if (id != null) {
            persistentNew.remove(id);
        }
    }

}
