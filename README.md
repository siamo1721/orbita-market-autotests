# OrbitaMarket — автотесты

**Отдельный репозиторий** автотестов для дипломного проекта OrbitaMarket (ТЗ п. 7.2).

Тестируется **API Gateway** (`http://localhost:8080`), не прямые порты сервисов.

## Что покрыто

### REST-эндпоинты (минимум 1 успешный тест на каждый)

| Сервис | Метод | Путь | Класс |
|--------|-------|------|-------|
| Payments | POST | `/payments/accounts` | `PaymentsApiTest` |
| Payments | POST | `/payments/accounts/top-up` | `PaymentsApiTest` |
| Payments | GET | `/payments/accounts/balance` | `PaymentsApiTest` |
| Orders | POST | `/orders/orders` | `OrdersApiTest` |
| Orders | GET | `/orders/orders` | `OrdersApiTest` |
| Orders | GET | `/orders/orders/{order_id}` | `OrdersApiTest` |

### Сценарии чек-листа 7.1

| Сценарий | Класс |
|----------|-------|
| Happy path → PAID, balance 880 | `Scenarios71Test` |
| Недостаточно средств → PAYMENT_FAILED | `Scenarios71Test` |
| Два заказа по 400 при 1000 | `Scenarios71Test` |
| Повторный POST /accounts → 409 | `Scenarios71Test` |

## Требования

- Java 21
- OrbitaMarket поднят локально:

```bash
# в репозитории orbita-market
docker compose up --build -d
```

Дождись старта всех сервисов (~30–60 сек).

## Запуск тестов

```bash
cd orbita-market-autotests
./gradlew test
```

Другой URL Gateway:

```bash
./gradlew test -Dorbita.base.url=http://localhost:8080
```

## Allure-отчёт

```bash
./gradlew test allureReport
./gradlew allureServe
```

Откроется браузер с отчётом по прогону.

Артефакты:
- `build/allure-results/` — сырые результаты
- `build/reports/allure-report/` — HTML-отчёт

## Стек

- Java 21
- JUnit 5
- RestAssured
- Allure
- Awaitility (ожидание async Kafka → PAID)

## Структура

```
orbita-market-autotests/
├── build.gradle
├── README.md
└── src/test/java/org/mainshop/
    ├── config/BaseApiTest.java
    └── tests/
        ├── PaymentsApiTest.java
        ├── OrdersApiTest.java
        └── Scenarios71Test.java
```

## Публикация репозитория

```bash
cd orbita-market-autotests
git init
git add .
git commit -m "Add OrbitaMarket API autotests with Allure"
git remote add origin <your-github-url>
git push -u origin main
```

> По ТЗ автотесты **не должны** лежать внутри репозитория микросервисов — этот проект отдельный.

## Связь с основным проектом

| Репозиторий | Назначение |
|-------------|------------|
| `orbita-market` | микросервисы, docker-compose |
| `orbita-market-autotests` | API-тесты + Allure |

---

*Учебный проект, ВШЭ/диплом — OrbitaMarket.*
