# Design and Implementation Notes

* Code generation
  * In the implementations of Entity interfaces, both setters and getters are generated, even if the interface only has a getter. This is because the implementation will generally at some point need to set the value, for example, when reading from the database.
  * The parameter of the setter matches the field name, as specified by the getter, and not using the parameter name in the interface.

* Relationships

* PersistenceManager
* PersistenceCapable
  * The PersistenceCapable interface is used to access the persistence machinery, such as the entity ID and the entity state (dirty, etc.). This allows application code to access these details without needing to recast the object to a specific implementation class.
  * The PersistenceManager is responsible for managing the persistence of entities and relationships, including creating, updating, and deleting them. It also provides methods for querying the database and retrieving entities and relationships.
  * The PersistenceManager is not intended to be used directly by application code, but rather through the PersistenceCapable interface.
