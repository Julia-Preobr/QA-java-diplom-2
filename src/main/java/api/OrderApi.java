package api;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import types.Order;

import static api.UserApi.BEARER_HEADER;
import static io.restassured.RestAssured.given;

public class OrderApi {

    public static ValidatableResponse createOrder(Order order, String authToken) {
        return getBaseRequestSpecification(order, authToken)  // Тело запроса с ингредиентами
                .log().all()
                .when()
                .post("/orders")  // URL для создания заказа
                .then()
                .log().all();
    }

    public static ValidatableResponse getOrders(String authToken) {
        return getBaseRequestSpecification(null, authToken)  // Тело запроса с ингредиентами
                .log().all()
                .when()
                .get("/orders")  // URL для получения заказов
                .then()
                .log().all();
    }

    public static RequestSpecification getBaseRequestSpecification(Order order, String authToken) {
        RequestSpecification requestSpecification = given().filter(new AllureRestAssured());
        if (order != null) {
            requestSpecification
                    .header("Content-type", "application/json")
                    .body(order);
        }
        if (authToken != null) {
            if (!authToken.startsWith(BEARER_HEADER)) {
                authToken = BEARER_HEADER + authToken;
            }
            requestSpecification = requestSpecification.header("Authorization", authToken);  // Заголовок с токеном авторизации
        }
        return requestSpecification;
    }
}
