package com.ahimsasystems.chenup.postgresdb;

import com.ahimsasystems.chenup.core.Mapper;
import com.ahimsasystems.chenup.core.PersistenceCapable;
import com.ahimsasystems.chenup.core.PersistenceManager;
import com.ahimsasystems.chenup.core.exceptions.DeletedObjectAccessException;
import com.ahimsasystems.chenup.postgresdb.PostgresPersistenceManager;

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

    public PersistenceCapable read(UUID id) {




        String sql = getReadSql();  // Use the method to get the SQL query
        PersistenceCapable result;

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {


            stmt.setObject(1, id);  // Works because PostgreSQL JDBC supports UUID


            try (ResultSet rs = stmt.executeQuery()) {

                result = (PersistenceCapable) getRecord(rs);





            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // get desired metadata for this id
        // At this point we are only checking if the record is marked as deleted.
        try (PreparedStatement stmt = getConnection().prepareStatement("SELECT DELETED FROM THING WHERE id = ?")) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                boolean deleted = rs.getBoolean("DELETED");
                if (deleted) {
                    // If the record is marked as deleted, we should not return it
                    throw new DeletedObjectAccessException(id);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;

    }

    public void upsert(PersistenceCapable object) {
        String sql = upsertSql();  // Use the method to get the SQL query
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {

            setRecord(stmt, object);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Delete can be done in the AbstractMapper since deleting from the THING table should enduce a cascade delete in the related tables, and there is no individual logic needed for delete like there is for upsert and read.
    */
    public void delete(PersistenceCapable object) {
        String sql = "DELETE FROM THING WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setObject(1, object.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String upsertSql();

    protected abstract void setRecord(PreparedStatement stmt, PersistenceCapable object);

    protected abstract String getReadSql();

    protected abstract Object getRecord(ResultSet rs);


}
