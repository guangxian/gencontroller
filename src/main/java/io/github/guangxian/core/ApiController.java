package io.github.guangxian.core;

import java.util.ArrayList;
import java.util.List;

public class ApiController {
    private String name;
    private List<ApiService> services = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ApiService> getServices() {
        return services;
    }

    public void setServices(List<ApiService> services) {
        this.services = services;
    }

    public ApiController(String name, List<ApiService> services) {
        this.name = name;
        this.services = services;
    }

    public ApiController() {
    }
}
