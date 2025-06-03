package com.ahimsasystems.chenup.postgresdb;

import com.ahimsasystems.chenup.core.PersistenceCapable;
import com.ahimsasystems.chenup.core.PersistenceManager;
import com.ahimsasystems.chenup.core.UUIDv7Generator;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.util.*;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.

/** * PostgresPersistenceManager is an implementation of PersistenceManager that handles the persistence of objects in a PostgreSQL database.
 * Rather than planting flags in the objects to track their state, it uses collections to manage the state of persistent objects.
 * These collections include:
 * - persistentNew: A set of objects that are newly created and not yet persisted to the database.
 * - persistentAll: A map that contains all objects currently managed by the persistence manager, indexed by their UUID.
 * - ... Other states as needed.
 *
 * persistentAll should be implemented such that the other collections are all subsets of it.
 * So whenever an object is added to any of the other collections, it should also be added to persistentAll.
 *

 * The other collections should be implemented as disjoint sets, meaning that an object should only appear in one of them at a time.
 * At this point there is no restriction that says an object must be in one of the subsets of persistentAll, that is, these subsets do not partition persistentAll. This may change in the future.
 * The plan is to create a new set of collection classes or at least interfaces that will allow for this kind of disjoint set management automatically.
 * This is very much a preliminary step towards allowing for relationships that function this way, for example, a Relative relationship return all objects in sub-relationships, including spouses, children, etc. This will not be implemented in the first release, however.
 *
 * Note that if an object does not have an ID, one will be generated for it.
 * However, if an object already has an ID, it will not be changed.
 * The caller may choose to set the ID before calling persist, even for an object which has not yet been marked with persist(), or it may choose to let the persistence manager generate one at the time persist() is called.
 * Since these are UUIDs, they will be unique of how the UUID is generated. However, it is highly recommended that the caller use a UUIDv7 generator to ensure that the IDs are unique and ordered by creation time. chenup.core.UUIDv7Generator is a good choice for this purpose.
 *
 * ToDo:
 * - Verify whether to use @NotNull from JetBrains annotations or Java's built-in @NonNull.
 *
 */
public class PostgresPersistenceManager implements PersistenceManager {

    public Map<UUID, PersistenceCapable> getPersistentNew() {
        return persistentNew;
    }



    public Map<UUID, PersistenceCapable> getPersistentAll() {
        return persistentAll;
    }



    final private Map<UUID, PersistenceCapable> persistentNew = new HashMap<>();
    final private Map<UUID, PersistenceCapable> persistentAll = new HashMap<>();
    final private Map<UUID, PersistenceCapable> persistentDirty = new HashMap<>();

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private DataSource dataSource;



    void persist(@NotNull PersistenceCapable object) {


        UUID id = object.getId();
        if (id == null) {
           object.setId(UUIDv7Generator.generateUUIDv7());
        }

        if (!persistentAll.containsKey(id)) {
            persistentNew.put(id, object);
            persistentAll.put(id, object);

        }






    }

    PersistenceCapable retrieve(UUID id) {

        if (persistentAll.containsKey(id)) {
            return persistentAll.get(id);
        }
        return null;
        // Logic to retrieve from the database can be added here.
    }

    public void dirty(@NotNull PersistenceCapable object) {
        // Mark the object as dirty, meaning it has been modified and needs to be persisted.
        // This could involve updating its state in the persistentNew collection or similar.
        UUID id = object.getId();
        if (id != null && persistentAll.containsKey(id)) {
            persistentDirty.put(id, object);
        }
    }
    // Other methods for update, delete, find, etc. can be added here.
}
