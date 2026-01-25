# Калькулятор отпускных (Vacation Pay Calculator)

Микросервис на **Spring Boot** и **Java 11**, вычисляющий сумму отпускных по средней зарплате и длительности отпуска. Поддерживает расчёт по фиксированному количеству дней и по периоду с учётом нерабочих праздничных дней.

---

## Содержание

- [Описание](#описание)
- [Требования и реализация](#требования-и-реализация)
- [Технологии](#технологии)
- [Структура проекта](#структура-проекта)
- [Запуск](#запуск)
- [API](#api)
- [Бизнес-логика](#бизнес-логика)
- [Тестирование](#тестирование)

---

## Описание

Сервис предоставляет единственный HTTP endpoint **`GET /calculate`**, который принимает:

- **Среднюю зарплату** за 12 месяцев;
- **Количество дней отпуска** **или** **период отпуска** (даты начала и окончания).

В ответ возвращается сумма отпускных в рублях (с точностью до копеек).

При указании точных дат отпуска расчёт учитывает **нерабочие праздничные дни** российского производственного календаря: такие дни не оплачиваются.

---

## Требования и реализация

| Требование | Реализация |
|------------|------------|
| Микросервис на Spring Boot + Java 11 | ✅ Spring Boot 2.7.18, Java 11 |
| API `GET /calculate` | ✅ `GET /calculate`, query-параметры |
| Средняя зарплата за 12 месяцев + дни отпуска → сумма отпускных | ✅ Режим «по дням»: `averageSalary`, `vacationDays` |
| Доп.: точные даты отпуска → учёт праздников | ✅ Режим «по датам»: `startDate`, `endDate`, `HolidayService` |
| Чистота кода, структура, имена, паттерны | ✅ Слои controller / service / dto, валидация, DTO, единый обработчик ошибок |
| Юнит-тесты расчёта | ✅ `VacationPayCalculatorServiceImplTest`, `VacationPayCalculatorControllerTest` |

---

## Технологии

- **Java 11**
- **Spring Boot 2.7.18** (Web, Validation)
- **Gradle 7.5** (wrapper)
- **Lombok**
- **JUnit 5**, **Mockito**, **MockMvc** (тесты)

---

## Структура проекта

```
src/main/java/com/example/vacation_pay_calculator/
├── VacationPayCalculatorApplication.java    # Точка входа
├── controller/
│   ├── VacationPayCalculatorController.java # GET /calculate
│   └── advice/
│       └── GlobalExceptionHandler.java      # Обработка ошибок (400/500)
├── dto/
│   ├── CalculateVacationPayRequest.java     # Запрос (валидация)
│   └── CalculateVacationPayResponse.java    # Ответ { vacationPay }
└── service/
    ├── VacationPayCalculatorService.java    # Интерфейс расчёта
    ├── HolidayService.java                  # Интерфейс «праздник или нет»
    └── impl/
        ├── VacationPayCalculatorServiceImpl.java  # Формула, два режима
        └── HolidayServiceImpl.java               # Праздники 2026 (РФ)
```

- **Controller** — приём запроса, `@Valid`, вызов сервиса.
- **DTO** — `CalculateVacationPayRequest` (поля + Bean Validation), `CalculateVacationPayResponse` (только `vacationPay`).
- **Service** — расчёт по дням или по датам, обращение к `HolidayService` при расчёте по периоду.
- **GlobalExceptionHandler** — `BindException` (ошибки валидации), `IllegalArgumentException` (ошибки сервиса), общий `Exception` → 400/500 и единый JSON-формат ответа.

---

## Запуск

### Требования

- **JDK 11** или выше  
- **Gradle** (достаточно wrapper в проекте)

### Сборка

```bash
./gradlew build
```

или на Windows:

```bash
gradlew.bat build
```

### Запуск приложения

```bash
./gradlew bootRun
```

Сервис поднимается на **порту 8080**. Базовый URL: `http://localhost:8080`.

### Только тесты

```bash
./gradlew test
```

---

## API

### `GET /calculate`

Вычисляет сумму отпускных.

#### Параметры запроса (query)

| Параметр | Тип | Обязательный | Описание |
|----------|-----|--------------|----------|
| `averageSalary` | число | да | Средняя зарплата за 12 месяцев (строго больше 0) |
| `vacationDays` | целое | да*, если нет дат | Количество дней отпуска (≥ 1) |
| `startDate` | дата (ISO 8601) | да**, если есть `endDate` | Дата начала отпуска |
| `endDate` | дата (ISO 8601) | да**, если есть `startDate` | Дата окончания отпуска |

\* Если не переданы `startDate` и `endDate`, обязательно указывать `vacationDays`.  
\** Если передаётся период, нужны **оба** поля. Праздники в периоде не оплачиваются.

#### Режимы расчёта

1. **По количеству дней**  
   Заданы только `averageSalary` и `vacationDays`. Сумма = (средняя зарплата / 29.3) × количество дней.

2. **По периоду**  
   Заданы `averageSalary`, `startDate`, `endDate`. Считаются календарные дни в периоде минус праздники. Сумма = (средняя зарплата / 29.3) × оплачиваемые дни.

#### Примеры запросов

**Расчёт по дням (14 дней, зарплата 40 000 ₽):**

```http
GET /calculate?averageSalary=40000&vacationDays=14
```

**Расчёт по датам (отпуск 12–16 мая 2026):**

```http
GET /calculate?averageSalary=40000&startDate=2026-05-12&endDate=2026-05-16
```

В режиме по датам `vacationDays` можно не передавать (оно игнорируется).

**Пример через curl:**

```bash
curl "http://localhost:8080/calculate?averageSalary=40000&vacationDays=14"
```

#### Успешный ответ (200 OK)

```json
{
  "vacationPay": 19112.63
}
```

`vacationPay` — сумма отпускных в рублях, два знака после запятой.

#### Ошибки

**400 Bad Request** — ошибки валидации (например, отрицательная зарплата, не заданы обязательные поля, некорректные даты):

```json
{
  "timestamp": "2026-01-24T12:00:00",
  "status": 400,
  "error": "Validation failed",
  "details": {
    "averageSalary": "The average salary should be a positive number",
    "vacationDays": "The number of vacation days must be at least 1"
  }
}
```

или для ошибок бизнес-логики (например, «конец раньше начала»):

```json
{
  "timestamp": "2026-01-24T12:00:00",
  "status": 400,
  "error": "Invalid input",
  "message": "The end date must be later than or equal to the start date"
}
```

**500 Internal Server Error** — непредвиденная ошибка:

```json
{
  "timestamp": "2026-01-24T12:00:00",
  "status": 500,
  "error": "Internal server error",
  "message": "An unexpected error occurred. Please check your request."
}
```

---

## Бизнес-логика

### Формула

- Используется **среднее количество календарных дней в месяце 29.3** (по ТК РФ).
- Дневная ставка:
  ```
  дневная_ставка = средняя_зарплата / 29.3
  ```
- Сумма отпускных:
  ```
  отпускные = дневная_ставка × количество_оплачиваемых_дней
  ```
- Округление — до копеек, по правилу **half-up** (`BigDecimal`).

### Праздники (режим «по датам»)

При расчёте по `startDate` и `endDate` нерабочие праздничные дни **не входят** в оплачиваемые. Список праздников задан в `HolidayServiceImpl` по **производственному календарю РФ** (например, consultant.ru) и по умолчанию настроен на **2026 год** (новогодние каникулы, 23 февраля, 8 марта, 1 и 9 мая, 12 июня, 4 ноября, 31 декабря и т.д.).

---

## Тестирование

### Что покрыто

- **`VacationPayCalculatorApplicationTests`** — подъём контекста Spring (`@SpringBootTest`).
- **`VacationPayCalculatorControllerTest`** (`@WebMvcTest`) — вызов `GET /calculate`:
  - успешный расчёт по `averageSalary` и `vacationDays`;
  - 400 при отрицательной зарплате и при `vacationDays = 0`.
- **`VacationPayCalculatorServiceImplTest`** (юнит-тесты с моком `HolidayService`):
  - расчёт по количеству дней (типовой случай, 1 день);
  - расчёт по датам без праздников;
  - расчёт по датам с исключением праздника (например, 23 февраля);
  - исключения при отрицательной зарплате, нуле дней, `endDate` раньше `startDate`.

### Запуск тестов

```bash
./gradlew test
```

Отчёт по тестам: `build/reports/tests/test/index.html`.

---
