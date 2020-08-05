package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);

    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;


    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char ch = advance();
        switch (ch) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;

            case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=': addToken(match('=') ? EQUAL : EQUAL_EQUAL); break;
            case '>': addToken(match('=') ? GREATER : GREATER_EQUAL); break;
            case '<': addToken(match('=') ? LESS : LESS_EQUAL); break;

            // '/' or comment
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;

            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                line++;
                break;

            case '"':
                string();
                break;

            default:
                if (isDigit(ch)) {
                    number();
                } else if (isAlpha(ch)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, IDENTIFIER);
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            char ch = advance();
            if (ch == '\n') {
                line++;
            }
        }
        if (peek() == '"') {
            advance();
            addToken(STRING, source.substring(start + 1, current - 1));
        } else {
            Lox.error(line, "Unterminated string");
        }
    }

    private boolean match(char expected) {
        if (isAtEnd())  return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return current + 1 < source.length() ? source.charAt(current + 1) : '\0';
    }

    private boolean isAlpha(char ch) {
        return ch == '_' || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    private boolean isAlphaNumeric(char ch) {
        return isAlpha(ch) || isDigit(ch);
    }

    private boolean isDigit(char ch) {
        return  ch >= '0' && ch <= '9';
    }


    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line));
    }
}
