package com.krastti.minilang;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserTest {

    private final Lexer lexer = new Lexer();
    private final Parser parser = new Parser();

    @Test
    void parsesAnEmptyProgram() {
        assertEquals(List.of(), parser.parse(lexer.tokenize("\n\n")));
    }

    @Test
    void parsesADeclarationAndPrintWithoutATrailingNewline() {
        var tokens = lexer.tokenize("remember age = 19\n\nprint age");

        assertEquals(List.of(
                new Parser.Remember("age", new Parser.NumberLiteral("19")),
                new Parser.Print(new Parser.Identifier("age"))
        ), parser.parse(tokens));
    }

    @Test
    void givesMultiplicationHigherPrecedenceThanAddition() {
        var multiplication = new Parser.Binary(TokenType.STAR,
                new Parser.NumberLiteral("3"), new Parser.NumberLiteral("4"));
        var addition = new Parser.Binary(TokenType.PLUS, new Parser.NumberLiteral("2"), multiplication);

        assertEquals(List.of(new Parser.Print(addition)), parser.parse(lexer.tokenize("print 2 + 3 * 4")));
    }

    @Test
    void parenthesesOverridePrecedence() {
        var addition = new Parser.Binary(TokenType.PLUS,
                new Parser.NumberLiteral("2"), new Parser.NumberLiteral("3"));
        var multiplication = new Parser.Binary(TokenType.STAR, addition, new Parser.NumberLiteral("4"));

        assertEquals(List.of(new Parser.Print(multiplication)), parser.parse(lexer.tokenize("print (2 + 3) * 4")));
    }

    @Test
    void groupsConsecutiveSubtractionsFromTheLeft() {
        var first = new Parser.Binary(TokenType.MINUS,
                new Parser.NumberLiteral("8"), new Parser.NumberLiteral("3"));
        var second = new Parser.Binary(TokenType.MINUS, first, new Parser.NumberLiteral("2"));

        assertEquals(List.of(new Parser.Print(second)), parser.parse(lexer.tokenize("print 8 - 3 - 2")));
    }

    @ParameterizedTest
    @CsvSource({"<, LESS", "<=, LESS_EQUAL", ">, GREATER", ">=, GREATER_EQUAL",
            "==, EQUAL_EQUAL", "!=, BANG_EQUAL"})
    void parsesComparisonOperators(String symbol, TokenType operator) {
        var comparison = new Parser.Binary(operator,
                new Parser.NumberLiteral("2"), new Parser.NumberLiteral("3"));

        assertEquals(List.of(new Parser.Print(comparison)),
                parser.parse(lexer.tokenize("print 2 " + symbol + " 3")));
    }

    @Test
    void arithmeticBindsBeforeOrderingAndOrderingBeforeEquality() {
        var sum = new Parser.Binary(TokenType.PLUS,
                new Parser.NumberLiteral("2"), new Parser.NumberLiteral("3"));
        var ordering = new Parser.Binary(TokenType.LESS, sum, new Parser.NumberLiteral("6"));
        var equality = new Parser.Binary(TokenType.EQUAL_EQUAL, new Parser.Identifier("flag"), ordering);

        assertEquals(List.of(new Parser.Print(equality)),
                parser.parse(lexer.tokenize("print flag == 2 + 3 < 6")));
    }

    @Test
    void parsesGroupedComparisonsInRemember() {
        var ordering = new Parser.Binary(TokenType.LESS_EQUAL,
                new Parser.NumberLiteral("2"), new Parser.NumberLiteral("3"));
        var equality = new Parser.Binary(TokenType.BANG_EQUAL, ordering, new Parser.Identifier("flag"));

        assertEquals(List.of(new Parser.Remember("result", equality)),
                parser.parse(lexer.tokenize("remember result = (2 <= 3) != flag")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"remember", "remember = 42", "remember x 42", "remember x =",
            "remember 123 = 7", "remember x + 7", "repeat 2\nprint 7", "print", "print 2 +",
            "print (2 + 3", "print 1 2", "print 1 print 2", "print 2 <=", "print 2 == == 3"})
    void rejectsInvalidSyntax(String source) {
        var tokens = lexer.tokenize(source);

        assertThrows(IllegalArgumentException.class, () -> parser.parse(tokens));
    }
}
