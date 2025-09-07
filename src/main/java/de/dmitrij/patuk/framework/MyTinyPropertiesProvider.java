package de.dmitrij.patuk.framework;

import java.lang.reflect.Parameter;

public class MyTinyPropertiesProvider {
    private final MyTinyPropertiesScanner propertiesScanner;

    public MyTinyPropertiesProvider(MyTinyPropertiesScanner propertiesScanner) {
        this.propertiesScanner = propertiesScanner;
    }

    public String provide(Parameter parameter) {
        if (!parameter.isAnnotationPresent(MyTinyValue.class)) {
            throw new IllegalArgumentException("@MyTinyValue annotation is not present");
        }

        var annotation = parameter.getAnnotation(MyTinyValue.class);
        var propertyName = annotation.name();

        if(propertyName.isEmpty()) {
            throw new IllegalArgumentException("@MyTinyPropertyName annotation is not present");
        }

        return propertiesScanner.get(propertyName);
    }

    public boolean canProvide(Parameter parameter) {
        if (parameter == null){
            return false;
        }
        return parameter.isAnnotationPresent(MyTinyValue.class);
    }
}
