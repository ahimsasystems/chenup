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
    final private Map<UUID, PersistenceCapable> persistentAll = new HashMap<>();
    final private Map<UUID, Class> persistentInterfaceTypes = new HashMap<>();

    final private PersistenceState state = new PersistenceState();


    final private Map<UUID, PersistenceCapable> persistentNew = new HashMap<>();
    final private Map<UUID, PersistenceCapable> persistentDirty = new HashMap<>();


//    public void setDirty(@NotNull PersistenceCapable object) {
//        // Mark the object as dirty, meaning it has been modified and needs to be persisted.
//        // This could involve updating its state in the persistentDirty collection or similar.
//        UUID id = object.getId();
//        if (id != null && persistentAll.containsKey(id)) {
//            persistentDirty.put(id, object);
//        }
//    }
//
////    public boolean isDirty(@NotNull PersistenceCapable object) {
////        // Check if the object is dirty, meaning it has been modified and needs to be persisted.
//        UUID id = object.getId();
//        return id != null && persistentDirty.containsKey(id);
//    }
//
//    public void clearDirty(@NotNull PersistenceCapable object) {
//        // Clear the dirty state of the object, meaning it has been persisted and no longer needs to be updated.
//        // This could involve removing it from the persistentDirty collection.
//        UUID id = object.getId();
//        if (id != null) {
//            persistentDirty.remove(id);
//        }
//    }
//
//    public void setNew(@NotNull PersistenceCapable object) {
//        // Mark the object as new, meaning it has been created but not yet persisted to the database.
//        // This could involve adding it to the persistentNew collection.
//        UUID id = object.getId();
//        if (id != null) {
//            persistentNew.put(id, object);
//         }
//    }
//
//    public boolean isNew(@NotNull PersistenceCapable object) {
//        // Check if the object is new, meaning it has been created but not yet persisted to the database.
//        UUID id = object.getId();
//        return id != null && persistentNew.containsKey(id);
//    }
//
//    public void clearNew(@NotNull PersistenceCapable object) {
//        // Clear the new state of the object, meaning it has been persisted and no longer needs to be created.
//        // This could involve removing it from the persistentNew collection.
//        UUID id = object.getId();
//        if (id != null) {
//            persistentNew.remove(id);
//        }
//    }



//    private Map<UUID, PersistenceCapable> getPersistentNew() {
//        return persistentNew;
//    }

    public void registerMapper(Class theClass, Supplier mapperConstructor) {
        mapperRegistry.put(theClass, mapperConstructor);
    }

    public void registerType(Class theClass, Supplier constructor) {
        typeRegistry.put(theClass, constructor);



    }

//    public Map<UUID, PersistenceCapable> getPersistentAll() {
//        return persistentAll;
//    }



    public Object create(@NotNull Class interfaceClass) {

        var constructor = typeRegistry.get(interfaceClass);
        if (constructor == null) {
            throw new IllegalArgumentException("No constructor registered for class: " + interfaceClass.getName());
        }
        var instance = constructor.get();
        if (!(instance instanceof PersistenceCapable)) {
            throw new IllegalArgumentException("The class " + interfaceClass.getName() + " does not implement PersistenceCapable.");
        }


        persistentAll.put(((PersistenceCapable) instance).getId(), (PersistenceCapable) instance);
        persistentInterfaceTypes.put(((PersistenceCapable) instance).getId(), interfaceClass);
        ( (PostgresAbstractPersistenceCapable) instance).setPersistenceManager(this);



        persistentNew.put(((PersistenceCapable) instance).getId(), (PersistenceCapable) instance);
        // setNew((PersistenceCapable) instance);


        return instance;
    }


    /**
     * Note that this uses Bloch's (from 3rd ed.) Item 33: Consider typesafe heterogeneous containers.
     */
    public synchronized <T extends PersistenceCapable> T read(UUID id, Class interfaceClass, PostgresContext context) {

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
            var result = (T) mapper.read(id, context);

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
        UUID id = object.getId();
        persistentDirty.put(id, object);

    }

    /**
     * This method flushes all the new and dirty objects to the database.
     * It should be called at the end of a transaction or when you want to persist all changes made to the objects.
     * It will iterate over the persistentDirty collection and call the upsert method on each object.
     * On successful conclusion, both the persistentNew and persistentDirty collections will be cleared.
     * * Note that this method does not commit the transaction; it only writes them to the database but does not commit the transaction.
     * That will normally happen at the end of a @Transactional method in the service layer or wherever the transaction management is handled.
     *
     * @param context The database connection to use for flushing the changes.
     * @throws SQLException If there is an error during the database operation.
     */
    public void flush(PostgresContext context) throws SQLException {


        for (PersistenceCapable pc : persistentNew.values()) {
            System.out.println("********** + " + pc.getId() + " is in persistentNew, which is not yet flushed. It will be flushed now.");
        }

        for (PersistenceCapable pc : persistentDirty.values()) {
            System.out.println("********** + " + pc.getId() + " is in persistentDirty, which is not yet flushed. It will be flushed now.");
        }

        boolean noOverlap = Collections.disjoint(persistentNew.keySet(), persistentDirty.keySet());
        assert(!noOverlap) : "persistentNew and persistentDirty collections must not overlap. Please ensure that an object is either in persistentNew or persistentDirty, but not both.";


        List<UUID> newRemovalIDs = new ArrayList<>();
        // Iterate over the persistentNew collection and insert new objects
        for (PersistenceCapable newObject : persistentNew.values()) {

            PostgresAbstractPersistenceCapable pcap = (PostgresAbstractPersistenceCapable) newObject;



            var interfaceClass = persistentInterfaceTypes.get(newObject.getId());
            Supplier<?> mapperConstructor = mapperRegistry.get(interfaceClass);
            var mapper = (PostgresAbstractMapper) mapperConstructor.get();
            mapper.setPersistenceManager(this);


            mapper.upsert(newObject, context);
            // Remove the object from persistentNew after upserting

            newRemovalIDs.add(newObject.getId());

            System.out.println("Flushing new object: " + newObject.getId() + " of type " + interfaceClass.getName());


        }

        // Remove all new objects from persistentNew after flushing
        for (UUID id : newRemovalIDs) {
            System.out.println("Removing " + id + " from persistentNew after flushing.");
            // clearNew(persistentNew.get(id));
            persistentNew.remove(id);
        }
        newRemovalIDs.clear();

        List<UUID> dirtyRemovalIDs = new ArrayList<>();

        // Iterate over the persistentDirty collection and update existing objects
        for (PersistenceCapable dirtyObject : persistentDirty.values()) {

            var interfaceClass = persistentInterfaceTypes.get(dirtyObject.getId());
            Supplier<?> mapperConstructor = mapperRegistry.get(interfaceClass);
            var mapper = (PostgresAbstractMapper) mapperConstructor.get();
            mapper.setPersistenceManager(this);


            mapper.upsert(dirtyObject, context);
            // Remove the object from persistentNew after upserting
            // persistentNew.remove(newObject.getId());

            dirtyRemovalIDs.add(dirtyObject.getId());

            System.out.println("Flushing dirty object: " + dirtyObject.getId() + " of type " + interfaceClass.getName());


        }

        // Remove all new objects from persistentNew after flushing
        for (UUID id : dirtyRemovalIDs) {
            System.out.println("Removing " + id + " from persistentDirty after flushing.");
            // clearDirty(persistentDirty.get(id));
            persistentDirty.remove(id);
        }
        dirtyRemovalIDs.clear();

        assert persistentNew.isEmpty() : "persistentNew should be empty after flushing.";
        assert persistentDirty.isEmpty() : "persistentDirty should be empty after flushing.";
    }

}
// Other methods for update, delete, find, etc. can be added here.

