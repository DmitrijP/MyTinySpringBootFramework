package de.dmitrij.patuk.template.nodes;

import de.dmitrij.patuk.template.MyTinyContext;

public interface MyTinyTemplateNode  {
    String render(MyTinyContext context);
    String prettyPrint(String indent);
}
