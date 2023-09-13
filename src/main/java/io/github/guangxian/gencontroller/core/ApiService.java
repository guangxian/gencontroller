package io.github.guangxian.gencontroller.core;

import java.util.ArrayList;
import java.util.List;

public class ApiService {
    private String name;
    private List<ApiMethod> methods = new ArrayList<>();

    public ApiService(String name, List<ApiMethod> methods) {
        this.name = name;
        this.methods = methods;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ApiMethod> getMethods() {
        return methods;
    }

    public void setMethods(List<ApiMethod> methods) {
        this.methods = methods;
    }

    public ApiService() {
    }
}
