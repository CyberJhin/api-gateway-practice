# API Gateway Practice

## О проекте

**API Gateway Practice** — учебный проект на Java, демонстрирующий базовые принципы построения микросервисной архитектуры с использованием API Gateway. В проекте реализованы три основных компонента:
- **API Gateway** — маршрутизация, фильтрация, безопасность на базе Spring Cloud Gateway;
- **Auth Service** — сервис аутентификации с поддержкой JWT и хранения пользователей;
- **User Service** — сервис управления пользователями.

## Назначение

- Практика построения и интеграции микросервисов на Spring Boot;
- Демонстрация настройки и использования Spring Cloud Gateway;
- Пример реализации аутентификации и авторизации через JWT;
- Ознакомление с паттернами маршрутизации, фильтрации и защиты API.

## Технологии и зависимости

- Java 17
- Spring Boot
- Spring Cloud Gateway
- Spring Security
- Spring WebFlux (для gateway)
- Spring Data JPA
- Lombok
- JWT (io.jsonwebtoken)
- PostgreSQL (как СУБД для сервисов)
- JUnit 5

## Структура проекта

```
api-gateway-practice/
├── api-gateway/      # Модуль gateway (Spring Cloud Gateway)
│   └── build.gradle
├── auth-service/     # Модуль сервиса аутентификации
│   └── build.gradle
├── user-service/     # Модуль сервиса пользователей
│   └── build.gradle
```

## Как запустить

1. Клонируйте репозиторий:
    ```sh
    git clone https://github.com/CyberJhin/api-gateway-practice.git
    cd api-gateway-practice
    ```

2. Перейдите в каждый модуль и соберите его (пример для Gradle):
    ```sh
    cd api-gateway && ./gradlew bootRun
    cd ../auth-service && ./gradlew bootRun
    cd ../user-service && ./gradlew bootRun
    ```
    (или используйте аналогичные команды с Maven, если добавлены pom.xml)

3. Убедитесь, что у вас запущена база данных PostgreSQL и настроены переменные окружения/файлы конфигурации.

## Краткое описание модулей

- **api-gateway**:
    - Использует Spring Cloud Gateway для маршрутизации и фильтрации запросов.
    - Настроена интеграция с сервисом аутентификации (Auth Service).
    - Применяет JWT для защиты маршрутов.
    - Использует WebFlux для реактивной обработки запросов.

- **auth-service**:
    - Реализует регистрацию, вход, выдачу JWT.
    - Хранит пользователей в PostgreSQL с помощью Spring Data JPA.
    - Использует Spring Security для управления доступом.

- **user-service**:
    - Позволяет управлять пользователями (CRUD).
    - Защищён авторизацией через JWT.
    - Также использует Spring Data JPA и PostgreSQL.


## Контакты

Автор: [CyberJhin](https://github.com/CyberJhin)
