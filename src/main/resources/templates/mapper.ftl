package ${packageName};

import com.ahimsasystems.chenup.core.PersistenceCapable;
import com.ahimsasystems.chenup.postgresdb.PostgresAbstractMapper;
import com.ahimsasystems.chenup.postgresdb.PostgresContext;
import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.List;

<#-- Add necessary imports for custom field types -->
<#list fields as field>
    <#if field.import??>
        import ${field.import};
    </#if>
</#list>

public class ${entityName}Mapper extends PostgresAbstractMapper {

public ${entityName}Mapper() {}

@Override
protected String getReadSql() {
return "SELECT id, <#list fields as field>${field.sqlName}<#if !field?is_last>, </#if></#list> FROM ${tableName} WHERE id = ?";


}

    protected String upsertSql() {
        return "INSERT INTO ${tableName} (id, <#list fields as field>${field.sqlName}<#if !field?is_last>, </#if></#list>) " +
                "VALUES (?, <#list fields as field>?<#if !field?is_last>, </#if></#list>) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "<#list fields as field>${field.sqlName} = EXCLUDED.${field.sqlName}<#if !field?is_last>, </#if></#list>";
    }


@Override
protected Object getRecord(ResultSet rs, PostgresContext context) {
try {
rs.next();

${entityName}Impl obj = new ${entityName}Impl();
obj.setPersistenceManager(context.getPersistenceManager());

obj.setId(rs.getObject("id", UUID.class));

<#list fields as field>
<#if field.udt?? && field.udt>

           var pgobject = (PGobject) rs.getObject("${field.sqlName}");
           ${field.jdbcType} record = toRecord(pgobject.getValue(), ${field.jdbcType}.class);
           obj.set${field.name?cap_first}(record);





    <#else>
        obj.set${field.name?cap_first}(rs.getObject("${field.sqlName}", ${field.jdbcType}.class));
    </#if>
    </#list>

    return obj;

    } catch (SQLException e) {
    throw new RuntimeException(e);
    }
    }

    protected void setRecord(PreparedStatement ps, PersistenceCapable obj, PostgresContext context) {
        try {
            int index = 1;

            var typedObj = (${entityName}Impl) obj;

            ps.setObject(index++, obj.getId());

<#list fields as field>
    <#if field.udt?? && field.udt>
        {
            ${field.jdbcType} sub = typedObj.get${field.name?cap_first}();
            PGobject pgobject = new PGobject();
            pgobject.setType("${field.udtType}"); // e.g., "separate_name_type"
            pgobject.setValue(sub != null ? serializeToPostgresComposite(sub) : null);
            ps.setObject(index++, pgobject);
        }
    <#else>
        ps.setObject(index++, typedObj.get${field.name?cap_first}());
    </#if>
</#list>

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
