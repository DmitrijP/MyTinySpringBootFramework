package de.dmitrij.patuk.framework;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

public class MyTinyViewProviderTests {
    @Test
    void throwsIfViewNotFound() {
        MyTinyViewProvider provider = new MyTinyViewProvider();
        Exception ex = assertThrows(RuntimeException.class, () -> provider.provideTinyView("/notfound"));
        assertTrue(ex.getMessage().contains("Unable to read view"));
    }

    @Test
    void readsViewFromResource(@TempDir Path tempDir) throws Exception {
        // Simulate a resource by copying a file to the classpath
        String viewName = "/testview";
        String content = "<h1>Hello View</h1>";
        Path viewsDir = tempDir.resolve("views");
        Files.createDirectories(viewsDir);
        Path viewFile = viewsDir.resolve("testview.html");
        Files.writeString(viewFile, content);
        // Add tempDir to classloader
        ClassLoader cl = new java.net.URLClassLoader(new java.net.URL[]{tempDir.toUri().toURL()}, getClass().getClassLoader());
        Thread.currentThread().setContextClassLoader(cl);
        MyTinyViewProvider provider = new MyTinyViewProvider();
        String result = provider.provideTinyView(viewName);
        assertTrue(result.contains("Hello View"));
    }
}
