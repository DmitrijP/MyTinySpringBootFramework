package de.dmitrij.patuk.template.nodes;

import de.dmitrij.patuk.template.MyTinyContext;

import java.util.List;
import java.util.Objects;

public class MyTinyTemplate implements MyTinyTemplateNode {
    final List<MyTinyTemplateNode> nodes;

    public MyTinyTemplate(List<MyTinyTemplateNode> nodes) {
        this.nodes = nodes;
    }

    public List<MyTinyTemplateNode> getNodes() {
        return nodes;
    }

    public void append(MyTinyTemplateNode node) {
        nodes.add(node);
    }

    public MyTinyTemplateNode popNode() {
        if (nodes.isEmpty()) return null;
        return nodes.removeFirst();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyTinyTemplate that)) return false;
        return Objects.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(nodes);
    }

    @Override
    public String render(MyTinyContext context) {
        StringBuilder sb = new StringBuilder();
        for (MyTinyTemplateNode node : nodes) {
            sb.append(node.render(context));
        }
        return sb.toString();
    }

    @Override
    public String prettyPrint(String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(indent).append("Template");
        for (MyTinyTemplateNode child : nodes) {
            sb.append(child.prettyPrint(indent + "  "));
        }
        return sb.toString();
    }
}
