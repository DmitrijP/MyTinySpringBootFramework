package de.dmitrij.patuk.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//Keep the annotation during runtime
@Retention(RetentionPolicy.RUNTIME)
//only allowed for types/classes
@Target(ElementType.TYPE)
public @interface MyTinyBootApplication {
}
