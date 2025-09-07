# My tiny spring boot like framework

## 1. Creation of annotation and entrypoint into the framework

Firs we create our own annotation that marks the entrypoint into our framework. 
```java
//Keep the annotation during runtime
@Retention(RetentionPolicy.RUNTIME)
//only allowed for types/classes
@Target(ElementType.TYPE)
public @interface MyTinyBootApplication {
}
```

Important are the annotations `@Retention` and `@Target`.
- Retention has a parameter called `RetentionPolicy` it governs how long our annotation is kept on the class. Runtime means that is is kept during the runtime of our app and we can access this annotation during that time.
- Target governs what we can apply the annotation on e.g. Type, Method, Parameter...

The class `MyTinyApplication` is launched as early as possible and controls our whole framework.

```java
public class MyTinyApplication {
    public static void run(Class<?> appClass, String[] args) {
        if(!appClass.isAnnotationPresent(MyTinyBootApplication.class)) {
            throw new IllegalArgumentException(appClass.getName() +
                    " is not annotated with @MyTinyBootApplication");
        }

        System.out.println("Starting " + appClass.getName());

        try {
            var instance = appClass.getDeclaredConstructor().newInstance();
            System.out.println("Created main application instance: " + instance);
        } catch (NoSuchMethodException | InstantiationException |
                 RuntimeException | IllegalAccessException |
                 InvocationTargetException e) {
            e.printStackTrace();
            System.out.printf("Failed to instantiate main application %s\n", appClass.getName());
        }
        System.out.println("Application started!");
    }
}

```
At this time it only creates a new instance of the Class it is applied to and prints some messages.