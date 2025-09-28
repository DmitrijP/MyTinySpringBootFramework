package de.dmitrij.patuk.framework;

import de.dmitrij.patuk.template.MyTinyParser;
import de.dmitrij.patuk.template.MyTinyTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ListModel {
    private final List<String> items;
    public ListModel(List<String> items) { this.items = items; }
    public List<String> getItems() { return items; }
    public String getTitle() { return "My List"; }
}

public class MyTinyViewRendererForeachTests {
    MyTinyViewProvider provider;
    MyTinyViewRenderer renderer;
    MyTinyParser parser;
    MyTinyTokenizer tokenizer;

    @BeforeEach
    void setup() {
        provider = mock(MyTinyViewProvider.class);
        parser = mock(MyTinyParser.class);
        tokenizer = mock(MyTinyTokenizer.class);
        renderer = new MyTinyViewRenderer(provider, parser, tokenizer);
    }

    @Test
    void rendersForeachBlockWithItems() {
        String template = "<h1>{{title}}</h1>\n<ul>{{ foreach item : items }}<li>{{ item }}</li>{{ endforeach }}</ul>";
        when(provider.provideTinyView("list")).thenReturn(template);
        ListModel model = new ListModel(List.of("A", "B", "C"));
        String result = renderer.render("list", model);
        assertTrue(result.contains("<li>A</li>"));
        assertTrue(result.contains("<li>B</li>"));
        assertTrue(result.contains("<li>C</li>"));
        assertTrue(result.contains("<h1>My List</h1>"));
    }

    @Test
    void rendersEmptyForEmptyCollection() {
        String template = "<ul>{{ foreach item : items }}<li>{{ item }}</li>{{ endforeach }}</ul>";
        when(provider.provideTinyView("empty")).thenReturn(template);
        ListModel model = new ListModel(List.of());
        String result = renderer.render("empty", model);
        assertEquals("<ul></ul>", result.replaceAll("\\s+", ""));
    }

    @Test
    void throwsIfNotACollection() {
        class BadModel { public String getItems() { return "not a list"; } }
        String template = "{{ foreach item : items }}{{ item }}{{ endforeach }}";
        when(provider.provideTinyView("bad")).thenReturn(template);
        BadModel model = new BadModel();
        Exception ex = assertThrows(RuntimeException.class, () -> renderer.render("bad", model));
        assertTrue(ex.getMessage().contains("is not a Collection"));
    }
}
