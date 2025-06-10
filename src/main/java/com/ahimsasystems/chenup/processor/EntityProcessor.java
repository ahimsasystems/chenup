package com.ahimsasystems.chenup.processor;

import com.ahimsasystems.chenup.annotations.Entity;
import com.ahimsasystems.chenup.annotations.Relationship;
import com.ahimsasystems.chenup.processor.model.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ahimsasystems.chenup.processor.JavaCodeGenerator.decapitalize;


@SupportedAnnotationTypes({"com.ahimsasystems.chenup.annotations.Entity", "com.ahimsasystems.chenup.annotations.Relationship"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class EntityProcessor extends AbstractProcessor {

    private Elements elementUtils;

    private final Set<EntityModel> entityModels = new java.util.HashSet<>();

    // Index for quick lookup by name. will be populated after processing all entities.
    private Map<String, EntityModel> entityTypeNameIndex = entityModels.stream()
            .collect(Collectors.toMap(EntityModel::getName, Function.identity()));
    private final Set<RelationshipModel> relationshipModels = new java.util.HashSet<>();
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
                // generateCode(entityModels, processingEnv);
                JavaCodeGenerator javaCodeGenerator = new JavaCodeGenerator();
                javaCodeGenerator.generateEntityCode(entityModels, processingEnv);
                javaCodeGenerator.generateRelationshipCode(relationshipModels, processingEnv);

                javaCodeGenerator.generatePersistenceInitializer(entityModels, relationshipModels, processingEnv);
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


        System.err.println("Entity Type Name Index: " + entityTypeNameIndex);

        for (Element e : roundEnv.getElementsAnnotatedWith(Relationship.class)) {
            Class x = Relationship.class;
            buildModel(e, ModelType.RELATIONSHIP);
        }
        return true;
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
                    System.err.println("*** " + returnType + " ***");
                    if (entityTypeNameIndex.containsKey(returnType)) {
                        // If the field is an entity, we can skip it for now.
                        System.err.println("Found an entity: " + returnType);
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
                    System.err.println("*** " + returnType + " ***");
                    if (entityTypeNameIndex.containsKey(paramType)) {
                        EntityModel entityModel = entityTypeNameIndex.get(paramType);
                        // If the field is an entity, we can skip it for now.
                        System.err.println("Found an entity: " + paramType);
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
                    System.err.println("Processing Relationship: " + interfaceName + " in package: " + packageName);
                    var relationshipModel = new RelationshipModel(
                            packageName,
                            interfaceName,
                            fieldMap,
                            relationshipEntities
                    );
                    relationshipModels.add(relationshipModel);

                    System.err.println("Relationship Model: " + relationshipModel);
                }
            else {
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



}
