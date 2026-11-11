## Badges

![Java 21](https://img.shields.io/badge/Java-21-blue)
![Maven](https://img.shields.io/badge/Build-Maven-C71A36)

# MiniLang

MiniLang — учебный язык программирования с интерпретатором на Java.

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

## Authors

- [@Krastti](https://github.com/Krastti)

## License

Лицензия пока не указана.
