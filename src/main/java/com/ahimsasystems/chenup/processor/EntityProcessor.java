package com.ahimsasystems.chenup.processor;

import com.ahimsasystems.chenup.annotations.Entity;
import com.ahimsasystems.chenup.annotations.Relationship;
import com.ahimsasystems.chenup.processor.model.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ahimsasystems.chenup.processor.JavaCodeGenerator.decapitalize;


@SupportedAnnotationTypes({"com.ahimsasystems.chenup.annotations.Entity", "com.ahimsasystems.chenup.annotations.Relationship"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class EntityProcessor extends AbstractProcessor {

    private final Set<EntityModel> entityModels = new java.util.HashSet<>();
    private final Set<RelationshipModel> relationshipModels = new java.util.HashSet<>();
    private Elements elementUtils;
    // Index for quick lookup by name. will be populated after processing all entities.
    private Map<String, EntityModel> entityTypeNameIndex = entityModels.stream()
            .collect(Collectors.toMap(EntityModel::getName, Function.identity()));
    private Types typeUtils;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();


    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (roundEnv.processingOver()) {
            try {

                Template template = generateImpls(entityModels);

                Template template2 = generateImpls(relationshipModels);

                generateMappers(entityModels);


//                for (RelationshipModel relationshipModel : relationshipModels) {
//                    System.out.println("Generating code for relationship: " + relationshipModel.getName());
//
//
//                    Map<String, Object> model = new HashMap<>();
//                    model.put("packageName", relationshipModel.getPackageName());
//                    model.put("className", relationshipModel.getName() + "Impl");
//
//                    model.put("interfaceName", relationshipModel.getName());
//
//
//                    List<Map<String, String>> fields = new ArrayList<>();
//
//
//                    for (Map.Entry<String, FieldModel> entry : relationshipModel.getFields().entrySet()) {
//                        FieldModel field = entry.getValue();
//
//                        // Skip the fields with built-in implementations.
//                        switch (field.getName()) {
//                            case "id", "currentTime", "clock" -> {
//                                continue;
//                            }
//                        }
//
//                        // Skip fields that have a default reader or writer.
//                        // ToDo: Convince yourself that this is correct.
//                        // I suspect it is, if either or both are overriden, then this is probably a computed field of some kind, and we don't want to generate the default code for it.
//                        if (field.getHasDefaultReader() || field.getHasDefaultWriter()) {
//                            continue;
//                        }
//
//                        fields.add(Map.of("name", field.getName(), "type", field.getType()));
//                    }
//
////                fields.add(Map.of("name", "name", "type", "java.lang.String"));
////                fields.add(Map.of("name", "birthDate", "type", "java.time.LocalDate"));
//
//
//                    model.put("fields", fields);
//
//
//                    JavaFileObject file = processingEnv.getFiler()
//                            .createSourceFile(relationshipModel.getPackageName() + "." + relationshipModel.getName() + "Impl");
//
//                    try (Writer writer = file.openWriter()) {
//                        template.process(model, writer);
//                    }
////                    template.process(model, processingEnv.getFiler().createSourceFile(
////                            entityModel.getPackageName() + "." + entityModel.getName() + "Impl"
////                    ).openWriter());
//
//
//                    System.out.println("template = " + template.toString());
//                }
//                // END: FreeMarker code generation

                // BEGIN: my custom code generator

//                // generateCode(entityModels, processingEnv);
                JavaCodeGenerator javaCodeGenerator = new JavaCodeGenerator();
//                javaCodeGenerator.generateEntityCode(entityModels, processingEnv);
//                javaCodeGenerator.generateRelationshipCode(relationshipModels, processingEnv);

                javaCodeGenerator.generatePersistenceInitializer(entityModels, relationshipModels, processingEnv);

                Jsonb jsonb = JsonbBuilder.create();

                jsonb.toJson(entityModels, processingEnv.getFiler().createResource(
                        StandardLocation.CLASS_OUTPUT,
                        "",
                        "entityModels.json"
                ).openWriter());

                jsonb.toJson(relationshipModels, processingEnv.getFiler().createResource(
                        StandardLocation.CLASS_OUTPUT,
                        "",
                        "relationshipModels.json"
                ).openWriter());

                // END: my custom code generator

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }


        for (Element e : roundEnv.getElementsAnnotatedWith(Entity.class)) {
            Class x = Entity.class;

            buildModel(e, ModelType.ENTITY);


        }

        entityTypeNameIndex = entityModels.stream().collect(Collectors.toMap(EntityModel::getFullName, Function.identity()));


        for (Element e : roundEnv.getElementsAnnotatedWith(Relationship.class)) {
            Class x = Relationship.class;
            buildModel(e, ModelType.RELATIONSHIP);
        }
        return true;
    }

    private <T extends MutableElementModel> Template generateMappers(Set<T> models) throws IOException, TemplateException {

        // Generate mappers


        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(getClass(), "/templates"); // or getClassLoader()
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);


        Template mapperTemplate = cfg.getTemplate("mapper.ftl");


        for (MutableElementModel mutableElementModel : models) {
            System.out.println("Generating mapper for: " + mutableElementModel.getName());


            Map<String, Object> mapperModel = new HashMap<>();
            mapperModel.put("packageName", mutableElementModel.getPackageName());
            mapperModel.put("entityName", mutableElementModel.getName());
            mapperModel.put("tableName", mutableElementModel.getName().toLowerCase(Locale.ROOT));

            List<Map<String, Object>> fieldModels = new ArrayList<>();
            for (Map.Entry<String, FieldModel> entry : mutableElementModel.getFields().entrySet()) {
                // Skip the fields with built-in implementations.
                FieldModel field = entry.getValue();
                switch (field.getName()) {
                    case "id", "currentTime", "clock" -> {
                        continue;
                    }
                }

                // Skip fields that have a default reader or writer.
                // ToDo: Convince yourself that this is correct.
                // I suspect it is, if either or both are overriden, then this is probably a computed field of some kind, and we don't want to generate the default code for it.
                if (field.getHasDefaultReader() || field.getHasDefaultWriter()) {
                    continue;
                }

                // For the moment, skip if a record / UDT type.
//                if (field.getType().equals("com.example.Name")) {
//                    continue;
//                }


                Map<String, Object> fieldModel = new HashMap<>();
                fieldModel.put("name", field.getName());
                fieldModel.put("sqlName", toSnakeCase(field.getName()));
                fieldModel.put("jdbcType", field.getType());
                if (field.getType().equals("com.example.PersonName")) {
                    fieldModel.put("udt", true);
                    fieldModel.put("udtType", "person_name");
                } else {
                    fieldModel.put("udt", false);
                }
                fieldModels.add(fieldModel);
            }


            mapperModel.put("fields", fieldModels);

            JavaFileObject file = processingEnv.getFiler()
                    .createSourceFile(mutableElementModel.getPackageName() + "." + mutableElementModel.getName() + "Mapper");

            try (Writer writer = file.openWriter()) {
                mapperTemplate.process(mapperModel, writer);
            }
        }

        return mapperTemplate;
    }

    private <T extends MutableElementModel> Template generateImpls(Set<T> models) throws IOException, TemplateException {


        // private Template getTemplate(Set<EntityModel> entityModels) throws IOException, TemplateException {
        // BEGIN: FreeMarker code generation
        // Freemarker initialization
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(getClass(), "/templates"); // or getClassLoader()
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);


        Template template = cfg.getTemplate("impl-entity.ftl");

        // Loop over all models and generate code for each one.
        // Currently doing the same code generation for both EntityModel and RelationshipModel.
        for (MutableElementModel mutableElementModel : models) {
            System.out.println("Generating code for entity: " + mutableElementModel.getName());


            Map<String, Object> model = new HashMap<>();
            model.put("packageName", mutableElementModel.getPackageName());
            model.put("className", mutableElementModel.getName() + "Impl");

            model.put("interfaceName", mutableElementModel.getName());


            List<Map<String, String>> fields = new ArrayList<>();


            for (Map.Entry<String, FieldModel> entry : mutableElementModel.getFields().entrySet()) {
                FieldModel field = entry.getValue();

                // Skip the fields with built-in implementations.
                switch (field.getName()) {
                    case "id", "currentTime", "clock" -> {
                        continue;
                    }
                }

                // Skip fields that have a default reader or writer.
                // ToDo: Convince yourself that this is correct.
                // I suspect it is, if either or both are overriden, then this is probably a computed field of some kind, and we don't want to generate the default code for it.
                if (field.getHasDefaultReader() || field.getHasDefaultWriter()) {
                    continue;
                }

                fields.add(Map.of("name", field.getName(), "type", field.getType()));
            }

//                fields.add(Map.of("name", "name", "type", "java.lang.String"));
//                fields.add(Map.of("name", "birthDate", "type", "java.time.LocalDate"));


            model.put("fields", fields);


            JavaFileObject file = processingEnv.getFiler()
                    .createSourceFile(mutableElementModel.getPackageName() + "." + mutableElementModel.getName() + "Impl");

            try (Writer writer = file.openWriter()) {
                template.process(model, writer);
            }
//                    template.process(model, processingEnv.getFiler().createSourceFile(
//                            entityModel.getPackageName() + "." + entityModel.getName() + "Impl"
//                    ).openWriter());


            System.out.println("template = " + template.toString());
        }
        return template;
    }

    private void buildModel(Element e, ModelType modelType) {
        if (e.getKind() != ElementKind.INTERFACE) {
            return;
        }


        TypeElement interfaceElement = (TypeElement) e;
        String packageName = elementUtils.getPackageOf(interfaceElement).getQualifiedName().toString();
        String interfaceName = interfaceElement.getSimpleName().toString();
        String className = interfaceName + "Impl";


        try {


            var fieldMap = new java.util.HashMap<String, FieldModel>();
            var relationshipEntities = new java.util.HashMap<String, EntityModel>();

            Set<ExecutableElement> allMethods = new LinkedHashSet<>();
            collectAllInterfaceMethods(interfaceElement, elementUtils, typeUtils, allMethods);


            for (ExecutableElement method : allMethods) {


                String name = method.getSimpleName().toString();
                String returnType = method.getReturnType().toString();

                if (name.startsWith("get") && method.getParameters().isEmpty()) {

                    String memberName = decapitalize(name.substring(3));

                    // Is this an entity?
                    if (entityTypeNameIndex.containsKey(returnType)) {
                        // If the field is an entity, we can skip it for now.
                        continue;
                    }


                    var access = fieldMap.containsKey(memberName) ? Access.READ_WRITE : Access.READ_ONLY;

                    var hasDefaultWriter = fieldMap.containsKey(memberName) && fieldMap.get(memberName).getHasDefaultWriter();
                    boolean hasDefaultReader = method.getModifiers().contains(Modifier.DEFAULT);

                    var fieldModel = new FieldModel(memberName, returnType, access, hasDefaultReader, hasDefaultWriter);
                    fieldMap.put(memberName, fieldModel);


                } else if (name.startsWith("set") && method.getParameters().size() == 1) {
                    String field = decapitalize(name.substring(3));
                    String paramType = method.getParameters().get(0).asType().toString();
                    String param = method.getParameters().get(0).getSimpleName().toString();

                    // Is this an entity?
                    if (entityTypeNameIndex.containsKey(paramType)) {
                        EntityModel entityModel = entityTypeNameIndex.get(paramType);
                        // If the field is an entity, we can skip it for now.
                        relationshipEntities.put(field, entityModel);
                        ;
                    }


                    var access = fieldMap.containsKey(field) ? Access.READ_WRITE : Access.WRITE_ONLY;

                    var hasDefaultReader = fieldMap.containsKey(field) && fieldMap.get(field).getHasDefaultReader();
                    boolean hasDefaultWriter = method.getModifiers().contains(Modifier.DEFAULT);

                    var fieldModel = new FieldModel(field, paramType, access, hasDefaultReader, hasDefaultWriter);

                    fieldMap.put(field, fieldModel);
                }
            }


            if (modelType == ModelType.ENTITY) {
                var entityModel = new EntityModel(packageName, interfaceName, fieldMap);
                entityModels.add(entityModel);

            } else if (modelType == ModelType.RELATIONSHIP) {
                var relationshipModel = new RelationshipModel(
                        packageName,
                        interfaceName,
                        fieldMap,
                        relationshipEntities
                );
                relationshipModels.add(relationshipModel);

            } else {
                System.err.println("Unknown annotation type: " + modelType);
            }


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void collectAllInterfaceMethods(TypeElement interfaceElement, Elements elementUtils, Types typeUtils, Set<ExecutableElement> methods) {
        // Collect declared methods
        for (Element enclosed : interfaceElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.METHOD && enclosed instanceof ExecutableElement method) {
                methods.add(method);
            }
        }

        // Walk superinterfaces
        for (TypeMirror iface : interfaceElement.getInterfaces()) {
            Element superElement = typeUtils.asElement(iface);
            if (superElement instanceof TypeElement superTypeElement) {
                collectAllInterfaceMethods(superTypeElement, elementUtils, typeUtils, methods);
            }
        }
    }

    public static String toSnakeCase(String input) {
        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2")
                .replaceAll("([A-Z])([A-Z][a-z])", "$1_$2")
                .toLowerCase();
    }



}
