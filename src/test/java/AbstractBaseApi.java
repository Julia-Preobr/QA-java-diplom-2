import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import types.User;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public abstract class AbstractBaseApi {
    protected String resource;
    protected String authToken; // Токен авторизации для тестов, требующих авторизации

    protected User user; // объект ApiUser для теста

    @Step("Ручка: {0}")
    protected void setResource(String resource) {
        // Инициализация RestAssured
        RestAssured.baseURI = Base.API_URL;

        this.resource = resource;
    }

    @Step("Генерация данных уникального пользователя")
    protected User getRandomUser() {
        // Генерация слйчайных данных для пользователя
        String username = RandomStringUtils.randomAlphanumeric(8, 15);  // уникальное имя
        String password = RandomStringUtils.randomAlphanumeric(8, 15);  // стандартный пароль
        String email = RandomStringUtils.randomAlphanumeric(8, 15).toLowerCase() + "@yandex.ru";  // email пользователя

        return new User(email, password, username);
    }

    @Step("Регистрация пользователя: {0}")
    protected void createDefinedUser(User user) {
        Assert.assertNotNull("Пользователь для тестов не задан", user);

        ValidatableResponse response = given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(user)
                .log().all()
                .when()
                .post("/auth/register")
                .then()
                .log().all()
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true))  // Успех
                .body("user.email", equalTo(user.getEmail()))  // Проверка email
                .body("user.name", equalTo(user.getName()))  // Проверка имени
                .body("accessToken", notNullValue())  // Проверка наличия токенов
                .body("refreshToken", notNullValue());

        // Сохраняем токен для дальнейших тестов
        authToken = response.extract().path("accessToken");
    }

    @Step("Проверка наличия активного пользователя, удаление пользователя: {0}")
    protected void deleteDefinedUser(User user) {
        if (authToken != null) {
            deleteActiveUser(user);
        }
    }

    @Step("Удаление пользователя: {0}")
    protected void deleteActiveUser(User user) {
        String authValue = authToken;

        authToken = null;

        given()
                .filter(new AllureRestAssured())
                .header("Authorization", authValue)  // Авторизация с использованием токена
                .log().all()
                .when()
                .delete("/auth/user")
                .then()
                .log().all()
                .assertThat()
                .statusCode(SC_ACCEPTED);  // Успешное удаление
    }
}
