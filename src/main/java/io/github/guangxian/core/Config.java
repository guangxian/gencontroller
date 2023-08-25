package io.github.guangxian.core;

public class Config {
    Boolean enable;
    String packagePath;
    String responseType;
    String returnExpression;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public String getPackagePath() {
        return packagePath;
    }

    public void setPackagePath(String packagePath) {
        this.packagePath = packagePath;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getReturnExpression() {
        return returnExpression;
    }

    public void setReturnExpression(String returnExpression) {
        this.returnExpression = returnExpression;
    }
}
