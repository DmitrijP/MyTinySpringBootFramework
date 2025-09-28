package de.dmitrij.patuk.template;

import de.dmitrij.patuk.template.token.MyTinyToken;
import de.dmitrij.patuk.template.token.MyTinyTokenType;

import java.util.ArrayList;
import java.util.List;

// we are moving through the input character by character with input.startsWith(String, from)
// we increment the from with each step accordingly
// we are eather inside an expression or outside of it.
// if we are outside than we have text, we need to append to buffer and move to next token.
// if we enter an expression than we need to flush the buffer into a text token and create a LBRACE token
// === now we can have eather a character/letter, a DOT or a COLON
// === character or letter can be an Identifier or e Keyword
// if we reach a character we need a new buffer for the identifier/keyword, we loop trough it appending as long as we have characters, letters or underscore
// on completion of the loop we have a keyword or a identifier, we check accordingly and create the token
// === we go to next main iteration
// now we can have a TEXT LBRACE, RBRACE, DOT, COLON or identifier/keyword
// we loop appending to the main buffer
// if we reach the RBRACE we create it and exit expression
// last step is to append what ever was left in the buffer as a text token

public class MyTinyTokenizer {
    public List<MyTinyToken> tokenize(String input) {
        List<MyTinyToken> tokens = new ArrayList<>();
        StringBuilder textBuffer = new StringBuilder();
        boolean inExpr = false;
        int i = 0;

        while (i < input.length()) {
            if (input.startsWith("{{", i)) {
                //we need to flush the text that was before the "{{"
                if (textBuffer.length() > 0) {
                    tokens.add(new MyTinyToken(textBuffer.toString(), MyTinyTokenType.TEXT));
                    textBuffer.setLength(0);
                }
                tokens.add(new MyTinyToken("{{", MyTinyTokenType.LBRACE2));
                //we are now inside an expression
                inExpr = true;
                i += 2;
            } else if (input.startsWith("}}", i)) {
                textBuffer.setLength(0);
                tokens.add(new MyTinyToken("}}", MyTinyTokenType.RBRACE2));
                //we have exited the expression
                inExpr = false;
                i += 2;
            } else if (inExpr && Character.isLetterOrDigit(input.charAt(i))) {
                textBuffer.setLength(0);
                //we create its own buffer for the expression
                StringBuilder identBuf = new StringBuilder();
                //we accept only letters, digits and lowercase inside expression
                while (i < input.length() &&
                        (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
                    identBuf.append(input.charAt(i++));
                }
                //we have reached an illegal char so we flush and check if the buffer is an identifier or a keyword
                String ident = identBuf.toString();
                if (ident.equals("foreach") || ident.equals("endforeach") || ident.equals("if") || ident.equals("endif")) {
                    tokens.add(new MyTinyToken(ident, MyTinyTokenType.KEYWORD));
                } else {
                    tokens.add(new MyTinyToken(ident, MyTinyTokenType.IDENTIFIER));
                }
                //since we don`t increment we will check on next iteration the illegal character, it can be a dot or colon
            } else if (inExpr && input.charAt(i) == '.') {
                tokens.add(new MyTinyToken(".", MyTinyTokenType.DOT));
                i++;

            } else if (inExpr && input.charAt(i) == ':') {
                tokens.add(new MyTinyToken(":", MyTinyTokenType.COLON));
                i++;
            } else {
                // we have not reached any important character so we just increment
                textBuffer.append(input.charAt(i++));
            }
        }
        //we have reached the end and if there is still something in the buffer it is just text
        if (textBuffer.length() > 0) {
            tokens.add(new MyTinyToken(textBuffer.toString(), MyTinyTokenType.TEXT));
        }

        return tokens;
    }

    public String tokensToString(List<MyTinyToken> tokens) {
        StringBuilder sb = new StringBuilder();
        for (MyTinyToken t : tokens) {
            sb.append(t.toString().replace(" ", "").replace("\n", "")).append("\n");
        }
        return sb.toString();
    }
}
