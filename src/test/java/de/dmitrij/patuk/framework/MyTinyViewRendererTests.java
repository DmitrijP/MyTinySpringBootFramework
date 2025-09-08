package de.dmitrij.patuk.framework;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DummyModel {
    public String name = "World";
    public int age = 42;
    private String secret = "hidden";
}

public class MyTinyViewRendererTests {
    MyTinyViewProvider provider;
    MyTinyViewRenderer renderer;

    @BeforeEach
    void setup() {
        provider = mock(MyTinyViewProvider.class);
        renderer = new MyTinyViewRenderer(provider);
    }

    @Test
    void rendersTemplateWithPublicFields() {
        when(provider.provideTinyView("hello")).thenReturn("Hello, {{name}}! You are {{age}} years old.");
        DummyModel model = new DummyModel();
        String result = renderer.render("hello", model);
        assertTrue(result.contains("World"));
        assertTrue(result.contains("42"));
        assertEquals("Hello, World! You are 42 years old.", result);
    }

    @Test
    void leavesUnknownPlaceholdersUnchanged() {
        when(provider.provideTinyView("test")).thenReturn("Hi, {{name}}! {{unknown}}");
        DummyModel model = new DummyModel();
        String result = renderer.render("test", model);
        assertTrue(result.contains("{{unknown}}"));
    }

    @Test
    void skipsInaccessibleFields() {
        when(provider.provideTinyView("secret")).thenReturn("Secret: {{secret}}");
        DummyModel model = new DummyModel();
        String result = renderer.render("secret", model);
        // Should not replace private field
        assertEquals("Secret: {{secret}}", result);
    }

    @Test
    void replacesNullFieldWithEmptyString() {
        when(provider.provideTinyView("nulltest")).thenReturn("Name: {{name}} Age: {{age}} Null: {{nullField}}");
        class NullModel {
            public String name = null;
            public int age = 0;
            public String nullField = null;
        }
        NullModel model = new NullModel();
        String result = renderer.render("nulltest", model);
        assertTrue(result.contains("Name:  Age: 0 Null: "));
    }
}
