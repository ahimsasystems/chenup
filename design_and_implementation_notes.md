# Design and Implementation Notes

* Code generation
  * In the implementations of Entity interfaces, both setters and getters are generated, even if the interface only has a getter. This is because the implementation will generally at some point need to set the value, for example, when reading from the database.
  * The parameter of the setter matches the field name, as specified by the getter, and not using the parameter name in the interface.