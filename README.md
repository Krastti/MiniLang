## Badges

![Java 21](https://img.shields.io/badge/Java-21-blue)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36)

# MiniLang

MiniLang — учебный язык программирования с интерпретатором на Java.

**Результат локального прогона:** прошли все 13 сценариев [InterpretatorTest](src/test/java/com/krastti/minilang/InterpretatorTest.java) и все 62 теста проекта. Тесты интерпретатора проверяют выполнение исходного кода через лексер, парсер и интерпретатор.

```text
remember total = 2 + 3 * 4
print total
print total >= 10
```

Программа выводит:

```text
14.0
true
```

## Documentation

[Документация MiniLang](Documentation.md) описывает синтаксис, запуск и примеры программ.

## Features

- Запуск программ из файлов `.mini`.
- Объявление переменных через `remember` и вывод значений через `print`.
- Арифметика со скобками и числовые сравнения с результатом `true` или `false`.

## Installation

Установи JDK 21 и Maven. В корне репозитория выполни:

```bash
mvn compile
java -cp target/classes com.krastti.minilang.Main examples/main.mini
```

## Running Tests

```bash
mvn test
```

Чтобы запустить только тесты интерпретатора:

```bash
mvn -Dtest=InterpretatorTest test
```

## Authors

- [@Krastti](https://github.com/Krastti)

## License

Лицензия пока не указана.
