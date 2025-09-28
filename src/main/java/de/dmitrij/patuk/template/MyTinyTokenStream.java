package de.dmitrij.patuk.template;

import de.dmitrij.patuk.template.token.MyTinyToken;
import de.dmitrij.patuk.template.token.MyTinyTokenType;

import java.util.List;

public class MyTinyTokenStream {
    private final List<MyTinyToken> tokens;
    private int position = 0;

    public MyTinyTokenStream(List<MyTinyToken> tokens) {
        this.tokens = tokens;
    }

    MyTinyToken peek() {
        //we get the current position is is now the next token
        return position < tokens.size() ? tokens.get(position) : null;
    }

    MyTinyToken peekNext() {
        //we get the next position
        return position + 1 < tokens.size() ? tokens.get(position + 1) : null;
    }

    MyTinyToken next() {
        //we get the current position and increment position to the next
        return position < tokens.size() ? tokens.get(position++) : null;
    }

    boolean match(MyTinyTokenType type) {
        if (peek() != null && peek().getType() == type) { next(); return true; }
        return false;
    }
}
