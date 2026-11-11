package com.krastti.minilang;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    public List<Statement> parse(List<Token> tokens) {

        List<Statement> statements = new ArrayList<>();

        for (int position = 0; position < tokens.size(); position++) {
            TokenType type = tokens.get(position).type();
            ParsedExpr result;
            Statement statement;

            switch (type) {
                case NEWLINE -> { continue; }
                case EOF -> { return statements; }
                case REMEMBER -> {
                    Token name = requireToken(tokens, position + 1, TokenType.IDENTIFIER,
                            "Ожидалось имя переменной после remember");
                    requireToken(tokens, position + 2, TokenType.EQUALS, "Ожидался знак = после имени переменной");
                    result = parseEquality(tokens, position + 3);
                    statement = new Remember(name.lexeme(), result.expression());
                }
                case PRINT -> {
                    result = parseEquality(tokens, position + 1);
                    statement = new Print(result.expression());
                }
                default -> throw new IllegalArgumentException("Неожиданный токен в начале инструкции: " + type);
            }

            int next = result.nextPosition();
            if (next < tokens.size()) {
                TokenType nextType = tokens.get(next).type();
                if (nextType != TokenType.EOF && nextType != TokenType.NEWLINE) {
                    throw new IllegalArgumentException("Ожидался перевод строки или конец файла");
                }
            }
            statements.add(statement);
            position = next - 1;
        }

        return statements;
    }

    private Token requireToken(List<Token> tokens, int position, TokenType expected, String message) {
        if (position >= tokens.size() || tokens.get(position).type() != expected) {
            throw new IllegalArgumentException(message);
        }
        return tokens.get(position);
    }

    private Expr parseOperand(List<Token> tokens, int position) {
        if (position >= tokens.size()) {
            throw new IllegalArgumentException("Ожидалось число или имя, получен конец ввода");
        }
        Token token = tokens.get(position);

        return switch (token.type()) {
            case NUMBER -> new NumberLiteral(token.lexeme());
            case IDENTIFIER -> new Identifier(token.lexeme());
            default -> throw new IllegalArgumentException(
                    "Ожидалось число или имя, получен " + token.type());
        };
    }

    private ParsedExpr parsePrimary(List<Token> tokens, int position) {
        if (position < tokens.size() && tokens.get(position).type() == TokenType.LEFT_PAREN) {
            ParsedExpr inner = parseEquality(tokens, position + 1);
            int closingPosition = inner.nextPosition();
            requireToken(tokens, closingPosition, TokenType.RIGHT_PAREN, "Ожидалась закрывающая скобка");
            return new ParsedExpr(inner.expression(), closingPosition + 1);
        }
        return new ParsedExpr(parseOperand(tokens, position), position + 1);
    }

    private ParsedExpr parseMultiplication(List<Token> tokens, int position) {
        ParsedExpr first = parsePrimary(tokens, position);
        Expr left = first.expression();
        int next = first.nextPosition();

        while (next < tokens.size()) {
            TokenType operator = tokens.get(next).type();
            if (operator != TokenType.STAR && operator != TokenType.SLASH) break;

            ParsedExpr right = parsePrimary(tokens, next + 1);
            left = new Binary(operator, left, right.expression());
            next = right.nextPosition();
        }
        return new ParsedExpr(left, next);
    }

    private ParsedExpr parseAddition(List<Token> tokens, int position) {
        ParsedExpr first = parseMultiplication(tokens, position);
        Expr left = first.expression();
        int next = first.nextPosition();

        while (next < tokens.size()) {
            TokenType operator = tokens.get(next).type();
            if (operator != TokenType.PLUS && operator != TokenType.MINUS) break;

            ParsedExpr right = parseMultiplication(tokens, next + 1);
            left = new Binary(operator, left, right.expression());
            next = right.nextPosition();
        }

        return new ParsedExpr(left, next);
    }

    private ParsedExpr parseComparison(List<Token> tokens, int position) {
        ParsedExpr result = parseAddition(tokens, position);
        while (result.nextPosition() < tokens.size()) {
            TokenType operator = tokens.get(result.nextPosition()).type();
            if (operator != TokenType.LESS && operator != TokenType.LESS_EQUAL
                    && operator != TokenType.GREATER && operator != TokenType.GREATER_EQUAL) {
                break;
            }

            ParsedExpr right = parseAddition(tokens, result.nextPosition() + 1);
            result = new ParsedExpr(new Binary(operator, result.expression(), right.expression()), right.nextPosition());
        }
        return result;
    }

    private ParsedExpr parseEquality(List<Token> tokens, int position) {
        ParsedExpr result = parseComparison(tokens, position);
        while (result.nextPosition() < tokens.size()) {
            TokenType operator = tokens.get(result.nextPosition()).type();
            if (operator != TokenType.EQUAL_EQUAL && operator != TokenType.BANG_EQUAL) {
                break;
            }

            ParsedExpr right = parseComparison(tokens, result.nextPosition() + 1);
            result = new ParsedExpr(new Binary(operator, result.expression(), right.expression()), right.nextPosition());
        }
        return result;
    }

    public interface Statement {}

    interface Expr {}

    private record ParsedExpr(Expr expression, int nextPosition) {}

    record NumberLiteral(String lexeme) implements Expr {}

    record Identifier(String name) implements Expr {}

    record Binary(TokenType operator, Expr left, Expr right) implements Expr {}

    record Remember(String variable, Expr right) implements Statement {}

    record Print(Expr forOutput) implements Statement {}
}
