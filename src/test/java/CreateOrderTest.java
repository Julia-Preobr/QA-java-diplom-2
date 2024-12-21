import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CreateOrderTest {

    private String authToken;  // Токен авторизации для авторизованных запросов

    @Before
    public void setUp() {
        // Инициализация RestAssured
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api";

        // Получение токена (пример получения, зависит от реализации API)
        authToken = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjY3MzczNGJmOWVkMjgwMDAxYjUxMDQwMCIsImlhdCI6MTczNDA5OTQ1OSwiZXhwIjoxNzM0MTAwNjU5fQ.EtfMJuFy2-q3RTJo-9NKrV3zVZEvZQILLgebocSuHSI";
    }


    @Test
    @Step("Создание заказа с авторизацией")
    public void createOrderWithAuth() {
        String orderJson = "{ \"ingredients\": [\"60d3463f7034a000269f45e7\", \"60d3463f7034a000269f45e9\"] }";

        // Выполнение POST-запроса для создания заказа с авторизацией
        Response response = given()
                .header("Authorization", "Bearer " + authToken)  // Заголовок с токеном авторизации
                .body(orderJson)  // Тело запроса с ингредиентами
                .when()
                .post("/orders")  // URL для создания заказа
                .then()
                .statusCode(200)  // Ожидаемый статус код для успешного создания заказа
                .body("success", equalTo(true))  // Проверка успешного ответа
                .body("order.ingredients.size()", equalTo(2))  // Проверка размера ингредиентов
                .extract()
                .response();

        // Используем переменную response для извлечения ID заказа и других данных из ответа
        String orderId = response.jsonPath().getString("order._id");  // Извлекаем ID нового заказа
        System.out.println("Created order ID: " + orderId);

        // Дополнительные проверки могут быть добавлены здесь, например, проверка того, что заказ был создан с правильным статусом.
        given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/orders/" + orderId)  // Получаем заказ по ID
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("order._id", equalTo(orderId));  // Проверяем, что полученный заказ имеет тот же ID
    }

    @Test
    @Step("Создание заказа без авторизации")
    public void createOrderWithoutAuth() {
        String orderJson = "{ \"ingredients\": [\"60d3463f7034a000269f45e7\", \"60d3463f7034a000269f45e9\"] }";

        // Выполнение POST-запроса для создания заказа без авторизации
        given()
                .body(orderJson)  // Тело запроса с ингредиентами
                .when()
                .post("/orders")  // URL для создания заказа
                .then()
                .statusCode(401)  // Ожидаемый статус код для неавторизованного пользователя
                .body("success", equalTo(false))  // Проверка, что ответ не успешен
                .body("message", equalTo("You should be authorised"));  // Проверка сообщения об ошибке
    }

    @Test
    @Step("Создание заказа с неверным хешем ингредиентов")
    public void createOrderWithInvalidIngredientsHash() {
        String orderJson = "{ \"ingredients\": [\"invalid_hash\"] }";  // Некорректный хеш ингредиентов

        // Выполнение POST-запроса для создания заказа с неверными ингредиентами
        given()
                .header("Authorization", "Bearer " + authToken)  // Заголовок с токеном авторизации
                .body(orderJson)  // Тело запроса с неверными ингредиентами
                .when()
                .post("/orders")  // URL для создания заказа
                .then()
                .statusCode(500);  // Ожидаемый статус код для неправильных данных

    }
    @Test
    @Step("Создание заказа с ингредиентами")
    public void createOrderWithIngredients() {
        String orderJson = "{ \"ingredients\": [\"60d3463f7034a000269f45e7\", \"60d3463f7034a000269f45e9\"] }";  // Пример с ингредиентами

        // Отправка POST-запроса для создания заказа
        Response response = given()
                .header("Authorization", "Bearer " + authToken)  // Заголовок с авторизацией
                .body(orderJson)  // Тело запроса с ингредиентами
                .when()
                .post("/orders")  // Путь для создания заказа
                .then()
                .statusCode(200)  // Ожидаемый код ответа при успешном создании заказа
                .body("success", equalTo(true))  // Проверка, что заказ успешно создан
                .body("order.ingredients.size()", equalTo(2))  // Проверка, что количество ингредиентов в заказе равно 2
                .extract()
                .response();

        // Логирование тела ответа для отладки
        System.out.println("Response Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.getBody().asString());
    }

    @Test
    @Step("Создание заказа без ингредиентов")
    public void createOrderWithoutIngredients() {
        String orderJson = "{ \"ingredients\": [] }";  // Тело запроса без ингредиентов

        // Отправка POST-запроса для создания заказа
        Response response = given()
                .header("Authorization", "Bearer " + authToken)  // Заголовок с авторизацией
                .body(orderJson)  // Тело запроса без ингредиентов
                .when()
                .post("/orders")  // Путь для создания заказа
                .then()
                .statusCode(400)  // Ожидаемый код ошибки при отсутствии ингредиентов
                .body("success", equalTo(false))  // Проверка, что заказ не был создан
                .extract()
                .response();

        // Логирование тела ответа для отладки
        System.out.println("Response Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.getBody().asString());
    }
}

