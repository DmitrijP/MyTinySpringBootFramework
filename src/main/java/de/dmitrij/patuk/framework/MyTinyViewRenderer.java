package de.dmitrij.patuk.framework;

import de.dmitrij.patuk.template.MyTinyContext;
import de.dmitrij.patuk.template.MyTinyParser;
import de.dmitrij.patuk.template.MyTinyTokenStream;
import de.dmitrij.patuk.template.MyTinyTokenizer;

public class MyTinyViewRenderer {
    private final MyTinyViewProvider provider;
    private final MyTinyParser parser;
    private final MyTinyTokenizer tokenizer;

    public MyTinyViewRenderer(MyTinyViewProvider provider, MyTinyParser parser, MyTinyTokenizer tokenizer) {
        this.provider = provider;
        this.parser = parser;
        this.tokenizer = tokenizer;
    }

    public String render(String viewName, Object model) {
        var templateString = provider.provideTinyView(viewName);
        var tokens = tokenizer.tokenize(templateString);
        var template = parser.parseTemplate(new MyTinyTokenStream(tokens));
        System.out.println( tokenizer.tokensToString(tokens));
        System.out.println(template.prettyPrint(""));
        return template.render(new MyTinyContext(model));
    }
}
