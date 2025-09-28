package de.dmitrij.patuk.template;

import de.dmitrij.patuk.template.token.MyTinyToken;
import de.dmitrij.patuk.template.token.MyTinyTokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MyTinyTokenizerTests {
    @Test
    public void textOnlyTest() {
        var input = "test";
        var expected = List.of(new MyTinyToken("test", MyTinyTokenType.TEXT));
        var tokenizer = new MyTinyTokenizer();
        var result = tokenizer.tokenize(input);
        assertTrue(result.equals(expected));
    }

    @Test
    public void lBraceOnlyTest() {
        var input = "{{";
        var expected = List.of(new MyTinyToken("{{", MyTinyTokenType.LBRACE2));
        var tokenizer = new MyTinyTokenizer();
        var result = tokenizer.tokenize(input);
        assertTrue(result.equals(expected));
    }

    @Test
    public void rBraceOnlyTest() {
        var input = "}}";
        var expected = List.of(new MyTinyToken("}}", MyTinyTokenType.RBRACE2));
        var tokenizer = new MyTinyTokenizer();
        var result = tokenizer.tokenize(input);
        assertTrue(result.equals(expected));
    }

    @Test
    public void rLBraceIdentifierDotIdentifierRBraceOnlyTest() {
        var input = "{{ model.key }}";
        var expected = List.of(new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("model", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken(".", MyTinyTokenType.DOT),
                new MyTinyToken("key", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2));
        var tokenizer = new MyTinyTokenizer();
        var result = tokenizer.tokenize(input);
        assertTrue(result.equals(expected));
    }

    @Test
    public void rBraceTextTest() {
        var input = "}";
        var expected = List.of(new MyTinyToken("}", MyTinyTokenType.TEXT));
        var tokenizer = new MyTinyTokenizer();
        var result = tokenizer.tokenize(input);
        assertTrue(result.equals(expected));
    }

    @Test
    public void textTextLbraceIdentifierRbraceTextTest() {
        var input = "test{{hallo}}test2";
        var expected = List.of(
                new MyTinyToken("test", MyTinyTokenType.TEXT),
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("hallo", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),
                new MyTinyToken("test2", MyTinyTokenType.TEXT));
        var tokenizer = new MyTinyTokenizer();
        var result = tokenizer.tokenize(input);
        assertTrue(result.equals(expected));
    }

    @Test
    public void textTextKeywordRbraceTextTest() {
        var input = "test{{foreach}}test2";
        var expected = List.of(
                new MyTinyToken("test", MyTinyTokenType.TEXT),
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),
                new MyTinyToken("test2", MyTinyTokenType.TEXT));
        var tokenizer = new MyTinyTokenizer();
        var result = tokenizer.tokenize(input);
        assertTrue(result.equals(expected));
    }

    @Test
    public void textTextKeywordIdentifierRbraceTextTest() {
        var input = "test{{foreach item}}test2";
        var expected = List.of(
                new MyTinyToken("test", MyTinyTokenType.TEXT),
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                new MyTinyToken("item", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),
                new MyTinyToken("test2", MyTinyTokenType.TEXT));
        var tokenizer = new MyTinyTokenizer();
        var result = tokenizer.tokenize(input);
        assertTrue(result.equals(expected));
    }

    @Test
    public void textTextKeywordIdentifierColonIdentifierRbraceTextTest() {
        var input = "test{{foreach item:item2}}test2";
        var expected = List.of(
                new MyTinyToken("test", MyTinyTokenType.TEXT),
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                new MyTinyToken("item", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken(":", MyTinyTokenType.COLON),
                new MyTinyToken("item2", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),
                new MyTinyToken("test2", MyTinyTokenType.TEXT));
        var tokenizer = new MyTinyTokenizer();
        var result = tokenizer.tokenize(input);
        assertTrue(result.equals(expected));
    }

    @Test
    public void textTextKeywordIdentifierColonIdentifierRbraceTextLBraceEndForeachRbraceTest() {
        var input = "test{{foreach item:item2}}test2{{endforeach}}";
        var expected = List.of(
                new MyTinyToken("test", MyTinyTokenType.TEXT),
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                new MyTinyToken("item", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken(":", MyTinyTokenType.COLON),
                new MyTinyToken("item2", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),
                new MyTinyToken("test2", MyTinyTokenType.TEXT),
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("endforeach", MyTinyTokenType.KEYWORD),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2)
                );

        var tokenizer = new MyTinyTokenizer();
        var result = tokenizer.tokenize(input);
        assertTrue(result.equals(expected));
    }

}
