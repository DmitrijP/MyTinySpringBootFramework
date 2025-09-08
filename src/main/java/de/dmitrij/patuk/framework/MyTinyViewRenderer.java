package de.dmitrij.patuk.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTinyViewRenderer {
    private final MyTinyViewProvider provider;

    public MyTinyViewRenderer(MyTinyViewProvider provider) {
        this.provider = provider;
    }

    public String render(String viewName, Object model) {
        var template = provider.provideTinyView(viewName);
        // our foreach will look like this {{ foreach item : items }} text {{ endforeach }}
        Pattern foreachPattern = Pattern.compile("\\{\\{ foreach (\\w+) : (\\w+) }}(.*?)\\{\\{ endforeach }}", Pattern.DOTALL);
        Matcher matcher = foreachPattern.matcher(template);

        try {
            while (matcher.find()) {
                String loopVar = matcher.group(1);
                String collectionName = matcher.group(2);
                String loopBody = matcher.group(3);

                // Get collection from model
                Method method = model.getClass().getDeclaredMethod(toPropertyName(collectionName));

                Object collectionObj = method.invoke(model);

                if (!(collectionObj instanceof Collection<?> col)) {
                    throw new RuntimeException("Field " + collectionName + " is not a Collection");
                }

                // Build replacement
                StringBuilder loopResult = new StringBuilder();
                for (Object item : col) {
                    String itemBlock = loopBody.replace("{{ " + loopVar + " }}", item.toString());
                    loopResult.append(itemBlock);
                }

                // Replace the entire foreach block
                template = template.replace(matcher.group(0), loopResult.toString());
            }
        } catch (NoSuchMethodException | IllegalAccessException |InvocationTargetException e) {
            throw new RuntimeException(e);
        }


        var methods = model.getClass().getDeclaredMethods();
        for (var method : methods) {
            if(!method.canAccess(model)) {
                continue;
            }
            if(!isGetter(method)) {
                continue;
            }
            var propertyName = getPropertyName(method);
            Object value = null;
            try {
                value = method.invoke(model);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Field was not accessible",e);
            }

            String placeholder = "{{" + propertyName + "}}";
            template = template.replace(placeholder, value != null ? value.toString() : "");
        }
        return template;
    }

    private boolean isGetter(Method method) {
        if (method.getParameterCount() > 0) return false;
        if (method.getReturnType() == void.class) return false;

        String name = method.getName();
        if (name.startsWith("get") && name.length() > 3) return true;
        if (name.startsWith("is") && name.length() > 2
                && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
            return true;
        }
        return false;
    }

    private String getPropertyName(Method method) {
        String name = method.getName();
        if (name.startsWith("get")) {
            return Character.toLowerCase(name.charAt(3)) + name.substring(4);
        }
        if (name.startsWith("is")) {
            return Character.toLowerCase(name.charAt(2)) + name.substring(3);
        }
        return name;
    }

    private String toPropertyName(String propertyName) {
        return "get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    }
}
