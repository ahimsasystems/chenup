package com.ahimsasystems.chenup.postgresdb;

import com.ahimsasystems.chenup.core.AbstractPersistenceCapable;
import com.ahimsasystems.chenup.core.AbstractPersistenceManager;
import com.ahimsasystems.chenup.core.PersistenceCapable;
import com.ahimsasystems.chenup.core.PersistenceManager;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.ApplicationScoped;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.

/**
 * PostgresPersistenceManager is an implementation of PersistenceManager that handles the persistence of objects in a PostgreSQL database.
 * Rather than planting flags in the objects to track their state, it uses collections to manage the state of persistent objects.
 * These collections include:
 * - persistentNew: A set of objects that are newly created and not yet persisted to the database.
 * - persistentAll: A map that contains all objects currently managed by the persistence manager, indexed by their UUID.
 * - ... Other states as needed.
 * <p>
 * persistentAll should be implemented such that the other collections are all subsets of it.
 * So whenever an object is added to any of the other collections, it should also be added to persistentAll.
 * <p>
 * <p>
 * The other collections should be implemented as disjoint sets, meaning that an object should only appear in one of them at a time.
 * At this point there is no restriction that says an object must be in one of the subsets of persistentAll, that is, these subsets do not partition persistentAll. This may change in the future.
 * The plan is to create a new set of collection classes or at least interfaces that will allow for this kind of disjoint set management automatically.
 * This is very much a preliminary step towards allowing for relationships that function this way, for example, a Relative relationship return all objects in sub-relationships, including spouses, children, etc. This will not be implemented in the first release, however.
 * <p>
 * Note that if an object does not have an ID, one will be generated for it.
 * However, if an object already has an ID, it will not be changed.
 * The caller may choose to set the ID before calling persist, even for an object which has not yet been marked with persist(), or it may choose to let the persistence manager generate one at the time persist() is called.
 * Since these are UUIDs, they will be unique of how the UUID is generated. However, it is highly recommended that the caller use a UUIDv7 generator to ensure that the IDs are unique and ordered by creation time. chenup.core.UUIDv7Generator is a good choice for this purpose.
 * <p>
 * ToDo:
 * - Verify whether to use @NotNull from JetBrains annotations or Java's built-in @NonNull.
 */
@Unremovable
@ApplicationScoped
public class PostgresPersistenceManager extends AbstractPersistenceManager {
    private final Map<Class, Supplier> mapperRegistry = new HashMap<>();
    private final Map<Class, Supplier> typeRegistry = new HashMap<>();
    final private Map<UUID, PersistenceCapable> persistentNew = new HashMap<>();
    final private Map<UUID, PersistenceCapable> persistentAll = new HashMap<>();
    final private Map<UUID, PersistenceCapable> persistentDirty = new HashMap<>();
    final private Map<UUID, Class> persistentInterfaceTypes = new HashMap<>();

    private Map<UUID, PersistenceCapable> getPersistentNew() {
        return persistentNew;
    }

    public void registerMapper(Class theClass, Supplier mapperConstructor) {
        mapperRegistry.put(theClass, mapperConstructor);
    }

    public void registerType(Class theClass, Supplier constructor) {
        typeRegistry.put(theClass, constructor);



    }

    public Map<UUID, PersistenceCapable> getPersistentAll() {
        return persistentAll;
    }



    public Object create(@NotNull Class interfaceClass) {

        var constructor = typeRegistry.get(interfaceClass);
        if (constructor == null) {
            throw new IllegalArgumentException("No constructor registered for class: " + interfaceClass.getName());
        }
        var instance = constructor.get();
        if (!(instance instanceof PersistenceCapable)) {
            throw new IllegalArgumentException("The class " + interfaceClass.getName() + " does not implement PersistenceCapable.");
        }


        persistentNew.put(((PersistenceCapable) instance).getId(), (PersistenceCapable) instance);
        persistentAll.put(((PersistenceCapable) instance).getId(), (PersistenceCapable) instance);

        persistentInterfaceTypes.put(((PersistenceCapable) instance).getId(), interfaceClass);


        ( (PostgresAbstractPersistenceCapable) instance).setPersistenceManager(this);

        return instance;
    }


    /**
     * Note that this uses Bloch's (from 3rd ed.) Item 33: Consider typesafe heterogeneous containers.
     */
    public synchronized <T extends PersistenceCapable> T read(UUID id, Class interfaceClass, Connection conn) {

        if (persistentAll.containsKey(id)) {
            return (T) persistentAll.get(id);
        }
        // ... check the database for the object with this ID
        // If not found, return null or throw an exception based on your design choice.
        // For now, assume it is there and call the mapper to read it.
        // Got to figure out the class of the ID, which is a UUID, so we can get the mapper for it.
        // I guess we need another registry for the types of the objects by UUID.
        // For now, pass it?
        Supplier<?> mapperConstructor = mapperRegistry.get(interfaceClass);
        if (mapperConstructor != null) {
            PostgresAbstractMapper mapper = (PostgresAbstractMapper) mapperConstructor.get();

            mapper.setPersistenceManager(this);
            var result = (T) mapper.read(id, conn);

            // This must be a PostgresAbstractPersistenceCapable object, which extends AbstractPersistenceCapable, which implements PersistenceCapable. So we can downcast it so we can access the metadata.
            PostgresAbstractPersistenceCapable pcap = (PostgresAbstractPersistenceCapable) result;






            persistentAll.put(id, (PersistenceCapable) result);
            persistentInterfaceTypes.put(id, interfaceClass);

            return result;
        }

        return null;
    }

    @Override
    public Supplier<?> getMapper(Class theClass) {
        return mapperRegistry.get(theClass);
    }

    public void dirty(@NotNull PersistenceCapable object) {
        // Mark the object as dirty, meaning it has been modified and needs to be persisted.
        // This could involve updating its state in the persistentNew collection or similar.
        UUID id = object.getId();
        if (id != null && persistentAll.containsKey(id)) {
            persistentDirty.put(id, object);
        }
    }

    /**
     * This method flushes all the new and dirty objects to the database.
     * It should be called at the end of a transaction or when you want to persist all changes made to the objects.
     * It will iterate over the persistentDirty collection and call the upsert method on each object.
     * On successful conclusion, both the persistentNew and persistentDirty collections will be cleared.
     * * Note that this method does not commit the transaction; it only writes them to the database but does not commit the transaction.
     * That will normally happen at the end of a @Transactional method in the service layer or wherever the transaction management is handled.
     *
     * @param conn The database connection to use for flushing the changes.
     * @throws SQLException If there is an error during the database operation.
     */
    public void flush(Connection conn) throws SQLException {



        List<UUID> removalIDs = new ArrayList<>();
        // Iterate over the persistentNew collection and insert new objects
        for (PersistenceCapable newObject : persistentNew.values()) {

            PostgresAbstractPersistenceCapable pcap = (PostgresAbstractPersistenceCapable) newObject;



            var interfaceClass = persistentInterfaceTypes.get(newObject.getId());
            Supplier<?> mapperConstructor = mapperRegistry.get(interfaceClass);
            var mapper = (PostgresAbstractMapper) mapperConstructor.get();
            mapper.setPersistenceManager(this);


            mapper.upsert(newObject, conn);
            // Remove the object from persistentNew after upserting
            // persistentNew.remove(newObject.getId());

            removalIDs.add(newObject.getId());


        }

        // Remove all new objects from persistentNew after flushing
        for (UUID id : removalIDs) {
            persistentNew.remove(id);
        }
        removalIDs.clear();

        // Iterate over the persistentDirty collection and update existing objects
        for (PersistenceCapable dirtyObject : persistentDirty.values()) {

            var interfaceClass = persistentInterfaceTypes.get(dirtyObject.getId());
            Supplier<?> mapperConstructor = mapperRegistry.get(interfaceClass);
            var mapper = (PostgresAbstractMapper) mapperConstructor.get();
            mapper.setPersistenceManager(this);


            mapper.upsert(dirtyObject, conn);
            // Remove the object from persistentNew after upserting
            // persistentNew.remove(newObject.getId());

            removalIDs.add(dirtyObject.getId());


        }

        // Remove all new objects from persistentNew after flushing
        for (UUID id : removalIDs) {
            persistentDirty.remove(id);
        }
        removalIDs.clear();
    }

}
// Other methods for update, delete, find, etc. can be added here.

