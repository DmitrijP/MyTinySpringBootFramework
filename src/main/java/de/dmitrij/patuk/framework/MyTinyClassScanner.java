package de.dmitrij.patuk.framework;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyTinyClassScanner {

    public List<Class<?>> findAnnotatedClasses(String packageName, Class annotation) {
        System.out.println("findAnnotatedClasses " + packageName + " for " + annotation);
        var annotatedClasses = new ArrayList<Class<?>>();

        //Turn package names into a path
        var path = packageName.replace('.', '/');

        try {
            var resources = Thread.currentThread().getContextClassLoader().getResources(path);
            List<File> dirs = new ArrayList<>();
            while (resources.hasMoreElements()) {
                var url = resources.nextElement();
                dirs.add(new File(url.getFile()));
            }

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
