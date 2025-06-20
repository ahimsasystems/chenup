package ${packageName};

public class ${className} extends com.ahimsasystems.chenup.postgresdb.PostgresAbstractPersistenceCapable implements ${interfaceName} {

<#list fields as field>
    private ${field.type} ${field.name};

    synchronized public ${field.type} get${field.name?cap_first}() {
    return ${field.name};
    }

    synchronized public void set${field.name?cap_first}(${field.type} ${field.name}) {
    this.${field.name} = ${field.name};
    getPersistenceManager().dirty(this);
    }

        synchronized public void load${field.name?cap_first}(${field.type} ${field.name}) {
            this.${field.name} = ${field.name};
        }
</#list>

}
