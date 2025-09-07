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

## 4. Autoinjection of Properties

We have created a lass in `2. Reading properties files` that is able to read the .properties File and provide us with those properties.
We will now make use of that.

First we need an annotation to mark those parameters that should be provided with values from the properties file.
Notice the `Target` of the annotation and the property `name()` that is able to store a string.
This will be used to name the property key that we want to be provided.
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface MyTinyValue {
    String name() default "";
}
```

Now the class that does most of the work. It receives the properties scanned that we build earlier.
Each call to `provide()` expects a Parameter. That parameter must be annotated with `@MyTinyValue`.
We extract the string that is inside the `name()` property of the annotation and use it to get the property from the `properties scanner`.
```java
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
```

Now the update to our dependency container. It requires the `PropertiesProvider` we just build.
We use it on each parameter of a method that is annotated with `@MyTinyBean` to get the properties and store them in a list.
This list is then passed to the constructor of our service to be created.
```java

public class MyTinyApplicationContext {
    private final Map<Class<?>, Object> beans = new HashMap<>();

    //new ===========
    private final MyTinyPropertiesProvider propertiesProvider;
    public MyTinyApplicationContext(MyTinyPropertiesProvider propertiesProvider) {
        this.propertiesProvider = propertiesProvider;
    }
    //new ===========

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
                if (!method.isAnnotationPresent(MyTinyBean.class)) {
                    continue;
                }

                if (method.getReturnType() == Void.TYPE) {
                    throw new RuntimeException("Missing return type for method " + method.getName());
                }

                //new ===========
                ArrayList<Object> values = new ArrayList<>();
                var parameters = method.getParameters();
                for (var parameter : parameters) {
                    if(!propertiesProvider.canProvide(parameter)){
                        continue;
                    }
                    var value = propertiesProvider.provide(parameter);
                    values.add(value);
                }
                //new ===========

                var object = method.invoke(configInstance, values.toArray());
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
Now the Application class. It only requires a small change.
We inject the provider into our context, the rest should work just as it is.
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

        var propertiesScanner = new MyTinyPropertiesScanner("application.properties");
        System.out.println(propertiesScanner.get("my.boot.application-name"));

        //new ===========
        var propertiesProvider = new MyTinyPropertiesProvider(propertiesScanner);
        var context = new MyTinyApplicationContext(propertiesProvider);
        //new ===========

        context.registerConfiguration(AppConfig.class);
        var service = context.getBean(AppService.class);
        System.out.println("Starting " + service.call());


        System.out.println("Application started!");
    }
}
```

Now an update to our `AppConfig` and `AppService` classes.
The Config receives the `@MyTinyValue` notice the string `my.config-value` it is the key of our configuration inside the `application.properties` file.
The `AppService` just expects that value and stores it to print it later as an example.
```java
@MyTinyConfiguration
public class AppConfig {
    @MyTinyBean
    public AppService provideAppService(@MyTinyValue(name = "my.config-value") String value) {
        return new AppService(value);
    }
}

public class AppService {
    private final String importantConfigValue;

    public AppService(String importantConfigValue) {
        this.importantConfigValue = importantConfigValue;
    }

    public String call() {
        return "Hello from AppService! \n" + "The important config value is: " + importantConfigValue + "\n";
    }
}
```
The `application.properties` file
```properties
my.boot.application-name:My App Name
my.config-value:The Config Value
```

## 5. Scanning for annotated classes

Next we will need the ability to find all classes with a certain annotation in our package. For example all classes annotated with `@MyTinyConfiguration` in order to automatically register all dependency providers.

This class scans from the base directory of our application every single sub package and collects the Classes annotated with the given annotation.
They are simply returned inside a list for further processing.
```java

public class MyTinyClassScanner {

    public List<Class<?>> findAnnotatedClasses(String packageName, Class annotation) {
        System.out.println("findAnnotatedClasses " + packageName + " for " + annotation);
        var annotatedClasses = new ArrayList<Class<?>>();

        //Turn package names into a path
        var path = packageName.replace('.', '/');

        try {
            //find all directories in our package that we need to scan
            var resources = Thread.currentThread().getContextClassLoader().getResources(path);
            List<File> dirs = new ArrayList<>();
            while (resources.hasMoreElements()) {
                var url = resources.nextElement();
                dirs.add(new File(url.getFile()));
            }

            //scan the found directories for the annotated classes
            for (File dir : dirs) {
                annotatedClasses.addAll(findClasses(dir, packageName, annotation, 0));
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return annotatedClasses;
    }

    private List<Class<?>> findClasses(File dir, String packageName, Class annotation, int level)
            throws ClassNotFoundException {
        System.out.println("called findClasses " + packageName + " for " + annotation + " level " + level);
        List<Class<?>> classes = new ArrayList<>();
        if(dir == null){
            return classes;
        }
        if (!dir.exists()) {
            return classes;
        }
        //get all files in the directory
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                System.out.printf("Scanning directory %s...%n", f.getName());
                //go one directory down and scan again
                classes.addAll(findClasses(f, packageName + "." + f.getName(), annotation, level + 1));
                continue;
            }
            //we have a class
            if (f.getName().endsWith(".class")) {
                //we get the package name and get the class for that name
                String className = packageName + '.' + f.getName().replace(".class", "");
                System.out.println("Getting class for name " + className);
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(annotation)) {
                    System.out.println("Found class " + clazz.getSimpleName() + "with annotation " + annotation);
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }
}
```

Changes inside `MyTinyApplication`

We simply create a new instance of our `MyTinyClassScanner` and let it search for all classes annotated with `@MyTinyConfiguration`.
Next we register all those classes inside our `MyTinyApplicationContext` and simply request our `AppService` as a test.
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

        var propertiesScanner = new MyTinyPropertiesScanner("application.properties");
        System.out.println(propertiesScanner.get("my.boot.application-name"));

        var propertiesProvider = new MyTinyPropertiesProvider(propertiesScanner);
        var context = new MyTinyApplicationContext(propertiesProvider);

        //new ====================
        var classScanner = new MyTinyClassScanner();
        var configClasses = classScanner.findAnnotatedClasses(appClass.getPackageName(),
                MyTinyConfiguration.class);
        for(var configClass : configClasses) {
            context.registerConfiguration(configClass);
        }
        //new ====================

        var service = context.getBean(AppService.class);
        System.out.println("Starting " + service.call());
        System.out.println("Application started!");
    }
}
```