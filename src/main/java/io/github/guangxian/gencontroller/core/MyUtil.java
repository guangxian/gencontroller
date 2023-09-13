package io.github.guangxian.gencontroller.core;

public class MyUtil {
    public static String toSimpleName(String name, boolean lower, String split) {
        name = name.substring(name.lastIndexOf(".")+1);
        if (lower) {
            name = firstLower(name);
            System.out.println("1:" + name);
        }
        if (split != null && !split.isEmpty()) {
            name = firstLower(name);
            System.out.println("2:" + name);
            name = camelToUnderline(name);
            System.out.println("3:" + name);
            name = name.replaceAll("_",split);
            System.out.println("4:" + name);
        }
        return name;
    }
    public static String firstUpper(String str) {
        if (!Character.isUpperCase(str.charAt(0))) {
            char[] cs = str.toCharArray();
            cs[0] -= 32;
            return String.valueOf(cs);
        }
        return str;
    }

    public static String firstLower(String str) {
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


//    public static MethodSpec.Builder docHandler(MethodSpec.Builder builder, ApiMethod method) {
//        if (method.getDoc() != null) {
//            builder.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("io.swagger.v3.oas.annotations.Operation"))
//                    .addMember("summary", "$S", method.getDoc().getSummary())
//                    .addMember("description", "$S", method.getDoc().getDescription())
//                    .build());
//        }
//        return builder;
//    }





//    public static List<? extends Element>  removeInvalidMethod(List<? extends Element> enclosedElements) {
//        String[] ignoreMethods = new String[] {"<init>", "main"};
//        enclosedElements.removeIf(v ->
//                !Arrays.stream(ignoreMethods).toList().contains(v.getSimpleName().toString())
//                && "public".equals(v.getModifiers().stream().toList().get(0).toString())
//        );
//        return enclosedElements;
//    }





}
