package com.ahimsasystems.chenup.processor;

import com.ahimsasystems.chenup.processor.model.EntityModel;
import com.ahimsasystems.chenup.processor.model.FieldModel;
import com.ahimsasystems.chenup.processor.model.RelationshipModel;
import com.ahimsasystems.chenup.processor.template.Template;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.
public class JavaCodeGenerator {

    public void generateEntityCode(Set<EntityModel> entityModels, ProcessingEnvironment processingEnv) throws IOException {


        for (EntityModel entityModel : entityModels) {

            generateEntityCode(entityModel, processingEnv);


        }
    }

    private void generateEntityCode(EntityModel entityModel, ProcessingEnvironment processingEnv) throws IOException {
        JavaFileObject file = processingEnv.getFiler().createSourceFile(entityModel.getPackageName() + "." + entityModel.getName() + "Impl");


        try (Writer writer = file.openWriter()) {
            writer.write(entityHeaderTemplate.render(Map.of(
                    "packageName", entityModel.getPackageName(),
                    "name", entityModel.getName()
            )));


            for (FieldModel field : entityModel.getFields().values()) {
                generateCode(field, writer);
            }

            writer.write(entityFooterTemplateString);
        }
    }


    private void generateCode(FieldModel fieldModel, Writer writer) throws IOException {


        String fieldSource = fieldTemplate.render(Map.of(
                "type", fieldModel.getType(),
                "name", fieldModel.getName(),
                "capName", capitalize(fieldModel.getName())
        ));



        writer.write(fieldSource);


    }

    public void generateRelationshipCode(Set<RelationshipModel> relationshipModels, ProcessingEnvironment processingEnv) throws IOException {

        System.err.println("*** Generating code for relationships: " + relationshipModels);

        for (RelationshipModel relationshipModel : relationshipModels) {

            generateRelationshipCode(relationshipModel, processingEnv);


        }
    }

    private void generateRelationshipCode(RelationshipModel relationshipModel, ProcessingEnvironment processingEnv) throws IOException {
        JavaFileObject file = processingEnv.getFiler().createSourceFile(relationshipModel.getPackageName() + "." + relationshipModel.getName() + "Impl");


        try (Writer writer = file.openWriter()) {
            writer.write(entityHeaderTemplate.render(Map.of(
                    "packageName", relationshipModel.getPackageName(),
                    "name", relationshipModel.getName()
            )));


            for (FieldModel field : relationshipModel.getFields().values()) {
                generateCode(field, writer);
            }

            for (EntityModel entity : relationshipModel.getEntities().values()) {
                System.err.println("*** Generating code for entity field: " + entity.getName());
                writer.write(fieldTemplate.render(Map.of(
                        "type", entity.getName() + "Impl",
                        "name", decapitalize(entity.getName()),
                        "capName", capitalize(decapitalize(entity.getName()))
                )));
            }

            writer.write(entityFooterTemplateString);
        }
    }

    public static String decapitalize(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private Template fieldTemplate;
    private Template entityHeaderTemplate;
    private Template entityFooterTemplate;
    private Types typeUtils;

    String fieldTemplateString = """
                
                private $(type) $(name);
                
                public $(type) get$(capName)() {
                    return $(name);
                }
                
                public void set$(capName)($(type) $(name)) {
                    this.$(name) = $(name);
                }
                """;


    String entityHeaderTemplateString = """
                package $(packageName);
                
                public class $(name)Impl extends com.ahimsasystems.chenup.core.AbstractPersistenceCapable implements $(name)  {
                """;

    String entityFooterTemplateString = """
                }
                """;

    {
        fieldTemplate = new Template();
        fieldTemplate.compile(fieldTemplateString);
        entityHeaderTemplate = new Template();
        entityHeaderTemplate.compile(entityHeaderTemplateString);
        entityFooterTemplate = new Template();
        entityFooterTemplate.compile(entityFooterTemplateString);

    }

    public void generatePersistenceInitializer(Set<EntityModel> entityModels, Set<RelationshipModel> relationshipModels,ProcessingEnvironment processingEnv) throws IOException {

        // Hard coded for now, needs to be templatized.


        JavaFileObject file = processingEnv.getFiler().createSourceFile("com.example.PersistenceInitializer");
        try (Writer writer = file.openWriter()) {
            writer.write("""
                  package com.example;

// © 2025 Stephen W. Strom
// Licensed under the MIT License. See LICENSE file in the project root for details.

import com.ahimsasystems.chenup.core.PersistenceManager;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.function.Supplier;

                // This class will be generated so that its dependence on generated code wiill not cause compile errors.
@ApplicationScoped
public class PersistenceInitializer implements com.ahimsasystems.chenup.core.PersistenceInitializer {
    public void registerAll(PersistenceManager pm) {

            // Register all classes that need to be persisted.
            // For example:
            pm.registerType(Person.class, PersonImpl::new);
            pm.registerMapper(Person.class, PersonMapper::new);
            
            pm.registerType(Organization.class, OrganizationImpl::new);
            pm.registerMapper(Organization.class, OrganizationMapper::new);
            
            pm.registerType(Employment.class, EmploymentImpl::new);
            pm.registerMapper(Employment.class, EmploymentMapper::new);


            // Add more registrations as needed.
            // pm.register(AnotherClass.class, AnotherClass::new);

   // etc.

    }
}







                            """
            );
    }
    }
}
