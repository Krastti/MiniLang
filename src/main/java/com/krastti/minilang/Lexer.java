package com.krastti.minilang;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class Lexer {

    private static final Map<String, TokenType> KEYWORDS = Map.of(
            "remember", TokenType.REMEMBER,
            "print", TokenType.PRINT,
            "when", TokenType.WHEN,
            "otherwise", TokenType.OTHERWISE,
            "repeat", TokenType.REPEAT
    );

    public List<Token> tokenize(String args) {
        List<Token> tokens = new TokenList();

        for (int pos = 0; pos < args.length(); ++pos) {
            var symbol = args.charAt(pos);

            if (symbol == ' ' || symbol == '\t') continue;

            if (symbol == '\r' && pos + 1 < args.length() && args.charAt(pos + 1) == '\n') {
                tokens.add(new Token(TokenType.NEWLINE, "\r\n"));
                pos++;
                continue;
            }

            if (symbol == '=' && pos + 1 < args.length() && args.charAt(pos + 1) == '=') {
                tokens.add(new Token(TokenType.EQUAL_EQUAL, "=="));
                pos++;
                continue;
            }

            if (symbol == '!' && pos + 1 < args.length() && args.charAt(pos + 1) == '=') {
                tokens.add(new Token(TokenType.BANG_EQUAL, "!="));
                pos++;
                continue;
            }

            if (symbol == '<' && pos + 1 < args.length() && args.charAt(pos + 1) == '=') {
                tokens.add(new Token(TokenType.LESS_EQUAL, "<="));
                pos++;
                continue;
            }

            if (symbol == '>' && pos + 1 < args.length() && args.charAt(pos + 1) == '=') {
                tokens.add(new Token(TokenType.GREATER_EQUAL, ">="));
                pos++;
                continue;
            }

            if (Character.isDigit(symbol)) {
                int end = readNumber(args, pos);
                tokens.add(new Token(TokenType.NUMBER, args.substring(pos, end)));
                pos = end - 1;
                continue;
            }

            if (Character.isAlphabetic(symbol) || symbol == '_') {
                int end = readIdentifier(args, pos);
                String word = args.substring(pos, end);
                TokenType type = KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER);
                tokens.add(new Token(type, word));
                pos = end - 1;
                continue;
            }

            try {
                tokens.add(scanToken(symbol));
            } catch (IndexOutOfBoundsException e) {
                throw new IndexOutOfBoundsException("Позиция вышла за пределы строки" + e);
            }
        }

        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private Token scanToken(char symbol) {
        TokenType type = switch (symbol) {
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINUS;
            case '*' -> TokenType.STAR;
            case '/' -> TokenType.SLASH;

            case '=' -> TokenType.EQUALS;
            case '<' -> TokenType.LESS;
            case '>' -> TokenType.GREATER;

            case '(' -> TokenType.LEFT_PAREN;
            case ')' -> TokenType.RIGHT_PAREN;
            case '\n' -> TokenType.NEWLINE;

            default -> throw new IllegalArgumentException("Неожиданный символ: " + symbol);
        };

        return new Token(type, String.valueOf(symbol));
    }

    private int readNumber(String source, int start) {
        int end = start;
        while (end < source.length() && Character.isDigit(source.charAt(end))) end++;
        return end;
    }

    private int readIdentifier(String source, int start) {
        int end = start + 1;
        while (end < source.length()) {
            char symbol = source.charAt(end);
            if (!(Character.isAlphabetic(symbol) || Character.isDigit(symbol) || symbol == '_')) {
                break;
            }
            end++;
        }
        return end;
    }

    private static final class TokenList extends ArrayList<Token> {
        @Override
        public String toString() {
            String newline = System.lineSeparator();
            var lines = new StringJoiner(newline, "", newline).setEmptyValue("");
            for (Token token : this) {
                String lexeme = token.lexeme()
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\r", "\\r")
                        .replace("\n", "\\n")
                        .replace("\t", "\\t");
                lines.add(token.type() + " \"" + lexeme + "\"");
            }
            return lines.toString();
        }
    }
}

enum TokenType {
    REMEMBER,
    PRINT,
    WHEN,
    OTHERWISE,
    REPEAT,

    IDENTIFIER,
    NUMBER,

    PLUS,
    MINUS,
    STAR,
    SLASH,

    EQUALS,
    EQUAL_EQUAL,
    BANG_EQUAL,
    LESS,
    LESS_EQUAL,
    GREATER,
    GREATER_EQUAL,

    LEFT_PAREN,
    RIGHT_PAREN,
    NEWLINE,
    EOF
}
