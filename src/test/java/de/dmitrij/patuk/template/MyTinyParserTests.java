package de.dmitrij.patuk.template;

import de.dmitrij.patuk.template.nodes.*;
import de.dmitrij.patuk.template.token.MyTinyToken;
import de.dmitrij.patuk.template.token.MyTinyTokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyTinyParserTests {

    @Test
    void parseTextExpressionTest() {
        var input = List.of(new MyTinyToken("test", MyTinyTokenType.TEXT));
        MyTinyParser parser = new MyTinyParser();
        var expected = new MyTinyTemplate(List.of(new MyTinyTextNode("test")));
        var result = parser.parseTemplate(new MyTinyTokenStream(input));
        assertTrue(expected.equals(result));
    }


    @Test
    void parseTextExpressionWithColonTest() {
        var input = List.of(new MyTinyToken("te:st", MyTinyTokenType.TEXT));
        MyTinyParser parser = new MyTinyParser();
        var expected = new MyTinyTemplate(List.of(new MyTinyTextNode("te:st")));
        var result = parser.parseTemplate(new MyTinyTokenStream(input));
        assertTrue(expected.equals(result));
    }

    @Test
    void parseTextExpressionWithLbrace2Test() {
        var input = List.of(
                new MyTinyToken("te", MyTinyTokenType.TEXT),
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("st", MyTinyTokenType.IDENTIFIER)
        );
        MyTinyParser parser = new MyTinyParser();
        assertThrows(RuntimeException.class, () -> parser.parseTemplate(new MyTinyTokenStream(input)),
                "Expected: <RBRACE2> but got null!");
    }

    @Test
    void parseIdentifierExpressionTest() {
        var input = List.of
                (new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2));
        MyTinyParser parser = new MyTinyParser();
        var expected = new MyTinyTemplate(List.of(new MyTinyExpressionNode(new MyTinyPropertyPath(List.of(new MyTinyIdentifier("te"))))));
        var result = parser.parseTemplate(new MyTinyTokenStream(input));
        assertTrue(expected.equals(result));
    }

    @Test
    void parsePropertyPathExpressionTest() {
        var input = List.of
                (new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2));
        MyTinyParser parser = new MyTinyParser();
        var expected = new MyTinyTemplate(List.of(
                new MyTinyExpressionNode(
                        new MyTinyPropertyPath(
                                List.of(
                                        new MyTinyIdentifier("te"),
                                        new MyTinyIdentifier("te"),
                                        new MyTinyIdentifier("te"))))));
        var result = parser.parseTemplate(new MyTinyTokenStream(input));
        assertTrue(expected.equals(result));
    }

    @Test
    void parseForeachExpressionThrowsExpectAndRemoveLBraceTest() {
        var input = List.of
                (new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(":", MyTinyTokenType.COLON),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2));
        MyTinyParser parser = new MyTinyParser();
        assertThrows(RuntimeException.class, () -> parser.parseTemplate(new MyTinyTokenStream(input)), "Expected: <LBRACE2> but got null!");
    }

    @Test
    void parseForeachExpressionThrowsExpectAndRemoveKeywordTest() {
        var input = List.of
                (
                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(":", MyTinyTokenType.COLON),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),
                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2)
                );

        MyTinyParser parser = new MyTinyParser();
        assertThrows(RuntimeException.class, () ->
                parser.parseTemplate(new MyTinyTokenStream(input)), "Expected: <KEYWORD> or <EXPRESSION> but got null!");
    }

    @Test
    void parseForeachExpressionThrowsExpectAndRemoveRbraceTest() {
        var input = List.of
                (
                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(":", MyTinyTokenType.COLON),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),
                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("foreach", MyTinyTokenType.KEYWORD)
                );
        MyTinyParser parser = new MyTinyParser();
        assertThrows(RuntimeException.class, () ->
                parser.parseTemplate(new MyTinyTokenStream(input)), "Streams next token was null!");
    }

    @Test
    void parseForeachExpressionNoBodyTemplateTest() {
        var input = List.of
                (
                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(":", MyTinyTokenType.COLON),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),
                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("endforeach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2)
                );
        var expected = new MyTinyTemplate(
                List.of(
                        new MyTinyForeachNode(
                                new MyTinyIdentifier("te"),
                                new MyTinyExpressionNode(
                                        new MyTinyPropertyPath(List.of(
                                                new MyTinyIdentifier("te"),
                                                new MyTinyIdentifier("te")))), new MyTinyTemplate(List.of()))));
        MyTinyParser parser = new MyTinyParser();
        var result = parser.parseTemplate(new MyTinyTokenStream(input));
        assertTrue(expected.equals(result));
    }


    @Test
    void parseForeachExpressionIdentifierBodyTemplateTest() {
        var input = List.of
                (
                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(":", MyTinyTokenType.COLON),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("endforeach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2)
                );
        var expected = new MyTinyTemplate(
                List.of(
                        new MyTinyForeachNode(
                                new MyTinyIdentifier("te"),
                                new MyTinyExpressionNode(
                                        new MyTinyPropertyPath(List.of(
                                                new MyTinyIdentifier("te"),
                                                new MyTinyIdentifier("te")))), new MyTinyTemplate(
                                List.of(
                                        new MyTinyExpressionNode(new MyTinyPropertyPath(List.of(new MyTinyIdentifier("te"))))
                                )
                        ))));
        MyTinyParser parser = new MyTinyParser();
        var result = parser.parseTemplate(new MyTinyTokenStream(input));
        System.out.println( result.prettyPrint(""));
        assertTrue(expected.equals(result));
    }

    @Test
    void parseForeachExpressionPropertyPathBodyTemplateTest() {
        var input = List.of
                (
                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(":", MyTinyTokenType.COLON),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("endforeach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2)
                );
        var expected = new MyTinyTemplate(
                List.of(
                        new MyTinyForeachNode(
                                new MyTinyIdentifier("te"),
                                new MyTinyExpressionNode(
                                        new MyTinyPropertyPath(List.of(
                                                new MyTinyIdentifier("te"),
                                                new MyTinyIdentifier("te")))), new MyTinyTemplate(
                                List.of(
                                        new MyTinyExpressionNode(new MyTinyPropertyPath(List.of(
                                                new MyTinyIdentifier("te"),
                                                new MyTinyIdentifier("te"),
                                                new MyTinyIdentifier("te"))))
                                )
                        ))));
        MyTinyParser parser = new MyTinyParser();
        var result = parser.parseTemplate(new MyTinyTokenStream(input));
        System.out.println( result.prettyPrint(""));
        assertTrue(expected.equals(result));
    }


    @Test
    void parseForeachExpressionPropertyPathBodyAndForeachTemplateTest() {
        var input = List.of
                (
                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(":", MyTinyTokenType.COLON),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("te", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("ge", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(":", MyTinyTokenType.COLON),
                        new MyTinyToken("ge", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken(".", MyTinyTokenType.DOT),
                        new MyTinyToken("ge", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("endforeach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("endforeach", MyTinyTokenType.KEYWORD),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2)
                );
        var expected = new MyTinyTemplate(
                List.of(
                        new MyTinyForeachNode(
                                new MyTinyIdentifier("te"),
                                new MyTinyExpressionNode(
                                        new MyTinyPropertyPath(List.of(
                                                new MyTinyIdentifier("te"),
                                                new MyTinyIdentifier("te")))), new MyTinyTemplate(
                                List.of(
                                        new MyTinyExpressionNode(new MyTinyPropertyPath(List.of(
                                                new MyTinyIdentifier("te"),
                                                new MyTinyIdentifier("te"),
                                                new MyTinyIdentifier("te")))),
                                        new MyTinyForeachNode(
                                                new MyTinyIdentifier("ge"),
                                                new MyTinyExpressionNode(new MyTinyPropertyPath(List.of(
                                                        new MyTinyIdentifier("ge"),
                                                        new MyTinyIdentifier("ge")))),
                                                new MyTinyTemplate(List.of())
                                        )
                                )))));
        MyTinyParser parser = new MyTinyParser();
        var result = parser.parseTemplate(new MyTinyTokenStream(input));
        System.out.println( result.prettyPrint(""));
        assertTrue(expected.equals(result));
    }

    @Test
    void parseIfExpressionThenElseTemplateTest() {
        var input = List.of(
                // {{ if cond.path }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("if", MyTinyTokenType.KEYWORD),
                new MyTinyToken("cond", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken(".", MyTinyTokenType.DOT),
                new MyTinyToken("path", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                // then branch: {{ user.name }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("user", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken(".", MyTinyTokenType.DOT),
                new MyTinyToken("name", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                // {{ else }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("else", MyTinyTokenType.KEYWORD),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                // else branch: {{ fallback }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("fallback", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                // {{ endif }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("endif", MyTinyTokenType.KEYWORD),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2)
        );

        var expected = new MyTinyTemplate(List.of(
                new MyTinyIfNode(
                        // condition
                        new MyTinyExpressionNode(
                                new MyTinyPropertyPath(List.of(
                                        new MyTinyIdentifier("cond"),
                                        new MyTinyIdentifier("path")
                                ))
                        ),
                        // then branch
                        new MyTinyTemplate(List.of(
                                new MyTinyExpressionNode(
                                        new MyTinyPropertyPath(List.of(
                                                new MyTinyIdentifier("user"),
                                                new MyTinyIdentifier("name")
                                        ))
                                )
                        )),
                        // else branch
                        new MyTinyTemplate(List.of(
                                new MyTinyExpressionNode(
                                        new MyTinyPropertyPath(List.of(
                                                new MyTinyIdentifier("fallback")
                                        ))
                                )
                        ))
                )
        ));

        MyTinyParser parser = new MyTinyParser();
        var result = parser.parseTemplate(new MyTinyTokenStream(input));

        System.out.println(result.prettyPrint(""));
        assertTrue(expected.equals(result));
    }
    @Test
    void parseForeachWithIfElseBodyTest() {
        var input = List.of(
                // {{ foreach item : items.list }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("foreach", MyTinyTokenType.KEYWORD),
                new MyTinyToken("item", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken(":", MyTinyTokenType.COLON),
                new MyTinyToken("items", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken(".", MyTinyTokenType.DOT),
                new MyTinyToken("list", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                // {{ if item.active }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("if", MyTinyTokenType.KEYWORD),
                new MyTinyToken("item", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken(".", MyTinyTokenType.DOT),
                new MyTinyToken("active", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                // then branch: {{ item.name }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("item", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken(".", MyTinyTokenType.DOT),
                new MyTinyToken("name", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                // {{ else }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("else", MyTinyTokenType.KEYWORD),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                // else branch: {{ fallback }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("fallback", MyTinyTokenType.IDENTIFIER),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                // {{ endif }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("endif", MyTinyTokenType.KEYWORD),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2),

                // {{ endforeach }}
                new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                new MyTinyToken("endforeach", MyTinyTokenType.KEYWORD),
                new MyTinyToken("}}", MyTinyTokenType.RBRACE2)
        );

        var expected = new MyTinyTemplate(List.of(
                new MyTinyForeachNode(
                        new MyTinyIdentifier("item"),
                        new MyTinyExpressionNode(new MyTinyPropertyPath(List.of(
                                new MyTinyIdentifier("items"),
                                new MyTinyIdentifier("list")
                        ))),
                        new MyTinyTemplate(List.of(
                                new MyTinyIfNode(
                                        // condition
                                        new MyTinyExpressionNode(new MyTinyPropertyPath(List.of(
                                                new MyTinyIdentifier("item"),
                                                new MyTinyIdentifier("active")
                                        ))),
                                        // then branch
                                        new MyTinyTemplate(List.of(
                                                new MyTinyExpressionNode(new MyTinyPropertyPath(List.of(
                                                        new MyTinyIdentifier("item"),
                                                        new MyTinyIdentifier("name")
                                                )))
                                        )),
                                        // else branch
                                        new MyTinyTemplate(List.of(
                                                new MyTinyExpressionNode(new MyTinyPropertyPath(List.of(
                                                        new MyTinyIdentifier("fallback")
                                                )))
                                        ))
                                )
                        ))
                )
        ));

        MyTinyParser parser = new MyTinyParser();
        var result = parser.parseTemplate(new MyTinyTokenStream(input));

        System.out.println(result.prettyPrint(""));
        assertTrue(expected.equals(result));
    }

}
