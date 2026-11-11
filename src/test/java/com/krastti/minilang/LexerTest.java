package com.krastti.minilang;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LexerTest {

    @Test
    void skipsSpacesAndRecognizesEquals() {
        var tokens = new Lexer().tokenize(" = ");
        var types = tokens.stream().map(Token::type).toList();

        assertEquals(List.of(TokenType.EQUALS, TokenType.EOF), types);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "\t \t"})
    void endsWithAnEmptyEofToken(String source) {
        assertEquals(List.of(new Token(TokenType.EOF, "")), new Lexer().tokenize(source));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "42", "007", "123456"})
    void readsConsecutiveDigitsAsOneNumber(String source) {
        assertTokens(source, new Token(TokenType.NUMBER, source));
    }

    @ParameterizedTest
    @ValueSource(strings = {"age", "x2", "_count", "remembered", "remember1"})
    void readsTheWholeIdentifierBeforeCheckingForKeywords(String source) {
        assertTokens(source, new Token(TokenType.IDENTIFIER, source));
    }

    @Test
    void recognizesRememberAsAKeyword() {
        assertTokens("remember", new Token(TokenType.REMEMBER, "remember"));
    }

    @Test
    void tokenizesADeclaration() {
        assertTokens("remember age = 19",
                new Token(TokenType.REMEMBER, "remember"),
                new Token(TokenType.IDENTIFIER, "age"),
                new Token(TokenType.EQUALS, "="),
                new Token(TokenType.NUMBER, "19"));
    }

    @Test
    void separatesTokensWithoutSpaces() {
        assertTokens("x=42",
                new Token(TokenType.IDENTIFIER, "x"),
                new Token(TokenType.EQUALS, "="),
                new Token(TokenType.NUMBER, "42"));
    }

    @Test
    void recognizesArithmeticOperatorsAndParentheses() {
        assertTokenTypeNames("(2+3)*4-8/2", "LEFT_PAREN", "NUMBER", "PLUS", "NUMBER",
                "RIGHT_PAREN", "STAR", "NUMBER", "MINUS", "NUMBER", "SLASH", "NUMBER", "EOF");
    }

    @ParameterizedTest
    @ValueSource(strings = {"\n", "\r\n"})
    void preservesOneNewlineBetweenInstructions(String newline) {
        assertTokenTypeNames("remember x = 42" + newline + "print x",
                "REMEMBER", "IDENTIFIER", "EQUALS", "NUMBER", "NEWLINE", "PRINT", "IDENTIFIER", "EOF");
    }

    @Test
    void keepsEachComparisonOperatorTogether() {
        assertTokenTypeNames("< <= > >= == != =", "LESS", "LESS_EQUAL", "GREATER", "GREATER_EQUAL",
                "EQUAL_EQUAL", "BANG_EQUAL", "EQUALS", "EOF");
    }

    @Test
    void recognizesControlFlowKeywords() {
        assertTokenTypeNames("when otherwise repeat whenever otherwise1 repeatCount",
                "WHEN", "OTHERWISE", "REPEAT", "IDENTIFIER", "IDENTIFIER", "IDENTIFIER", "EOF");
    }

    @Test
    void rejectsAnUnexpectedCharacter() {
        assertThrows(RuntimeException.class, () -> new Lexer().tokenize(" = @"));
    }

    private static void assertTokens(String source, Token... expected) {
        var expectedTokens = new ArrayList<>(List.of(expected));
        expectedTokens.add(new Token(TokenType.EOF, ""));

        assertEquals(expectedTokens, new Lexer().tokenize(source));
    }

    private static void assertTokenTypeNames(String source, String... expected) {
        var actual = new Lexer().tokenize(source).stream().map(token -> token.type().name()).toList();

        assertEquals(List.of(expected), actual);
    }
}
