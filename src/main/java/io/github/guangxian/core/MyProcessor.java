package io.github.guangxian.core;

import com.squareup.javapoet.*;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SupportedAnnotationTypes(value = {"io.github.guangxian.core.GenController"})
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
        Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(GenController.class);

        // 获取yml配置
        YamlPropertiesFactoryBean yamlProFb = new YamlPropertiesFactoryBean();
        yamlProFb.setResources(new ClassPathResource("application.yml"));
        Properties properties = yamlProFb.getObject();

        boolean enable = properties.get("stc.enable") != null ? Boolean.parseBoolean(properties.get("stc.enable").toString()) : true;
        if (!elementsAnnotatedWith.isEmpty() && enable) {
            Map<String, TypeSpec.Builder> typeSpecBuilders = new HashMap<>();
            for (Element element : elementsAnnotatedWith) {
                TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(MyUtil.firstUpper(element.getAnnotation(GenController.class).name()) + "Controller");

                List<MethodSpec> methodSpecs = buildMethods(properties, element);// new ArrayList<>() 为当前生成的methods

                List<FieldSpec> fieldSpecs = new ArrayList<>();
                fieldSpecs.add(FieldSpec
                        .builder(ClassName.bestGuess(element.toString()), firstLower(element.getSimpleName().toString()))
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                        .build());

                // if same controller name, then add fields and methods
                if (typeSpecBuilders.containsKey(element.getAnnotation(GenController.class).name())) {
                    TypeSpec.Builder builder = typeSpecBuilders.get(element.getAnnotation(GenController.class).name());
                    fieldSpecs.addAll(builder.fieldSpecs);
                    methodSpecs.addAll(builder.methodSpecs);
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

                // build class
                typeSpecBuilder.addFields(fieldSpecs);
                typeSpecBuilder.addMethods(methodSpecs);
                typeSpecBuilder.addAnnotation(ClassName.bestGuess("org.springframework.web.bind.annotation.RestController"));
                typeSpecBuilder.addModifiers(Modifier.PUBLIC);
                typeSpecBuilders.put(element.getAnnotation(GenController.class).name(), typeSpecBuilder);
            }

            typeSpecBuilders.forEach((k, v) -> {
                TypeSpec build = v.build();

                try {
                    JavaFile javaFile = JavaFile.builder("com.example.web.controller", build)
                            .addFileComment(" This codes are generated automatically. Do not modify!")
                            .build();
                    javaFile.writeTo(filer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }


        return true;
    }


    public List<MethodSpec> buildMethods(Properties properties, Element classElement) {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (Element element : classElement.getEnclosedElements()) {
            ExecutableElement methodElement = (ExecutableElement) element;
            String[] ignoreMethods = new String[] {"<init>", "main"};

            // 排除构造函数、mian方法，只匹配public开头的方法
            if (!Arrays.stream(ignoreMethods).collect(Collectors.toList()).contains(methodElement.getSimpleName().toString())
                    && "public".equals(methodElement.getModifiers().stream().collect(Collectors.toList()).get(0).toString())) {

                // 初始化方法
                MethodSpec.Builder builder = MethodSpec.methodBuilder(methodElement.getSimpleName().toString());

                // 添加PostMapping注解
                String mapping = "/" + MyUtil.toSimpleName(classElement.getAnnotation(GenController.class).name(), true, "-") + "/" + MyUtil.toSimpleName(methodElement.getSimpleName().toString(), true, "-");
                builder.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PostMapping")).addMember("value", "$S", mapping).build());
                StcMethod stcMethod = methodElement.getAnnotation(StcMethod.class);
                if (stcMethod != null) {
                    builder.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("io.swagger.v3.oas.annotations.Operation"))
                            .addMember("summary", "$S", stcMethod.summary())
                            .addMember("description", "$S", stcMethod.description())
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
                String responseParameter = "stc.response-parameter";
                String responseReturnExpression = "stc.response-return-expression";
                if (properties.get(responseParameter) != null) {
                    ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(
                            ClassName.bestGuess(properties.get(responseParameter).toString()),
                            ClassName.bestGuess(methodElement.getReturnType().toString()));
                    builder.returns(parameterizedTypeName);


//                    messager.printMessage(Diagnostic.Kind.NOTE, r("ssssssssssss::::::::::::::::") + parameterSpecs.get(0).name);

                    builder.addStatement("$L response = $L.$L($L)",
                            MyUtil.toSimpleName(methodElement.getReturnType().toString(), false, null),
                            MyUtil.toSimpleName(classElement.getSimpleName().toString(), true, null),
                            methodElement.getSimpleName().toString(),
                            parameterSpecs.get(0).name);
                    builder.addStatement("return " + properties.get(responseReturnExpression).toString(), "response");
                } else {
                    builder.returns(ClassName.bestGuess(methodElement.getReturnType().toString()));
                    builder.addStatement("return $L.$L(request)", MyUtil.toSimpleName(classElement.getSimpleName().toString(), true, null), methodElement.getSimpleName().toString());
                }

                methodSpecs.add(builder.build());
            }
        }
        return methodSpecs;
    }
}
