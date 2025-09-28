package de.dmitrij.patuk.template;

import de.dmitrij.patuk.template.nodes.*;
import de.dmitrij.patuk.template.token.MyTinyTokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyTinyParser {

    //our entrypoint (no stop keywords)
    public MyTinyTemplate parseTemplate(MyTinyTokenStream stream) {
        return parseTemplate(stream, Set.of());
    }

    //we parse until one of stopKeywords is seen as the next keyword after "{{"
    private MyTinyTemplate parseTemplate(MyTinyTokenStream stream, Set<String> stopKeywords) {
        List<MyTinyTemplateNode> children = new ArrayList<>();
        while (true) {
            var cur = stream.peek();
            if (cur == null) {
                break;
            }

            // if next is "{{" and the following token is a stopping keyword we return
            if (cur.getType() == MyTinyTokenType.LBRACE2) {
                var nxt = stream.peekNext();
                if (nxt != null && nxt.getType() == MyTinyTokenType.KEYWORD && stopKeywords.contains(nxt.getToken())) {
                    // do not consume anything the caller will handle the closing directive
                    break;
                }
                //parse a normal expression/directiveoo
                children.add(parseExpressionOrDirective(stream));
                continue;
            }

            if (cur.getType() == MyTinyTokenType.TEXT) {
                children.add(new MyTinyTextNode(stream.next().getToken()));
                continue;
            }
            throw new RuntimeException("Unexpected token in template: " + cur);
        }
        return new MyTinyTemplate(children);
    }

    private MyTinyTemplateNode parseExpressionOrDirective(MyTinyTokenStream stream) {
        //we consume '{{'
        expectAndRemove(stream, MyTinyTokenType.LBRACE2);

        var next = stream.peek();
        if (next == null) throw new RuntimeException("Unexpected end of input after '{{'");

        if (next.getType() == MyTinyTokenType.KEYWORD) {
            String kw = next.getToken();
            if ("if".equals(kw)) {
                return parseIf(stream);       // parseIf assumes KEYWORD 'if' is next
            } else if ("foreach".equals(kw)) {
                return parseForeach(stream);  // parseForeach assumes KEYWORD 'foreach' is next
            } else {
                throw new RuntimeException("Unknown directive: " + kw);
            }
        } else {
            // expression like {{ user.name }} - parseExpression will consume the closing '}}'
            return parseExpression(stream);
        }
    }

    private MyTinyExpressionNode parseExpression(MyTinyTokenStream stream) {
        // parse property path (ident(.ident)*)
        List<MyTinyIdentifier> parts = new ArrayList<>();
        parts.add(parseIdentifier(stream));
        while (stream.peek() != null && stream.peek().getType() == MyTinyTokenType.DOT) {
            stream.next(); // consume DOT
            parts.add(parseIdentifier(stream));
        }
        // expression consumes the closing braces
        expectAndRemove(stream, MyTinyTokenType.RBRACE2);
        return new MyTinyExpressionNode(new MyTinyPropertyPath(parts));
    }

    private MyTinyIfNode parseIf(MyTinyTokenStream stream) {
        // the stream is currently at KEYWORD "if"
        expectAndRemove(stream, MyTinyTokenType.KEYWORD, "if");
        // we now parse condition expression - parseExpression will consume its closing '}}'
        MyTinyExpressionNode condition = parseExpression(stream);
        // we now parse then-branch until 'else' or 'endif'
        MyTinyTemplate thenBranch = parseTemplate(stream, Set.of("else", "endif"));

        MyTinyTemplate elseBranch = null;
        // now expect '{{' followed by either 'else' or 'endif'
        expectAndRemove(stream, MyTinyTokenType.LBRACE2);
        if (stream.peek() != null && stream.peek().getType() == MyTinyTokenType.KEYWORD && "else".equals(stream.peek().getToken())) {
            expectAndRemove(stream, MyTinyTokenType.KEYWORD, "else");
            expectAndRemove(stream, MyTinyTokenType.RBRACE2);
            elseBranch = parseTemplate(stream, Set.of("endif"));

            // after else body, we consume closing '{{ endif }}'
            expectAndRemove(stream, MyTinyTokenType.LBRACE2);
        }
        // now we must have 'endif'
        expectAndRemove(stream, MyTinyTokenType.KEYWORD, "endif");
        expectAndRemove(stream, MyTinyTokenType.RBRACE2);

        return new MyTinyIfNode(condition, thenBranch, elseBranch);
    }

    private MyTinyForeachNode parseForeach(MyTinyTokenStream stream) {
        // stream at KEYWORD "foreach"
        expectAndRemove(stream, MyTinyTokenType.KEYWORD, "foreach");
        var loopVariable = parseIdentifier(stream);
        expectAndRemove(stream, MyTinyTokenType.COLON);
        //we parse the expression it consumes its closing '}}'
        MyTinyExpressionNode collection = parseExpression(stream);

        //we have to parse the body until '{{ endforeach }}' appears
        MyTinyTemplate body = parseTemplate(stream, Set.of("endforeach"));

        //we now consume '{{ endforeach }}'
        expectAndRemove(stream, MyTinyTokenType.LBRACE2);
        expectAndRemove(stream, MyTinyTokenType.KEYWORD, "endforeach");
        expectAndRemove(stream, MyTinyTokenType.RBRACE2);

        return new MyTinyForeachNode(loopVariable, collection, body);
    }

    private void expectAndRemove(MyTinyTokenStream stream, MyTinyTokenType expected) {
        if (stream.peek() == null) {
            throw new RuntimeException("Expected: <" + expected.name() + "> but got null!");
        }
        if (stream.peek().getType() == expected) {
            stream.next();
            return;
        }
        throw new RuntimeException("Expected: <" + expected.name() + "> but got <" + stream.peek().getType().name() + "> !");
    }

    private void expectAndRemove(MyTinyTokenStream stream, MyTinyTokenType expected, String keyWord) {
        if (stream.peek() == null) {
            throw new RuntimeException("Expected: <" + expected.name() + "> keyword: <" + keyWord + "> but got null!");
        }
        if (stream.peek().getType() == expected && stream.peek().getToken().equals(keyWord)) {
            stream.next();
            return;
        }
        throw new RuntimeException("Expected: " + expected.name() + "keyword: " + keyWord + "but got " + stream.peek().getType().name() + "!");
    }


    private MyTinyIdentifier parseIdentifier(MyTinyTokenStream stream) {
        if (stream.peek() == null) {
            throw new RuntimeException("Streams next token was null!");
        }
        var token = stream.next();
        if (token.getType() == MyTinyTokenType.IDENTIFIER) {
            return new MyTinyIdentifier(token.getToken());
        }
        throw new RuntimeException("Parsing identifier, unexpected token type: " + token.getType());
    }
}
