# Building the MyTinyParser 

Now that we have our tokenizer working and can convert template text into tokens, 
we need to build a parser that can take these tokens and create an Abstract Syntax Tree (AST) that represents our template structure.

## 1. Understanding the Parser's Role

The parser takes the flat list of tokens from our tokenizer and builds a tree structure that represents the logical structure of our template. For example:

**Input tokens:** `{{`, `if`, `user`, `.`, `name`, `}}`, `Hello`, `{{`, `endif`, `}}`

**Output AST:** An `IfNode` containing:
- Condition: `ExpressionNode` with property path `user.name`
- Then branch: `Template` with `TextNode("Hello")`
- Else branch: null

## 2. Setting Up the Foundation Classes

Before we can build the parser, we need the AST node classes and a token stream helper.

### The MyTinyTokenStream Class

First, let's create a helper class that makes it easier to work with our tokens:

```java
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
        return position < tokens.size() ? tokens.get(position) : null;
    }

    MyTinyToken peekNext() {
        return (position + 1) < tokens.size() ? tokens.get(position + 1) : null;
    }

    MyTinyToken next() {
        return position < tokens.size() ? tokens.get(position++) : null;
    }

    boolean hasNext() {
        return position < tokens.size();
    }
}
```

### The Base Node Interface

All our AST nodes will implement this interface:

```java
package de.dmitrij.patuk.template.nodes;

import de.dmitrij.patuk.template.MyTinyContext;

public interface MyTinyTemplateNode {
    String render(MyTinyContext context);
    String prettyPrint(String indent);
}
```

## 3. Building the Parser - Starting Simple

Let's start with the basic parser structure and gradually add to it:

```java
package de.dmitrij.patuk.template;

import de.dmitrij.patuk.template.nodes.*;
import de.dmitrij.patuk.template.token.MyTinyTokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyTinyParser {

    // Our main entry point
    public MyTinyTemplate parseTemplate(MyTinyTokenStream stream) {
        return parseTemplate(stream, Set.of());
    }

    // Internal method that can handle stop keywords
    private MyTinyTemplate parseTemplate(MyTinyTokenStream stream, Set<String> stopKeywords) {
        List<MyTinyTemplateNode> children = new ArrayList<>();
        
        return new MyTinyTemplate(children);
    }
}
```

## 4. Parsing Text Nodes - The Simple Case

Let's add our first parsing - handling plain text:

```java
private MyTinyTemplate parseTemplate(MyTinyTokenStream stream, Set<String> stopKeywords) {
    List<MyTinyTemplateNode> children = new ArrayList<>();
    
    while (true) {
        var current = stream.peek();
        if (current == null) {
            break; 
        }

        if (current.getType() == MyTinyTokenType.TEXT) {
            children.add(new MyTinyTextNode(stream.next().getToken()));
            continue;
        }
        throw new RuntimeException("Unexpected token: " + current);
    }
    return new MyTinyTemplate(children);
}
```

### Tests for Text Parsing

```java
@Test
void testParseSimpleText() {
    MyTinyTokenizer tokenizer = new MyTinyTokenizer();
    MyTinyParser parser = new MyTinyParser();
    
    List<MyTinyToken> tokens = tokenizer.tokenize("Hello World");
    MyTinyTokenStream stream = new MyTinyTokenStream(tokens);
    MyTinyTemplate template = parser.parseTemplate(stream);
    
    assertEquals(1, template.getNodes().size());
    assertTrue(template.getNodes().get(0) instanceof MyTinyTextNode);
    assertEquals("Hello World", ((MyTinyTextNode) template.getNodes().get(0)).getText());
}
```

## 5. Parsing Expressions

Now let's handle simple expressions like `{{ user.name }}`:

```java
private MyTinyTemplate parseTemplate(MyTinyTokenStream stream, Set<String> stopKeywords) {
    List<MyTinyTemplateNode> children = new ArrayList<>();
    
    while (true) {
        var current = stream.peek();
        if (current == null) {
            break;
        }

        if (current.getType() == MyTinyTokenType.LBRACE2) {
            var next = stream.peekNext();
            if (next != null && next.getType() == MyTinyTokenType.KEYWORD && stopKeywords.contains(next.getToken())) {
                break; 
            }
            children.add(parseExpressionOrDirective(stream));
            continue;
        }

        if (current.getType() == MyTinyTokenType.TEXT) {
            children.add(new MyTinyTextNode(stream.next().getToken()));
            continue;
        }
        throw new RuntimeException("Unexpected token: " + current);
    }
    return new MyTinyTemplate(children);
}

private MyTinyTemplateNode parseExpressionOrDirective(MyTinyTokenStream stream) {
    expectAndRemove(stream, MyTinyTokenType.LBRACE2);
    var next = stream.peek();
    if (next == null) {
        throw new RuntimeException("Unexpected end after '{{'");
    }
    if (next.getType() == MyTinyTokenType.KEYWORD) {
        throw new RuntimeException("Directives not implemented yet");
    } else {
        return parseExpression(stream);
    }
}

private MyTinyExpressionNode parseExpression(MyTinyTokenStream stream) {
    // Parse property path (identifier.identifier.identifier...)
    List<MyTinyIdentifier> parts = new ArrayList<>();
    parts.add(parseIdentifier(stream));
    
    while (stream.peek() != null && stream.peek().getType() == MyTinyTokenType.DOT) {
        stream.next(); 
        parts.add(parseIdentifier(stream));
    }
    
    expectAndRemove(stream, MyTinyTokenType.RBRACE2);
    return new MyTinyExpressionNode(new MyTinyPropertyPath(parts));
}

private MyTinyIdentifier parseIdentifier(MyTinyTokenStream stream) {
    if (stream.peek() == null) {
        throw new RuntimeException("Expected identifier but got null");
    }
    
    var token = stream.next();
    if (token.getType() != MyTinyTokenType.IDENTIFIER) {
        throw new RuntimeException("Expected identifier but got: " + token.getType());
    }
    
    return new MyTinyIdentifier(token.getToken());
}

private void expectAndRemove(MyTinyTokenStream stream, MyTinyTokenType expected) {
    if (stream.peek() == null) {
        throw new RuntimeException("Expected " + expected + " but got null");
    }
    if (stream.peek().getType() != expected) {
        throw new RuntimeException("Expected " + expected + " but got " + stream.peek().getType());
    }
    stream.next();
}
```

### Tests for Expression Parsing

```java
@Test
void testParseSimpleExpression() {
    MyTinyTokenizer tokenizer = new MyTinyTokenizer();
    MyTinyParser parser = new MyTinyParser();
    
    List<MyTinyToken> tokens = tokenizer.tokenize("{{ name }}");
    MyTinyTokenStream stream = new MyTinyTokenStream(tokens);
    MyTinyTemplate template = parser.parseTemplate(stream);
    
    assertEquals(1, template.getNodes().size());
    assertTrue(template.getNodes().get(0) instanceof MyTinyExpressionNode);
    
    MyTinyExpressionNode expr = (MyTinyExpressionNode) template.getNodes().get(0);
    assertEquals(1, expr.getPropertyPath().getParts().size());
    assertEquals("name", expr.getPropertyPath().getParts().get(0).getName());
}

@Test
void testParsePropertyPath() {
    MyTinyTokenizer tokenizer = new MyTinyTokenizer();
    MyTinyParser parser = new MyTinyParser();
    
    List<MyTinyToken> tokens = tokenizer.tokenize("{{ user.profile.name }}");
    MyTinyTokenStream stream = new MyTinyTokenStream(tokens);
    MyTinyTemplate template = parser.parseTemplate(stream);
    
    MyTinyExpressionNode expr = (MyTinyExpressionNode) template.getNodes().get(0);
    assertEquals(3, expr.getPropertyPath().getParts().size());
    assertEquals("user", expr.getPropertyPath().getParts().get(0).getName());
    assertEquals("profile", expr.getPropertyPath().getParts().get(1).getName());
    assertEquals("name", expr.getPropertyPath().getParts().get(2).getName());
}
```

## 6. Parsing If Directives - Conditional Logic

Now let's take care of the more complex if statements:

```java
private MyTinyTemplateNode parseExpressionOrDirective(MyTinyTokenStream stream) {
    expectAndRemove(stream, MyTinyTokenType.LBRACE2);
    
    var next = stream.peek();
    if (next == null) {
        throw new RuntimeException("Unexpected end after '{{'");
    }

    if (next.getType() == MyTinyTokenType.KEYWORD) {
        String keyword = next.getToken();
        if ("if".equals(keyword)) {
            return parseIf(stream);
        } else if ("foreach".equals(keyword)) {
            return parseForeach(stream);
        } else {
            throw new RuntimeException("Unknown directive: " + keyword);
        }
    } else {
        return parseExpression(stream);
    }
}

private MyTinyIfNode parseIf(MyTinyTokenStream stream) {
    expectAndRemove(stream, MyTinyTokenType.KEYWORD, "if");
    MyTinyExpressionNode condition = parseExpression(stream);
    MyTinyTemplate thenBranch = parseTemplate(stream, Set.of("else", "endif"));
    MyTinyTemplate elseBranch = null;
    
    expectAndRemove(stream, MyTinyTokenType.LBRACE2);
    if (stream.peek() != null && stream.peek().getType() == MyTinyTokenType.KEYWORD && "else".equals(stream.peek().getToken())) {
        expectAndRemove(stream, MyTinyTokenType.KEYWORD, "else");
        expectAndRemove(stream, MyTinyTokenType.RBRACE2);
        elseBranch = parseTemplate(stream, Set.of("endif"));
        expectAndRemove(stream, MyTinyTokenType.LBRACE2);
    }
    
    expectAndRemove(stream, MyTinyTokenType.KEYWORD, "endif");
    expectAndRemove(stream, MyTinyTokenType.RBRACE2);
    
    return new MyTinyIfNode(condition, thenBranch, elseBranch);
}

private void expectAndRemove(MyTinyTokenStream stream, MyTinyTokenType expected, String keyword) {
    if (stream.peek() == null) {
        throw new RuntimeException("Expected " + expected + " with keyword '" + keyword + "' but got null");
    }
    var token = stream.peek();
    if (token.getType() != expected || !keyword.equals(token.getToken())) {
        throw new RuntimeException("Expected " + expected + " with keyword '" + keyword + "' but got " + token);
    }
    stream.next();
}
```

### Tests for If Parsing

```java
@Test
void testParseSimpleIf() {
    MyTinyTokenizer tokenizer = new MyTinyTokenizer();
    MyTinyParser parser = new MyTinyParser();
    
    String input = "{{ if user.isActive }}Welcome!{{ endif }}";
    List<MyTinyToken> tokens = tokenizer.tokenize(input);
    MyTinyTokenStream stream = new MyTinyTokenStream(tokens);
    MyTinyTemplate template = parser.parseTemplate(stream);
    
    assertEquals(1, template.getNodes().size());
    assertTrue(template.getNodes().get(0) instanceof MyTinyIfNode);
    
    MyTinyIfNode ifNode = (MyTinyIfNode) template.getNodes().get(0);
    assertNotNull(ifNode.getCondition());
    assertNotNull(ifNode.getThenBranch());
    assertNull(ifNode.getElseBranch());
}

@Test
void testParseIfWithElse() {
    MyTinyTokenizer tokenizer = new MyTinyTokenizer();
    MyTinyParser parser = new MyTinyParser();
    
    String input = "{{ if user.isActive }}Welcome!{{ else }}Please log in{{ endif }}";
    List<MyTinyToken> tokens = tokenizer.tokenize(input);
    MyTinyTokenStream stream = new MyTinyTokenStream(tokens);
    MyTinyTemplate template = parser.parseTemplate(stream);
    
    MyTinyIfNode ifNode = (MyTinyIfNode) template.getNodes().get(0);
    assertNotNull(ifNode.getElseBranch());
    assertEquals(1, ifNode.getElseBranch().getNodes().size());
}
```

## 7. Parsing Foreach Directives - Iteration Logic

Now we need to implement the foreach parsing logic.

```java
private MyTinyForeachNode parseForeach(MyTinyTokenStream stream) {
    expectAndRemove(stream, MyTinyTokenType.KEYWORD, "foreach");
    MyTinyIdentifier loopVariable = parseIdentifier(stream);
    expectAndRemove(stream, MyTinyTokenType.COLON);
    MyTinyExpressionNode collection = parseExpression(stream);
    MyTinyTemplate body = parseTemplate(stream, Set.of("endforeach"));
    expectAndRemove(stream, MyTinyTokenType.LBRACE2);
    expectAndRemove(stream, MyTinyTokenType.KEYWORD, "endforeach");
    expectAndRemove(stream, MyTinyTokenType.RBRACE2);
    
    return new MyTinyForeachNode(loopVariable, collection, body);
}
```

### Tests for Foreach Parsing

```java
@Test
void testParseForeach() {
    MyTinyTokenizer tokenizer = new MyTinyTokenizer();
    MyTinyParser parser = new MyTinyParser();
    
    String input = "{{ foreach person : people }}{{ person.name }}{{ endforeach }}";
    List<MyTinyToken> tokens = tokenizer.tokenize(input);
    MyTinyTokenStream stream = new MyTinyTokenStream(tokens);
    MyTinyTemplate template = parser.parseTemplate(stream);
    
    assertEquals(1, template.getNodes().size());
    assertTrue(template.getNodes().get(0) instanceof MyTinyForeachNode);
    
    MyTinyForeachNode foreachNode = (MyTinyForeachNode) template.getNodes().get(0);
    assertEquals("person", foreachNode.getLoopVariable().getName());
    assertNotNull(foreachNode.getCollection());
    assertNotNull(foreachNode.getBody());
}
```

## 8. Complete Parser Implementation

Here's our final, complete parser:

```java
package de.dmitrij.patuk.template;

import de.dmitrij.patuk.template.nodes.*;
import de.dmitrij.patuk.template.token.MyTinyTokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MyTinyParser {

    public MyTinyTemplate parseTemplate(MyTinyTokenStream stream) {
        return parseTemplate(stream, Set.of());
    }

    private MyTinyTemplate parseTemplate(MyTinyTokenStream stream, Set<String> stopKeywords) {
        List<MyTinyTemplateNode> children = new ArrayList<>();
        
        while (true) {
            var current = stream.peek();
            if (current == null) {
                break;
            }

            if (current.getType() == MyTinyTokenType.LBRACE2) {
                var next = stream.peekNext();
                if (next != null && next.getType() == MyTinyTokenType.KEYWORD && stopKeywords.contains(next.getToken())) {
                    break;
                }
                children.add(parseExpressionOrDirective(stream));
                continue;
            }

            if (current.getType() == MyTinyTokenType.TEXT) {
                children.add(new MyTinyTextNode(stream.next().getToken()));
                continue;
            }
            
            throw new RuntimeException("Unexpected token: " + current);
        }
        
        return new MyTinyTemplate(children);
    }

    private MyTinyTemplateNode parseExpressionOrDirective(MyTinyTokenStream stream) {
        expectAndRemove(stream, MyTinyTokenType.LBRACE2);
        
        var next = stream.peek();
        if (next == null) {
            throw new RuntimeException("Unexpected end after '{{'");
        }

        if (next.getType() == MyTinyTokenType.KEYWORD) {
            String keyword = next.getToken();
            if ("if".equals(keyword)) {
                return parseIf(stream);
            } else if ("foreach".equals(keyword)) {
                return parseForeach(stream);
            } else {
                throw new RuntimeException("Unknown directive: " + keyword);
            }
        } else {
            return parseExpression(stream);
        }
    }

    private MyTinyExpressionNode parseExpression(MyTinyTokenStream stream) {
        List<MyTinyIdentifier> parts = new ArrayList<>();
        parts.add(parseIdentifier(stream));
        
        while (stream.peek() != null && stream.peek().getType() == MyTinyTokenType.DOT) {
            stream.next();
            parts.add(parseIdentifier(stream));
        }
        
        expectAndRemove(stream, MyTinyTokenType.RBRACE2);
        return new MyTinyExpressionNode(new MyTinyPropertyPath(parts));
    }

    private MyTinyIfNode parseIf(MyTinyTokenStream stream) {
        expectAndRemove(stream, MyTinyTokenType.KEYWORD, "if");
        MyTinyExpressionNode condition = parseExpression(stream);
        MyTinyTemplate thenBranch = parseTemplate(stream, Set.of("else", "endif"));

        MyTinyTemplate elseBranch = null;
        expectAndRemove(stream, MyTinyTokenType.LBRACE2);
        
        if (stream.peek() != null && stream.peek().getType() == MyTinyTokenType.KEYWORD && "else".equals(stream.peek().getToken())) {
            expectAndRemove(stream, MyTinyTokenType.KEYWORD, "else");
            expectAndRemove(stream, MyTinyTokenType.RBRACE2);
            elseBranch = parseTemplate(stream, Set.of("endif"));
            expectAndRemove(stream, MyTinyTokenType.LBRACE2);
        }
        
        expectAndRemove(stream, MyTinyTokenType.KEYWORD, "endif");
        expectAndRemove(stream, MyTinyTokenType.RBRACE2);

        return new MyTinyIfNode(condition, thenBranch, elseBranch);
    }

    private MyTinyForeachNode parseForeach(MyTinyTokenStream stream) {
        expectAndRemove(stream, MyTinyTokenType.KEYWORD, "foreach");
        MyTinyIdentifier loopVariable = parseIdentifier(stream);
        expectAndRemove(stream, MyTinyTokenType.COLON);
        MyTinyExpressionNode collection = parseExpression(stream);
        MyTinyTemplate body = parseTemplate(stream, Set.of("endforeach"));

        expectAndRemove(stream, MyTinyTokenType.LBRACE2);
        expectAndRemove(stream, MyTinyTokenType.KEYWORD, "endforeach");
        expectAndRemove(stream, MyTinyTokenType.RBRACE2);

        return new MyTinyForeachNode(loopVariable, collection, body);
    }

    private void expectAndRemove(MyTinyTokenStream stream, MyTinyTokenType expected) {
        if (stream.peek() == null) {
            throw new RuntimeException("Expected " + expected + " but got null");
        }
        if (stream.peek().getType() != expected) {
            throw new RuntimeException("Expected " + expected + " but got " + stream.peek().getType());
        }
        stream.next();
    }

    private void expectAndRemove(MyTinyTokenStream stream, MyTinyTokenType expected, String keyword) {
        if (stream.peek() == null) {
            throw new RuntimeException("Expected " + expected + " with keyword '" + keyword + "' but got null");
        }
        var token = stream.peek();
        if (token.getType() != expected || !keyword.equals(token.getToken())) {
            throw new RuntimeException("Expected " + expected + " with keyword '" + keyword + "' but got " + token);
        }
        stream.next();
    }

    private MyTinyIdentifier parseIdentifier(MyTinyTokenStream stream) {
        if (stream.peek() == null) {
            throw new RuntimeException("Expected identifier but got null");
        }
        
        var token = stream.next();
        if (token.getType() != MyTinyTokenType.IDENTIFIER) {
            throw new RuntimeException("Expected identifier but got: " + token.getType());
        }
        
        return new MyTinyIdentifier(token.getToken());
    }
}
```