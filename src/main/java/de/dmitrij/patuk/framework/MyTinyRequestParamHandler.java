package de.dmitrij.patuk.framework;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class MyTinyRequestParamHandler {

    public Map<String, String> getRequestParams(String query) {
        var parsedParams = new HashMap<String, String>();
        if (query == null) {
            return parsedParams;
        }
        if (query.isEmpty()) {
            return parsedParams;
        }
        var queryParams = query.split("&");
        for (var param : queryParams) {
            if (param.isEmpty()) {
                continue;
            }
            var paramParts = param.split("=");
            if (paramParts.length != 2) {
                continue;
            }
            parsedParams.put(paramParts[0], paramParts[1]);
        }
        return parsedParams;
    }

    public <T> T handle(Parameter methodParameter, Map<String, String> requestParams) {
        if (!methodParameter.isAnnotationPresent(MyTinyRequestParam.class)) {
            throw new RuntimeException(
                    "Parameter " + methodParameter.getName() + " is not annotated with @MyTinyRequestParam");
        }
        var annotation = methodParameter.getAnnotation(MyTinyRequestParam.class);
        var name = annotation.name();
        var type = methodParameter.getType();
        if (!requestParams.containsKey(name)) {
            throw new RuntimeException("Parameter " + name + " is not inside request parameters");
        }
        var value = requestParams.get(name);

        if (type.equals(String.class)) {
            return (T) value;
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            return (T) Integer.valueOf(value);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return (T) Long.valueOf(value);
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return (T) Double.valueOf(value);
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            return (T) Float.valueOf(value);
        }
        throw new RuntimeException("Type " + type + " is not supported");
    }

    public boolean canHandle(Parameter parameter) {
        return parameter.isAnnotationPresent(MyTinyRequestParam.class);
    }
}
