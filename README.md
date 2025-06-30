**# 🏦 Virtual Bank — Spring Boot проект

Проект "Virtual Bank" — это учебное веб-приложение для работы с пользователями и их банковскими счетами. Реализованы CRUD-операции, переводы между счетами и валидация данных. Проект построен на основе Spring Boot.**


📌 Основной функционал
✅ Регистрация пользователя

✅ Создание банковского счёта

✅ CRUD-операции для пользователей и счетов

✅ Переводы:

по ID аккаунта

по номеру телефона

по ID пользователя

✅ Проверка блокировки счёта

✅ Генерация уникального ID аккаунта

✅ Обработка ошибок и валидация

🛠️ Как запустить
📦 Через IDE (IntelliJ IDEA)
Склонируй проект:

bash
Копировать
Редактировать
git clone https://github.com/yourusername/virtual-bank.git
Открой в IntelliJ IDEA

Запусти BankApplication.java

🐘 Через Gradle
bash
Копировать
Редактировать
./gradlew bootRun
🧪 Тестовые данные
Все данные хранятся в памяти (H2) и очищаются при перезапуске

Консоль H2 доступна по адресу: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:testdb

User: sa, Password: (пусто)

🧪 Примеры запросов
➕ Создание пользователя
http
Копировать
Редактировать
POST /users
Content-Type: application/json

{
  "username": "john_doe",
  "password": "12345",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+123456789"
}
💰 Перевод между аккаунтами
http
Копировать
Редактировать
POST /accounts/transfer
Content-Type: application/json

{
  "fromAccId": 10000001,
  "toAccId": 10000002,
  "amount": 250.00,
  "comment": "Перевод другу"
}
✅ Планы на развитие
🔐 Добавить Spring Security + JWT

📊 Добавить сущность Transaction для логирования операций

🌐 Подключить Swagger/OpenAPI

🐳 Docker-файл и деплой

🧪 Написание Unit и Integration тестов

👥 Роли и авторизация (user/admin)
