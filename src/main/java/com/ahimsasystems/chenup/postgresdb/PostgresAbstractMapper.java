package com.ahimsasystems.chenup.postgresdb;

import com.ahimsasystems.chenup.core.Mapper;
import com.ahimsasystems.chenup.core.PersistenceCapable;
import com.ahimsasystems.chenup.core.PersistenceManager;
import com.ahimsasystems.chenup.core.exceptions.DeletedObjectAccessException;
import com.ahimsasystems.chenup.postgresdb.PostgresPersistenceManager;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

//    public Connection getConnection() {
//        return this.getPersistenceManager().getConnection();
//    }

    public PersistenceCapable read(UUID id, Connection conn) {




        String sql = getReadSql();  // Use the method to get the SQL query
        PostgresAbstractPersistenceCapable result;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {


            stmt.setObject(1, id);  // Works because PostgreSQL JDBC supports UUID


            try (ResultSet rs = stmt.executeQuery()) {

                // Has to pass a connection because getting a Relationship may require pulling in entities from the database.
                result = (PostgresAbstractPersistenceCapable) getRecord(rs, conn);  // Cast to PersistenceCapable, assuming getRecord returns a PersistenceCapable object





            }



        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // get desired metadata for this id
        // At this point we are only checking if the record is marked as deleted.

        boolean deleted = false;
        int version = -1;
        try (PreparedStatement stmt = conn.prepareStatement("SELECT DELETED, VERSION FROM THING WHERE id = ?")) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {

                deleted = rs.getBoolean("DELETED");
                version = rs.getInt("VERSION");

//                // TODO: should we stop here or go ahead and return the object with its metadata set including deleted?
//                if (deleted) {
//                    // If the record is marked as deleted, we should not return it
//                    throw new DeletedObjectAccessException(id);
//                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        result.getMetaData().setDeleted(deleted);
        result.getMetaData().setVersion(version);

        return result;

    }

    public void upsert(PersistenceCapable object, Connection conn) {
        String sql = upsertSql();  // Use the method to get the SQL query

        // Downcast to PostgresAbstractPersistenceCapable to access getMetaData()
        if (!(object instanceof PostgresAbstractPersistenceCapable)) {
            throw new IllegalArgumentException("Object must be an instance of PostgresAbstractPersistenceCapable");
        }
        PostgresAbstractPersistenceCapable pc = (PostgresAbstractPersistenceCapable) object;

        // TODO: There is logic in a databse trigger for this, but it did not appear to be doing what I expected. Need to investigate further.
        String sqlToInsertIntoThing = "INSERT INTO THING (id) VALUES (?) ON CONFLICT DO NOTHING"; // This is to ensure the THING table has the id before we insert into the specific table.



        try (PreparedStatement stmt = conn.prepareStatement(sqlToInsertIntoThing)) {
            stmt.setObject(1, object.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Check version number

        try (PreparedStatement stmt = conn.prepareStatement("SELECT VERSION FROM THING WHERE id = ?")) {
            stmt.setObject(1, object.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int currentVersion = rs.getInt("VERSION");
                if (pc.getMetaData().getVersion() != currentVersion) {
                    throw new RuntimeException("Version mismatch for object with id " + object.getId() + ". Current version: " + currentVersion + ", Object version: " + pc.getMetaData().getVersion() + ". Object class: " + object.getClass().getName());
                }
            } else {
                // If there is no record in THING, it means this is a new object. Set version to 0.
                pc.getMetaData().setVersion(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Increment version number
        pc.getMetaData().incrementVersion();
        // Save the new version number to the database
        try (PreparedStatement stmt = conn.prepareStatement("UPDATE THING SET VERSION = ? WHERE id = ?")) {
            stmt.setInt(1, pc.getMetaData().getVersion());
            stmt.setObject(2, object.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        try (PreparedStatement stmt = conn.prepareStatement(sql)) {


            setRecord(stmt, object);

            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Delete can be done in the AbstractMapper since deleting from the THING table should induce a cascade delete in the related tables, and there is no individual logic needed for delete like there is for upsert and read.
    */
    public void delete(PersistenceCapable object, Connection conn) {
        String sql = "DELETE FROM THING WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, object.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract String upsertSql();

    protected abstract void setRecord(PreparedStatement stmt, PersistenceCapable object) throws SQLException;

    protected abstract String getReadSql();

    protected abstract Object getRecord(ResultSet rs, Connection connection) throws SQLException;

    // These methods for parsing and unparsing PgRecords should be moved to PostgresAbstractMapper or a utility class.
    public static List<String> parsePgRecord(String record) {
        if (record == null || record.length() < 2 || record.charAt(0) != '(' || record.charAt(record.length() - 1) != ')') {
            throw new IllegalArgumentException("Invalid PostgreSQL composite format: " + record);
        }

        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        boolean inEscape = false;

        for (int i = 1; i < record.length() - 1; i++) {
            char c = record.charAt(i);

            if (inEscape) {
                sb.append(c);
                inEscape = false;
            } else if (c == '\\') {
                inEscape = true;
            } else if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(convertNull(sb.toString()));
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        result.add(convertNull(sb.toString()));  // final field
        return result;
    }

    private static String convertNull(String s) {
        return s.isEmpty() ? null : s;
    }

    // BEGIN WARNING: The following methods were generated by chatGPT and may not be fully tested or optimized.
    public static String unparsePgRecord(List<String> fields) {
        StringBuilder sb = new StringBuilder("(");

        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            if (field == null) {
                sb.append(""); // PostgreSQL treats empty string as NULL in record input
            } else if (needsQuoting(field)) {
                sb.append('"').append(escapeQuoted(field)).append('"');
            } else {
                sb.append(field);
            }

            if (i < fields.size() - 1) {
                sb.append(',');
            }
        }

        sb.append(')');
        return sb.toString();
    }

    private static boolean needsQuoting(String field) {
        // Per PostgreSQL docs, quote if it contains special chars
        return field.contains(",") || field.contains("\"") || field.contains("\\") || field.contains(" ");
    }

    private static String escapeQuoted(String field) {
        return field.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static PGobject createPgObject(String typeName, List<String> fields) throws SQLException {
        PGobject pg = new PGobject();
        pg.setType(typeName); // e.g., "my_udt_type"
        pg.setValue(unparsePgRecord(fields));
        return pg;
    }
    // END WARNING
}
