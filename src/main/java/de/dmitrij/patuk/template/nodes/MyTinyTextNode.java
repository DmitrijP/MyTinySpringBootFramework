package de.dmitrij.patuk.template.nodes;

import de.dmitrij.patuk.template.MyTinyContext;

import java.util.Objects;

public class MyTinyTextNode implements MyTinyTemplateNode {
    String text;
    public MyTinyTextNode(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyTinyTextNode that)) return false;
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(text);
    }

    @Override
    public String render(MyTinyContext context) {
        return text;
    }

    @Override
    public String prettyPrint(String indent) {
        return indent + "Text(\"" + text.replace(" ", "").replace("\n", "") + "\")\n";
    }
}
