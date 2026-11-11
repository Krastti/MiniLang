package com.krastti.minilang;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterpretatorTest {

    @Test
    void producesNoOutputForAnEmptyProgram() {
        assertEquals(List.of(), run(""));
    }

    @ParameterizedTest
    @CsvSource({
            "7, 7",
            "2 + 3 * 4, 14",
            "(2 + 3) * 4, 20",
            "8 - 3 - 2, 3",
            "12 / 3, 4"
    })
    void printsTheResultOfAnArithmeticExpression(String expression, double expected) {
        assertEquals(List.of(expected), numericOutput("print " + expression));
    }

    @Test
    void remembersEvaluatedValuesAndPrintsInProgramOrder() {
        assertEquals(List.of(7.0, 21.0), numericOutput("""
                remember base = 2 + 5
                remember total = base * 3
                print base
                print total
                """));
    }

    @ParameterizedTest
    @CsvSource({
            "2 < 3, 3 < 3",
            "3 <= 3, 4 <= 3",
            "4 > 3, 3 > 3",
            "3 >= 3, 2 >= 3",
            "2 + 1 == 3, 2 == 3",
            "2 != 3, 3 != 3"
    })
    void printsBooleanResultsForComparisons(String trueExpression, String falseExpression) {
        assertEquals(List.of("true", "false"),
                run("print " + trueExpression + "\nprint " + falseExpression));
    }

    private List<Double> numericOutput(String source) {
        return run(source).stream().map(Double::parseDouble).toList();
    }

    private List<String> run(String source) {
        var statements = new Parser().parse(new Lexer().tokenize(source));
        var output = new ByteArrayOutputStream();
        try (var stream = new PrintStream(output, false, StandardCharsets.UTF_8)) {
            new Interpretator().execute(statements, stream);
        }
        return output.toString(StandardCharsets.UTF_8).lines().toList();
    }
}
