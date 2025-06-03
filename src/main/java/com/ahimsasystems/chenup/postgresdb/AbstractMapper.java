package com.ahimsasystems.chenup.postgresdb;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.

/* This is an abstract class that provides a template for mapping database records to Java objects.
 * It uses a DataSource to retrieve connections and execute SQL queries.
 * The `retrieve` method fetches a record by its UUID and maps the result set to a newly created Java object.
 * Subclasses must implement the `mapFields` method for the specific type they are mapping.
 * This design follows the Template Method pattern, allowing subclasses to define specific mapping logic while reusing the common retrieval logic.
 * This is also implementing a good deal of the persistenceManager functionality, so it may a good place to start for that. The most important thing to note for that, though, is that mapping code is generated, so there has to be a specific mapper for each class that is persisted.
 * Is there a possible way to register the mappers with the persistence manager? Or should this remain part of the generated code? In any case, at this point the mappers must talk to the persistence manager in order to register these objects to be managed.
 */
public abstract class AbstractMapper {
    public PostgresPersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    private PostgresPersistenceManager persistenceManager;

    public void setPersistenceManager(PostgresPersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }



    public Object retrieve(UUID id) {
        if (getPersistenceManager().getPersistentAll().containsKey(id)) {
            return getPersistenceManager().getPersistentAll().get(id);
        }
        try {
            DataSource dataSource = persistenceManager.getDataSource();
            var connection = dataSource.getConnection(); // Example usage of aDataSource
            String sql = """
                     SELECT * FROM person WHERE id = ?
                    """;


            PreparedStatement stmt = connection.prepareStatement(sql);

            stmt.setObject(1, id);  // Works because PostgreSQL JDBC supports UUID


            ResultSet rs = stmt.executeQuery();
            rs.next();  // Assuming there is one and only one result for the given ID
            Object object = mapFields(rs);


            rs.close();
            stmt.close();
            connection.close();

            return object;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ToDo: I have a feeling this might be replacable by an injected function. But for now, this is the standard GoF template method pattern.
    protected abstract Object mapFields(ResultSet rs);
}
