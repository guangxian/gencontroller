package io.github.guangxian.gencontroller.core;

public class ApiMethod {
    private String name;
    private String request;
    private String response;
    private ApiMethodDoc doc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public ApiMethodDoc getDoc() {
        return doc;
    }

    public void setDoc(ApiMethodDoc doc) {
        this.doc = doc;
    }
}
