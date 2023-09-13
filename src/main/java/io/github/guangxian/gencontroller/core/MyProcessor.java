package io.github.guangxian.gencontroller.core;

import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes(value = {"io.github.guangxian.gencontroller.core.GenController", "io.github.guangxian.gencontroller.core.GenControllerConfig"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class MyProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private Elements elementUtils;
    private TypeParameterElement v2;
    private final static String[]  IGNORE_METHODS = {"<init>", "main"};

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        elementUtils = processingEnv.getElementUtils();
    }


    public int r2(int num1, int num2)
    {
        return (int) (num1 + Math.random() * (num2 - num1 + 1));
    }

    public String r(String...args) { return r2(1000000,9999999) + Arrays.toString(args); }

    public String firstUpper(String str) {
        if (!Character.isUpperCase(str.charAt(0))) {
            char[] cs = str.toCharArray();
            cs[0] -= 32;
            return String.valueOf(cs);
        }
        return str;
    }

    public String firstLower(String str) {
        if (!Character.isLowerCase(str.charAt(0))) {
            char[] cs=str.toCharArray();
            cs[0]+=32;
            return String.valueOf(cs);
        }
        return str;
    }

    public static String camelToUnderline(String str) {
        if (str == null || "".equals(str.trim())) {
            return "";
        }
        int len = str.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        boolean enable = true;
        Config config = new Config();


        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(GenControllerConfig.class);
        if (elements.size() < 1) {
            return false;
        }
        for (Element element : elements) {
            config.setEnable(element.getAnnotation(GenControllerConfig.class).enable());
            config.setPackagePath(element.getAnnotation(GenControllerConfig.class).packagePath());
            config.setResponseType(element.getAnnotation(GenControllerConfig.class).responseType());
            config.setReturnExpression(element.getAnnotation(GenControllerConfig.class).returnExpression());
            enable = config.getEnable();
            messager.printMessage(Diagnostic.Kind.NOTE, r("GenControllerConfig.packagePath = ") + config.getPackagePath());
        }

        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(GenController.class);
        messager.printMessage(Diagnostic.Kind.NOTE, r("GenController.class.size = ") + elementsAnnotatedWith.size());
        // get YML
//        YamlPropertiesFactoryBean yamlProFb = new YamlPropertiesFactoryBean();
//        yamlProFb.setResources(new ClassPathResource("application.yml"));
//        Properties properties = yamlProFb.getObject();

//        boolean enable = properties.get("gen-controller.enable") != null ? Boolean.parseBoolean(properties.get("gen-controller.enable").toString()) : true;
        if (!elementsAnnotatedWith.isEmpty() && enable) {
            Map<String, TypeSpec.Builder> typeSpecBuilders = new HashMap<>();
            for (Element element : elementsAnnotatedWith) {
                TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(MyUtil.firstUpper(element.getAnnotation(GenController.class).name()) + "Controller");

                List<MethodSpec> methodSpecs = buildMethods(config, element);// new ArrayList<>() 为当前生成的methods

                List<FieldSpec> fieldSpecs = new ArrayList<>();
                fieldSpecs.add(FieldSpec
                        .builder(ClassName.bestGuess(element.toString()), firstLower(element.getSimpleName().toString()))
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build());

                // declare annotations
                List<AnnotationSpec> annotationSpecs = new ArrayList<>();
                annotationSpecs.add(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")).build());

                // if same controller name, then add fields and methods
                if (typeSpecBuilders.containsKey(element.getAnnotation(GenController.class).name())) {
                    TypeSpec.Builder builder = typeSpecBuilders.get(element.getAnnotation(GenController.class).name());
                    fieldSpecs.addAll(builder.fieldSpecs);
                    methodSpecs.addAll(builder.methodSpecs);
                    annotationSpecs.addAll(builder.annotations);
                }

                // reset construction by fields
                methodSpecs.removeIf(MethodSpec::isConstructor);
                MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
                for (FieldSpec fieldSpec : fieldSpecs) {
                    constructorBuilder.addModifiers(Modifier.PUBLIC);
                    constructorBuilder.addParameter(ParameterSpec.builder(fieldSpec.type, firstLower(fieldSpec.name)).build());
                    constructorBuilder.addStatement("this.$N = $N", firstLower(fieldSpec.name), firstLower(fieldSpec.name));
                }
                methodSpecs.add(constructorBuilder.build());

                // add annotation
                if (!"".equals(element.getAnnotation(GenController.class).springDocTagName())) {
                    annotationSpecs.add(AnnotationSpec.builder(ClassName.bestGuess("io.swagger.v3.oas.annotations.tags.Tag"))
                            .addMember("name", "$S", element.getAnnotation(GenController.class).springDocTagName())
                            .addMember("description", "$S", element.getAnnotation(GenController.class).springDocTagDescription())
                            .build());
                }
                // remove duplicate
                annotationSpecs = annotationSpecs.stream()
                        .collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> o.type.toString()))), ArrayList::new));

                // build class
                typeSpecBuilder.addModifiers(Modifier.PUBLIC);
                typeSpecBuilder.addFields(fieldSpecs);
                typeSpecBuilder.addMethods(methodSpecs);
                typeSpecBuilder.addAnnotations(annotationSpecs);

                typeSpecBuilders.put(element.getAnnotation(GenController.class).name(), typeSpecBuilder);

//                messager.printMessage(Diagnostic.Kind.NOTE, r("::::::::::::::::") + parameter.toString());
            }

            if (typeSpecBuilders.size() > 0) {
                if (!"".equals(config.getPackagePath())) {
                    typeSpecBuilders.forEach((k, v) -> {
                        TypeSpec build = v.build();

                        try {
                            JavaFile javaFile = JavaFile.builder(config.getPackagePath(), build)
                                    .addFileComment(" This codes are generated automatically. Do not modify!")
                                    .build();
                            javaFile.writeTo(filer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
        return true;
    }


    public List<MethodSpec> buildMethods(Config config, Element classElement) {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (Element element : classElement.getEnclosedElements()) {
            ExecutableElement methodElement = (ExecutableElement) element;
            String[] ignoreMethods = new String[] {"<init>", "main"};

            if (Arrays.stream(ignoreMethods).collect(Collectors.toList()).contains(methodElement.getSimpleName().toString())) {
                continue;
            }
            if (!classElement.getClass().isInterface() && !"public".equals(methodElement.getModifiers().stream().collect(Collectors.toList()).get(0).toString())) {
                continue;
            }
            if (methodElement.getAnnotation(GenControllerMethod.class) != null && methodElement.getAnnotation(GenControllerMethod.class).ignore()) {
                continue;
            }

            // 排除构造函数、mian方法，只匹配public开头的方法
//            if (!Arrays.stream(ignoreMethods).toList().contains(methodElement.getSimpleName().toString())
//                    && "public".equals(methodElement.getModifiers().stream().toList().get(0).toString())
//                   ) {

            // 初始化方法
            MethodSpec.Builder builder = MethodSpec.methodBuilder(methodElement.getSimpleName().toString());
            builder.addModifiers(Modifier.PUBLIC);

            // 添加PostMapping注解
            String mapping = "/" + MyUtil.toSimpleName(classElement.getAnnotation(GenController.class).name(), true, "-") + "/" + MyUtil.toSimpleName(methodElement.getSimpleName().toString(), true, "-");
            builder.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PostMapping")).addMember("value", "$S", mapping).build());
            GenControllerMethod GenControllerMethod = methodElement.getAnnotation(GenControllerMethod.class);
            if (GenControllerMethod != null) {
                builder.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("io.swagger.v3.oas.annotations.Operation"))
                        .addMember("summary", "$S", GenControllerMethod.springDocOperationSummary())
                        .addMember("description", "$S", GenControllerMethod.springDocOperationDescription())
                        .build());
            }




            // 添加入参参数
            List<ParameterSpec> parameterSpecs = new ArrayList<>();
            for (VariableElement parameter : methodElement.getParameters()) {
                ParameterSpec.Builder paramterSpecBuilder = ParameterSpec.builder(ClassName.bestGuess(parameter.asType().toString()), parameter.toString());
                paramterSpecBuilder.addAnnotation(ClassName.bestGuess("org.springframework.validation.annotation.Validated"));
                paramterSpecBuilder.addAnnotation(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestBody"));
                parameterSpecs.add(paramterSpecBuilder.build());

//                    messager.printMessage(Diagnostic.Kind.NOTE, r("::::::::::::::::") + parameter.toString());
//                    messager.printMessage(Diagnostic.Kind.NOTE, r("::::::::::::::::") + parameter.getSimpleName().toString());
//                    messager.printMessage(Diagnostic.Kind.NOTE, r("::::::::::::::::") + parameter.asType().toString());
            }
            builder.addParameters(parameterSpecs);

            // 添加出参参数
            // response-parameterized
            // response-return
//                String responseType = "gen-controller.response-parameter";
//                String returnExpression = "gen-controller.response-return-expression";
            if (!"".equals(config.getResponseType())) {
                ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
                        ClassName.bestGuess(config.getResponseType()),
                        ClassName.bestGuess(methodElement.getReturnType().toString()));
                builder.returns(parameterizedTypeName);


//                    messager.printMessage(Diagnostic.Kind.NOTE, r("ssssssssssss::::::::::::::::") + parameterSpecs.get(0).name);

                builder.addStatement("$L response = $L.$L($L)",
                        MyUtil.toSimpleName(methodElement.getReturnType().toString(), false, null),
                        MyUtil.toSimpleName(classElement.getSimpleName().toString(), true, null),
                        methodElement.getSimpleName().toString(),
                        parameterSpecs.get(0).name);
                builder.addStatement("return " + config.getReturnExpression(), "response");
            } else {
                builder.returns(ClassName.bestGuess(methodElement.getReturnType().toString()));
                builder.addStatement("return $L.$L($L)",
                        MyUtil.toSimpleName(classElement.getSimpleName().toString(), true, null),
                        methodElement.getSimpleName().toString(),
                        parameterSpecs.get(0).name);
            }

            methodSpecs.add(builder.build());
//            }
        }
        return methodSpecs;
    }
}