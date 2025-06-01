package com.ahimsasystems.chenup.processor;

import com.ahimsasystems.chenup.annotations.Entity;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.ahimsasystems.chenup.processor.model.Access;
import com.ahimsasystems.chenup.processor.model.EntityModel;
import com.ahimsasystems.chenup.processor.model.FieldModel;
import com.ahimsasystems.chenup.processor.template.Template;


@SupportedAnnotationTypes("com.ahimsasystems.chenup.annotations.Entity")
@SupportedSourceVersion(SourceVersion.RELEASE_24)
public class EntityProcessor extends AbstractProcessor {

    private Elements elementUtils;

    private Set<EntityModel> entityModels = new java.util.HashSet<>();

    private Template fieldTemplate;
    private Template entityHeaderTemplate;
    private Template entityFooterTemplate;
    private Types typeUtils;



    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();


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
            
            public class $(name)Impl implements $(name), com.ahimsasystems.chenup.core.PersistenceCapable {
            """;

        String entityFooterTemplateString = """
            }
            """;

        fieldTemplate = new Template();
        fieldTemplate.compile(fieldTemplateString);
        entityHeaderTemplate = new Template();
        entityHeaderTemplate.compile(entityHeaderTemplateString);
        entityFooterTemplate = new Template();
        entityFooterTemplate.compile(entityFooterTemplateString);

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (roundEnv.processingOver()) {
            try {
                generateCode(entityModels);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }

        for (Element e : roundEnv.getElementsAnnotatedWith(Entity.class)) {
            if (e.getKind() != ElementKind.INTERFACE) {
                continue;
            }

            TypeElement interfaceElement = (TypeElement) e;
            String packageName = elementUtils.getPackageOf(interfaceElement).getQualifiedName().toString();
            String interfaceName = interfaceElement.getSimpleName().toString();
            String className = interfaceName + "Impl";


            try {
                // JavaFileObject file = processingEnv.getFiler().createSourceFile(packageName + "." + className + "Old") ;
                // try (Writer writer = file.openWriter()) {
                //    writer.write("package " + packageName + "; ");
                //    writer.write("public class " + className + " implements " + interfaceName + " { ");

                    var fieldMap = new java.util.HashMap<String, FieldModel>();

                Set<ExecutableElement> allMethods = new LinkedHashSet<>();
                collectAllInterfaceMethods(interfaceElement, elementUtils, typeUtils, allMethods);


                // for (Element enclosed : interfaceElement.getEnclosedElements()) {
                for (ExecutableElement method : allMethods) {

                    // if (enclosed.getKind() != ElementKind.METHOD) continue;



                        // ExecutableElement method = (ExecutableElement) enclosed;
                            String name = method.getSimpleName().toString();
                            String returnType = method.getReturnType().toString();

                            if (name.startsWith("get") && method.getParameters().isEmpty()) {
                                String field = decapitalize(name.substring(3));
                        //        writer.write("    private " + returnType + " " + field + "; ");
                        //        writer.write("    public " + returnType + " " + name + "() { return " + field + "; } ");

                                var access = fieldMap.containsKey(field) ? Access.READ_WRITE : Access.READ_ONLY;

                                var hasDefaultWriter = fieldMap.containsKey(field) && fieldMap.get(field).getHasDefaultWriter();
                                boolean hasDefaultReader = method.getModifiers().contains(Modifier.DEFAULT);

                                var fieldModel = new FieldModel(field, returnType, access, hasDefaultReader, hasDefaultWriter);
                                fieldMap.put(field, fieldModel);



                            } else if (name.startsWith("set") && method.getParameters().size() == 1) {
                                String field = decapitalize(name.substring(3));
                                String paramType = method.getParameters().get(0).asType().toString();
                                String param = method.getParameters().get(0).getSimpleName().toString();
                        //        writer.write("    public void " + name + "(" + paramType + " " + param + ") { this." + field + " = " + param + "; } ");

                                var access = fieldMap.containsKey(field) ? Access.READ_WRITE : Access.WRITE_ONLY;

                                var hasDefaultReader = fieldMap.containsKey(field) && fieldMap.get(field).getHasDefaultReader();
                                boolean hasDefaultWriter = method.getModifiers().contains(Modifier.DEFAULT);

                                var fieldModel = new FieldModel(field, paramType, access, hasDefaultReader, hasDefaultWriter);

                                fieldMap.put(field, fieldModel);
                            }
                    }

                   // writer.write("} ");

                    var entityModel = new EntityModel(packageName, interfaceName, fieldMap);
                    entityModels.add(entityModel);

                    System.err.println(entityModel);



            //    }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    private void generateCode(Set<EntityModel> entityModels) throws IOException {



        for (EntityModel entityModel : entityModels) {

            generateCode(entityModel);


        }
    }

    private void generateCode(EntityModel entityModel) throws IOException {
        System.err.println("Generating code for " + entityModel);
        JavaFileObject file = processingEnv.getFiler().createSourceFile(entityModel.getPackageName() + "." + entityModel.getName() + "Impl");

        entityHeaderTemplate.render(Map.of(
                "packageName", entityModel.getPackageName(),
                "name", entityModel.getName(),
                "interfaceName", entityModel.getName()
        ));

        try (Writer writer = file.openWriter()) {
            writer.write(entityHeaderTemplate.render(Map.of(
                    "packageName", entityModel.getPackageName(),
                    "name", entityModel.getName()
            )));

//            writer.write("package " + entityModel.packageName() + "; ");
//            writer.write("public class " + entityModel.getName() + "Impl" + " { ");

            for (FieldModel field : entityModel.getFields().values()) {
                generateCode(field, writer);
            }

            writer.write("} ");
        }
    }


    private void generateCode(FieldModel fieldModel, Writer writer) throws IOException {


        String fieldSource = fieldTemplate.render(Map.of(
                "type", fieldModel.getType(),
                "name", fieldModel.getName(),
                "capName", capitalize(fieldModel.getName())
        ));


        System.err.println(fieldSource);

        writer.write(fieldSource);

//        writer.write("    private " + fieldModel.type() + " " + fieldModel.getName() + "; ");
//        if (fieldModel.access() == Access.READ_WRITE || fieldModel.access() == Access.READ_ONLY) {
//            writer.write("    public " + fieldModel.type() + " get" + capitalize(fieldModel.getName()) + "() { return " + fieldModel.getName() + "; } ");
//        }
//
//        if (fieldModel.access() == Access.READ_WRITE || fieldModel.access() == Access.WRITE_ONLY) {
//            writer.write("    public void set" + capitalize(fieldModel.getName()) + "(" + fieldModel.type() + " " + fieldModel.getName() + ") { this." + fieldModel.getName() + " = " + fieldModel.getName() + "; } ");
//        }
    }

    private String decapitalize(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
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
