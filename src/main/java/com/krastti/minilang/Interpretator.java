package com.krastti.minilang;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused"})
public class Interpretator {

    public void execute(List<Parser.Statement> statements, PrintStream output) {
        Map<String, Object> variables = new HashMap<>();

        for (Parser.Statement statement : statements) {
            if (statement instanceof Parser.Print print) {
                var expression = print.forOutput();
                output.println(evaluate(expression, variables));
            } else if (statement instanceof Parser.Remember remember) {
                Object value = evaluate(remember.right(), variables);
                variables.put(remember.variable(), value);
            }
        }
    }

    private Object evaluate(Parser.Expr expr, Map<String, Object> variables) {

        return switch (expr) {
            case Parser.NumberLiteral number -> Double.parseDouble(number.lexeme());

            case Parser.Identifier identifier -> {
                Object value = variables.get(identifier.name());
                if (value == null) {
                    throw new IllegalArgumentException("Неизвестная переменная: " + identifier.name());
                }
                yield value;
            }

            case Parser.Binary binary -> {
                Object leftValue = evaluate(binary.left(), variables);
                Object rightValue = evaluate(binary.right(), variables);
                if (!(leftValue instanceof Double left) || !(rightValue instanceof Double right)) {
                    throw new IllegalArgumentException("Оператор " + binary.operator() + " ожидает два числа");
                }

                yield switch (binary.operator()) {
                    case PLUS -> left + right;
                    case MINUS -> left - right;
                    case STAR -> left * right;
                    case SLASH -> left / right;
                    case LESS -> left < right;
                    case LESS_EQUAL -> left <= right;
                    case GREATER -> left > right;
                    case GREATER_EQUAL -> left >= right;
                    case EQUAL_EQUAL -> left.doubleValue() == right.doubleValue();
                    case BANG_EQUAL -> left.doubleValue() != right.doubleValue();
                    default -> throw new IllegalArgumentException(
                            "Неподдерживаемый оператор: " + binary.operator());
                };
            }

            default -> throw new IllegalArgumentException("Неподдерживаемое выражение: " + expr);
        };
    }
}
