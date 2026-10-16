# Лабораторная работа №1 — Spring Boot

## Запуск
Требуется Java 17+ и Maven.

```bash
mvn spring-boot:run
```

После запуска приложение доступно на http://localhost:8080.

## Endpoints

- GET `/hello`
- GET `/hello?name=Petya`
- GET `/update-array?s=Apple`
- GET `/show-array`
- GET `/update-map?s=Apple`
- GET `/show-map`
- GET `/show-all-lenght`

Примеры:
```text
http://localhost:8080/hello
http://localhost:8080/hello?name=Petya
http://localhost:8080/update-array?s=Apple
http://localhost:8080/update-array?s=Banana
http://localhost:8080/show-array
http://localhost:8080/update-map?s=Apple
http://localhost:8080/update-map?s=Banana
http://localhost:8080/show-map
http://localhost:8080/show-all-lenght
```
