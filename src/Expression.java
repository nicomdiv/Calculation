import java.util.*;

import static java.lang.Double.parseDouble;

public class Expression {

    private final Double result;

    public Expression(String expression) {
        List<String> listRpn = Parsing.parseToRpn(expression);
        result = computeRpn(listRpn);
    }

    public Double getResult() {
        return result;
    }

    //Вычисление обратной польской записи
    Double computeRpn(List<String> rpn) {
        Deque<Double> stack = new ArrayDeque<>();
        for (String x : rpn) {
            switch (x) {
                case "+" -> stack.push(stack.pop() + stack.pop());
                case "-" -> stack.push(-stack.pop() + stack.pop());
                case "*" -> stack.push(stack.pop() * stack.pop());
                case "/" -> {
                    if (stack.peek() != null && stack.peek() == 0)
                        throw new ArithmeticException("Ошибка: нельзя делить на ноль");
                    stack.push((1 / stack.pop()) * stack.pop());
                }
                //Возведение в степень
                case "^" -> {
                    Double exponent = stack.pop();
                    stack.push(Math.pow(stack.pop(), exponent));
                }
                //
                case "unary_minus" -> stack.push(-stack.pop());
                default -> stack.push(Double.valueOf(x));
            }
        }
        return stack.pop();
    }
}

//Класс для работы с консолью (чтения, вывода)
class Console {

    public static void main(String[] args) {
        //Ввод с консоли
        String stringExpression = Console.readLine();
        //Валидация
        Validation.check(stringExpression);
        //Создаем выражение
        Expression expressions = new Expression(stringExpression);
        //Вывод результата
        Console.outputResult(expressions.getResult());
    }

    static String readLine() {
        System.out.println("Введите математическое выражение для вычисления(поддерживаются цифры b ):");
        return new Scanner(System.in).nextLine();
    }

    static void outputResult(Double result) {
        System.out.println("Результат вычисления:");
        System.out.printf("%f", result);
    }
}

//Класс валидации выражения
class Validation {
    static void check(String expression) {
        Scanner scanner = new Scanner(expression);
        String notSupported = scanner.findInLine("[^0-9(). +\\-*/^]+");
        if (notSupported != null)
            throw new UnsupportedOperationException("Ошибка: неподдерживаемый символ: " + notSupported);
    }
}

//Класс разбора (парсинга) строки в выражение ОПЗ
class Parsing {
    //Поддерживаемые операции
    public static final String operators = "+-*/^";
    //Разделители
    public static final String delimiters = "() " + operators;

    //Вычисление приоритета оператоции
    private static int priority(String operation) {
        return switch (operation) {
            case "(" -> 0;
            case "+", "-" -> 1;
            case "*", "/", "^" -> 2;
            default -> 3;
        };
    }

    //Парсинг текстового выражения в обратную польскую запись
    static List<String> parseToRpn(String expression) {
        List<String> postfix = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();

        StringTokenizer tokenizer = new StringTokenizer(expression, delimiters, true);
        String last = "";
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (token.equals(" ")) continue;
            //Если за оператором идет оператор
            if (operators.contains(token) && operators.contains(last))
                throw new UnsupportedOperationException("Ошибка: дублирование символа операции: " + token);
            //Если
            if (delimiters.contains(token) && token.length() == 1) {
                switch (token) {
                    case "(" -> stack.push(token);
                    case ")" -> {
                        while (stack.peek() != null && !stack.peek().equals("(")) {
                            postfix.add(stack.pop());
                        }
                        stack.pop();
                        if (!stack.isEmpty() && operators.contains(stack.peek())) {
                            postfix.add(stack.pop());
                        }
                    }
                    default -> {
                        if (token.equals("-") && (last.equals("") || (delimiters.contains(last) && last.length() == 1 && !last.equals(")")))) {
                            // унарный минус
                            token = "unary_minus";
                        } else {
                            while (!stack.isEmpty() && (priority(token) <= priority(stack.peek()))) {
                                postfix.add(stack.pop());
                            }

                        }
                        stack.push(token);
                    }
                }
            } else {
                try {
                    parseDouble(token);
                } catch (NumberFormatException e) {
                    throw new UnsupportedOperationException("Ошибка: число введено неверно : " + token);
                }
                postfix.add(token);
            }
            last = token;
        }
        while (!stack.isEmpty()) {
            postfix.add(stack.pop());
        }
        return postfix;
    }
}