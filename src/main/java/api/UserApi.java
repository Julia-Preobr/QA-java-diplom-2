package api;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import types.Login;
import types.User;

import static io.restassured.RestAssured.given;

public class UserApi {

    public static final String BEARER_HEADER = "Bearer ";

    public static ValidatableResponse createUser(User user) {
        return given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(user)
                .log().all()
                .when()
                .post("/auth/register")
                .then()
                .log().all();
    }

    public static ValidatableResponse loginUser(Login login) {
        return given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(login)
                .log().all()
                .when()
                .post("/auth/login")
                .then()
                .log().all();
    }

    public static ValidatableResponse logoutUser(String authToken) {
        if (!authToken.startsWith(BEARER_HEADER)) {
            authToken = BEARER_HEADER + authToken;
        }
        return given()
                .filter(new AllureRestAssured())
                .header("Authorization", authToken)  // Авторизация с использованием токена
                .log().all()
                .when()
                .delete("/auth/user")
                .then()
                .log().all();
    }

    public static ValidatableResponse updateUser(User user, String authToken) {
        RequestSpecification requestSpecification =
                given()
                        .filter(new AllureRestAssured())
                        .header("Content-type", "application/json")
                        .body(user);
        if (authToken != null) {
            if (!authToken.startsWith(BEARER_HEADER)) {
                authToken = BEARER_HEADER + authToken;
            }
            requestSpecification = requestSpecification.header(
                    "Authorization", authToken
            );  // Передача токена в заголовке
        }
        return requestSpecification
                .log().all()
                .when()
                .patch("/auth/user")
                .then()
                .log().all();
    }
}
