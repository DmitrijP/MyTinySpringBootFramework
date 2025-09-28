# MyTinyTemplateEngine

We will create a template engine like `Thymeleaf` in this readme and include it at the end into our framework. 
The tutorials for this part are in this playlist https://www.youtube.com/playlist?list=PLtFURTtAiZImLQxLOQvQydR5gdAHJ5z5O

## 1. our own template language

First we need to define our template language. I want to support `If` and `Foreach` statements as well as `identifiers` and `recursion`.
That is why i came up with this example that shows what should be possible with our language.
```html
<html lang="de">
<body>
    <h1>{{ model.title }}</h1>
    <h2>{{ model.subTitle }}</h2>
    {{ if model.show }} <p>This will be shown.</p> {{ else }} <p>This will not be shown.</p> {{ endif }}
    <table>
        <thead>
            <tr>
                <th>Id</th>
                <th>Name</th>
                <th>Surname</th>
                <th></th>
            </tr>
        </thead>
        <tbody>
            {{ foreach person : model.people }}
            <tr>
                <td>{{ person.id }}</td>
                <td>{{ person.name }}</td>
                <td>{{ person.surname }}</td>
                {{ if person.canDelete }}
                <td>
                    <a href="{{ person.deleteUrl }}">Delete</a>
                </td>
                {{ else }}
                <td>
                    <a href="#" disabled>Delete</a>
                </td>
                {{ endif }}
            </tr>
            {{ endforeach }}
        </tbody>
    </table>
</body>
</html>
```

Now we need to create the Backus–Naur Form (BNF) of our language.
Left side displays out entities and right sight explains them.
```psql
<text> ::= any characters not starting with "{{"

<template> ::= (<text> | <expression> | <foreach> | <if>)*
<expression> ::= "{{" <propertyPath> "}}"
<propertyPath> ::= <identifier> ("." <identifier>)*
<identifier> ::= <letter> (<letter> | <digit>)*

<foreach> ::= "{{ foreach " <identifier> ":" <identifier> "}}" <template> "{{ endforeach }}"
<if> ::= "{{ if " <identifier> "}}" <template> [ "{{ else }}" <template> ] "{{ endif }}"
```

Each of the items on the right side will be a token that we need to extract from the template and parse them into the items that are on the right side.
The `extraction` into tokens will be done with the Tokenizer.

## 2. The Tokenizer

The Tokenizers job is to iterate trough the text and find the tokens of our language. T
his will make it easier to parse the tokens and and build our tree.

The first step will be building is the Token. It recieves the string that is the token in our template and the type of the token. The types are the different parts of the Language.

```java
public class MyTinyToken {
    private final String token;
    private final MyTinyTokenType type;

    public MyTinyToken(String token, MyTinyTokenType type) {
        this.token = token;
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public MyTinyTokenType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MyTinyToken that)) return false;
        return Objects.equals(token, that.token) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, type);
    }

    @Override
    public String toString() {
        return type.name() + "(\"" + token + "\")";
    }
}

public enum MyTinyTokenType {
    TEXT,
    LBRACE2,
    RBRACE2,
    IDENTIFIER,
    DOT,
    KEYWORD,
    COLON,
}
```


Next we will need to Implement the class and method for tokenization. We will leave it blank for now.
```java 
import de.dmitrij.patuk.template.token.MyTinyToken;
import de.dmitrij.patuk.template.token.MyTinyTokenType;

public class MyTinyTokenizer {
    public List<MyTinyToken> tokenize(String input) {
        return null;
    }
}
```

And the Tests in order to verify that our method is working
```java

public class MyTinyTokenizerTests{
    @Test
    void testPlainTextOnly() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("Hello World");
        assertEquals(1, tokens.size());
        assertEquals(MyTinyTokenType.TEXT, tokens.get(0).getType());
        assertEquals("Hello World", tokens.get(0).getValue());
    }

    @Test
    void testEmptyString() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("");
        assertTrue(tokens.isEmpty(), "Empty input should return no tokens");
    }

    @Test
    void testSingleTextBlock() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("Hello World");
        assertEquals(1, tokens.size());
        assertEquals(MyTinyTokenType.TEXT, tokens.get(0).getType());
        assertEquals("Hello World", tokens.get(0).getValue());
    }
}

```

Next step is the scaffolding for out Tokenizer

```java
import de.dmitrij.patuk.template.token.MyTinyToken;
import de.dmitrij.patuk.template.token.MyTinyTokenType;

public class MyTinyTokenizer {
    public List<MyTinyToken> tokenize(String input) {
        List<MyTinyToken> tokens = new ArrayList<>();
        if (!input.isEmpty()) {
            tokens.add(new MyTinyToken(input, MyTinyTokenType.TEXT));
        }
        return tokens;
    }
}
```



## 3. The Expression Block {{ }}

Now we need to update our UnitTests and the Tokenizer for the ability to recognize expression blocks and parse them properly.

```java
public class MyTinyTokenizerTests{

    @Test
    void testTextAndBraces() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("Hello {{ name }} World");
        assertEquals(
                List.of(
                        new MyTinyToken("Hello ", MyTinyTokenType.TEXT),
                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken(" name ", MyTinyTokenType.TEXT),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2),
                        new MyTinyToken(" World", MyTinyTokenType.TEXT)
                ),
                tokens
        );
    }

    @Test
    void testExpressionDelimitersOnly() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("{{}}");
        assertEquals(2, tokens.size());
        assertEquals(MyTinyTokenType.LBRACE2, tokens.get(0).getType());
        assertEquals(MyTinyTokenType.RBRACE2, tokens.get(1).getType());
    }

    @Test
    void testTextAroundExpression() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("Hello {{}} World");
        assertEquals(3, tokens.stream().filter(t -> t.getType() == MyTinyTokenType.TEXT).count());
        assertEquals("Hello ", tokens.get(0).getValue());
        assertEquals(" World", tokens.get(2).getValue());
    }

}
```

Now we update our tokenize method to fulfill the tests.

```java
import de.dmitrij.patuk.template.token.MyTinyToken;
import de.dmitrij.patuk.template.token.MyTinyTokenType;

public class MyTinyTokenizer {
    public List<MyTinyToken> tokenize(String input) {
        List<MyTinyToken> tokens = new ArrayList<>();
        StringBuilder textBuffer = new StringBuilder();
        boolean inExpr = false;
        int i = 0;
        while (i < input.length()) {
            if (input.startsWith("{{", i)) {
                // our important bits are inside the {{ so we always dump everything that comes before beucase that is text
                if (textBuffer.length() > 0) {
                    tokens.add(new MyTinyToken(textBuffer.toString(), MyTinyTokenType.TEXT));
                    textBuffer.setLength(0);
                }
                tokens.add(new MyTinyToken("{{", MyTinyTokenType.LBRACE2));
                boolean inExpr = true;
                i += 2;
            } else if (input.startsWith("}}", i)) {
                tokens.add(new MyTinyToken("}}", MyTinyTokenType.RBRACE2));
                boolean inExpr = false;
                i += 2;
            } else {
                textBuffer.append(input.charAt(i++));
            }
        }
        if (textBuffer.length() > 0) {
            tokens.add(new MyTinyToken(textBuffer.toString(), MyTinyTokenType.TEXT));
        }
        return tokens;
    }

}

```


## 4. Identifiers

We are able to find the expressions, next are the identifiers. We first build the UnitTests.

```java
public class MyTinyTokenizerTests{
    //additional tests
    @Test
    void testIdentifierInsideExpression() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("{{ model }}");
        assertEquals(
                List.of(
                        new MyTinyToken("{{", MyTinyTokenType.LBRACE2),
                        new MyTinyToken("model", MyTinyTokenType.IDENTIFIER),
                        new MyTinyToken("}}", MyTinyTokenType.RBRACE2)
                ),
                tokens
        );
    }

    @Test
    void testSimpleIdentifier() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("{{ name }}");
        assertEquals(MyTinyTokenType.IDENTIFIER, tokens.get(1).getType());
        assertEquals("name", tokens.get(1).getValue());
    }

    @Test
    void testIdentifierWithDigitsAndUnderscore() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("{{ person_123 }}");
        assertEquals(MyTinyTokenType.IDENTIFIER, tokens.get(1).getType());
        assertEquals("person_123", tokens.get(1).getValue());
    }

}
```


Now we update the method to match the tests.

```java
import de.dmitrij.patuk.template.token.MyTinyToken;
import de.dmitrij.patuk.template.token.MyTinyTokenType;

public class MyTinyTokenizer {
    public List<MyTinyToken> tokenize(String input) {
        List<MyTinyToken> tokens = new ArrayList<>();
        StringBuilder textBuffer = new StringBuilder();
        boolean inExpr = false;
        int i = 0;
        while (i < input.length()) {
            if (input.startsWith("{{", i)) {
                // our important bits are inside the {{ so we always dump everything that comes before beucase that is text
                if (textBuffer.length() > 0) {
                    tokens.add(new MyTinyToken(textBuffer.toString(), MyTinyTokenType.TEXT));
                    textBuffer.setLength(0);
                }
                tokens.add(new MyTinyToken("{{", MyTinyTokenType.LBRACE2));
                boolean inExpr = true;
                i += 2;
            } else if (input.startsWith("}}", i)) {
                tokens.add(new MyTinyToken("}}", MyTinyTokenType.RBRACE2));
                boolean inExpr = false;
                i += 2;
            } 
            //new code 
            else if (inExpr && Character.isLetterOrDigit(input.charAt(i))) {
                StringBuilder identBuf = new StringBuilder();
                while (i < input.length() &&
                        (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
                    identBuf.append(input.charAt(i++));
                }
                tokens.add(new MyTinyToken(identBuf.toString(), MyTinyTokenType.IDENTIFIER));
            } else {
                textBuffer.append(input.charAt(i++));
            }
            //end new code 
        }
        if (textBuffer.length() > 0) {
            tokens.add(new MyTinyToken(textBuffer.toString(), MyTinyTokenType.TEXT));
        }
        return tokens;
    }

}

```

## 5. Keywords

The UnitTests:

```java
public class MyTinyTokenizerTests{
    //additional tests
    @Test
    void testKeywordsAreRecognized() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("{{ if }} {{ endif }}");
        assertEquals(MyTinyTokenType.KEYWORD, tokens.get(1).getType());
        assertEquals("if", tokens.get(1).getValue());
        assertEquals(MyTinyTokenType.KEYWORD, tokens.get(3).getType());
        assertEquals("endif", tokens.get(3).getValue());
    }

    @Test
    void testIfKeyword() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("{{ if }}");
        assertEquals(MyTinyTokenType.KEYWORD, tokens.get(1).getType());
        assertEquals("if", tokens.get(1).getValue());
    }

    @Test
    void testForeachKeyword() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("{{ foreach }} {{ endforeach }}");
        assertEquals("foreach", tokens.get(1).getValue());
        assertEquals("endforeach", tokens.get(3).getValue());
        assertEquals(MyTinyTokenType.KEYWORD, tokens.get(1).getType());
        assertEquals(MyTinyTokenType.KEYWORD, tokens.get(3).getType());
    }

    @Test
    void testElseKeyword() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("{{ else }}");
        assertEquals(MyTinyTokenType.KEYWORD, tokens.get(1).getType());
        assertEquals("else", tokens.get(1).getValue());
    }

}
```

The updated method:
```java
import de.dmitrij.patuk.template.token.MyTinyToken;
import de.dmitrij.patuk.template.token.MyTinyTokenType;

public class MyTinyTokenizer {
    public List<MyTinyToken> tokenize(String input) {
        List<MyTinyToken> tokens = new ArrayList<>();
        StringBuilder textBuffer = new StringBuilder();
        int i = 0;
        boolean inExpr = false;
        while (i < input.length()) {
            if (input.startsWith("{{", i)) {
                // our important bits are inside the {{ so we always dump everything that comes before beucase that is text
                if (textBuffer.length() > 0) {
                    tokens.add(new MyTinyToken(textBuffer.toString(), MyTinyTokenType.TEXT));
                    textBuffer.setLength(0);
                }
                tokens.add(new MyTinyToken("{{", MyTinyTokenType.LBRACE2));
                inExpr = true;
                i += 2;
            } else if (input.startsWith("}}", i)) {
                tokens.add(new MyTinyToken("}}", MyTinyTokenType.RBRACE2));
                inExpr = false;
                i += 2;
            } else if (inExpr && Character.isLetterOrDigit(input.charAt(i))) {
                StringBuilder identBuf = new StringBuilder();
                while (i < input.length() &&
                        (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
                    identBuf.append(input.charAt(i++));
                }
                // new code, we can have an identifier or a keyword so we use an if block to decide
                String ident = identBuf.toString();
                if (ident.equals("foreach") || ident.equals("endforeach") ||
                        ident.equals("if") || ident.equals("endif") || ident.equals("else")) {
                    tokens.add(new MyTinyToken(ident, MyTinyTokenType.KEYWORD));
                } else {
                    tokens.add(new MyTinyToken(ident, MyTinyTokenType.IDENTIFIER));
                }
                // end new code
            } else {
                textBuffer.append(input.charAt(i++));
            }
        }
        if (textBuffer.length() > 0) {
            tokens.add(new MyTinyToken(textBuffer.toString(), MyTinyTokenType.TEXT));
        }
        return tokens;
    }

}

```

## 6. DOT and COLON

Now we need to add parsing for the punctuation that. It will be similar to what we did previously. First the UnitTests.

```java
public class MyTinyTokenizerTests {
    //additional tests
    @Test
    void testDotAndColon() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("{{ person.name : people }}");
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == MyTinyTokenType.DOT));
        assertTrue(tokens.stream().anyMatch(t -> t.getType() == MyTinyTokenType.COLON));
    }


    @Test
    void testPropertyPathWithDot() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("{{ model.title }}");

        assertEquals("model", tokens.get(1).getValue());
        assertEquals(MyTinyTokenType.IDENTIFIER, tokens.get(1).getType());

        assertEquals(".", tokens.get(2).getValue());
        assertEquals(MyTinyTokenType.DOT, tokens.get(2).getType());

        assertEquals("title", tokens.get(3).getValue());
        assertEquals(MyTinyTokenType.IDENTIFIER, tokens.get(3).getType());
    }

    @Test
    void testForeachWithColon() {
        MyTinyTokenizer tokenizer = new MyTinyTokenizer();
        List<MyTinyToken> tokens = tokenizer.tokenize("{{ foreach person : people }}");

        assertEquals("foreach", tokens.get(1).getValue());
        assertEquals(MyTinyTokenType.KEYWORD, tokens.get(1).getType());

        assertEquals("person", tokens.get(2).getValue());
        assertEquals(MyTinyTokenType.IDENTIFIER, tokens.get(2).getType());

        assertEquals(":", tokens.get(3).getValue());
        assertEquals(MyTinyTokenType.COLON, tokens.get(3).getType());

        assertEquals("people", tokens.get(4).getValue());
        assertEquals(MyTinyTokenType.IDENTIFIER, tokens.get(4).getType());
    }

}
```

We just look at the next character and if it matches what we are looking for we create the token.
The updated method:
```java
import de.dmitrij.patuk.template.token.MyTinyToken;
import de.dmitrij.patuk.template.token.MyTinyTokenType;

public class MyTinyTokenizer {
    public List<MyTinyToken> tokenize(String input) {
        List<MyTinyToken> tokens = new ArrayList<>();
        StringBuilder textBuffer = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (input.startsWith("{{", i)) {
                // our important bits are inside the {{ so we always dump everything that comes before beucase that is text
                if (textBuffer.length() > 0) {
                    tokens.add(new MyTinyToken(textBuffer.toString(), MyTinyTokenType.TEXT));
                    textBuffer.setLength(0);
                }
                tokens.add(new MyTinyToken("{{", MyTinyTokenType.LBRACE2));
                i += 2;
            } else if (input.startsWith("}}", i)) {
                tokens.add(new MyTinyToken("}}", MyTinyTokenType.RBRACE2));
                i += 2;
            } else if (inExpr && Character.isLetterOrDigit(input.charAt(i))) {
                StringBuilder identBuf = new StringBuilder();
                while (i < input.length() &&
                        (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
                    identBuf.append(input.charAt(i++));
                }
                String ident = identBuf.toString();
                if (ident.equals("foreach") || ident.equals("endforeach") ||
                        ident.equals("if") || ident.equals("endif") || ident.equals("else")) {
                    tokens.add(new MyTinyToken(ident, MyTinyTokenType.KEYWORD));
                } else {
                    tokens.add(new MyTinyToken(ident, MyTinyTokenType.IDENTIFIER));
                }
            }
            // new code, we can have an identifier or a keyword so we use an if block to decide
            else if (inExpr && input.charAt(i) == '.') {
                tokens.add(new MyTinyToken(".", MyTinyTokenType.DOT));
                i++;
            } else if (inExpr && input.charAt(i) == ':') {
                tokens.add(new MyTinyToken(":", MyTinyTokenType.COLON));
                i++;
            }
            // end new code
            else {
                textBuffer.append(input.charAt(i++));
            }
        }
        if (textBuffer.length() > 0) {
            tokens.add(new MyTinyToken(textBuffer.toString(), MyTinyTokenType.TEXT));
        }
        return tokens;
    }

}

```