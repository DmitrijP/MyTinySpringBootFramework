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

## 2. Reading properties file

A simple class that gets a file name and searches for it in our applications resources directory.
If found it is simply read by the existing Properties class and can be accessed with keys.
```java
public class MyTinyPropertiesScanner {
    private final Properties properties = new Properties();
    public MyTinyPropertiesScanner(String fileName) {
        //getClass() gets the current class
        //getClassLoader().getResourceAsStream() looks in the resource 
        // directory and opens a file
        try (var fis = getClass().getClassLoader()
                .getResourceAsStream(fileName)) {
            if (fis != null) {
                properties.load(fis);
                System.out.println("Loaded properties from " + fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load properties file", e);
        }
    }
    public String get(String key) {
        return properties.getProperty(key);
    }
}
```

## 3. Creating a Dependency Container

We create a container to scan our configuration classes that are marked with `@MyTinyConfiguration` and provide dependenceis via `@MyTinyBean`

The Annotations. Notice the Target of `MyTinyBean` it only targets methods.
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MyTinyConfiguration {
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyTinyBean {
}
```

Now the dependency container. It stores the instances of classes that are created inside methods annotated with `@MyTinyBean` inside a Map.
```java
public class MyTinyApplicationContext {
    private final Map<Class<?>, Object> beans = new HashMap<>();

    public void registerConfiguration(Class<?> configClass) {
        //We check if the class is annotated with @MyConfiguration
        if (!configClass.isAnnotationPresent(MyTinyConfiguration.class)) {
            return;
        }

        try {
            //We instantiate the Class in order to call the bean methods
            Object configInstance = configClass.getDeclaredConstructor().newInstance();

            //We get all the methods
            var methods = configInstance.getClass().getDeclaredMethods();
            //we need at least one method
            if (methods.length < 1) {
                throw new RuntimeException("No provider methods found for class " + configClass.getName());
            }

            for (var method : methods) {
                //we only care about annotated methods
                if (!method.isAnnotationPresent(MyTinyBean.class)) {
                    continue;
                }

                //our provider methods must have a return type that is not void.
                if (method.getReturnType() == Void.TYPE) {
                    throw new RuntimeException("Missing return type for method " + method.getName());
                }

                var object = method.invoke(configInstance);
                var name = method.getName();

                beans.put(object.getClass(), object);
                System.out.println("Registered bean: " + name + " -> " + object.getClass().getSimpleName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to register configuration " + configClass, e);
        }
    }

    public <T> T getBean(Class<T> beanClass) {
        if(!beans.containsKey(beanClass)) {
            throw new RuntimeException("No bean registered for " + beanClass);
        }
        return (T)beans.get(beanClass);
    }
}
```

