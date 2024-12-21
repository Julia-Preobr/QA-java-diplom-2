import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CreateUserTest {

    private ApiUser user; // объект ApiUser для теста
    private String authToken; // Токен авторизации для тестов, требующих авторизации
    private String existingEmail = "Преображенскася_11@yandex.ru"; // Существующий email для тестов

    @Before
    public void setUp() {
        // Генерация уникальных данных для пользователя
        String username = "user" + System.currentTimeMillis();  // уникальное имя
        String password = "password123";  // стандартный пароль
        String email = "Преображенская_11@yandex.ru";  // email пользователя

        user = new ApiUser(username, password, email); // создание нового пользователя
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api"; // базовый URL API
    }

    @Test
    @Step("Создание уникального пользователя")
    public void testCreateUniqueUser() {
        String payload = "{ \"email\": \"" + user.getEmail() + "\", \"password\": \"" + user.getPassword() + "\", \"name\": \"" + user.getUsername() + "\" }";

        // Отправка запроса на регистрацию нового пользователя
        ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))  // Успех
                .body("user.email", equalTo(user.getEmail()))  // Проверка email
                .body("user.name", equalTo(user.getUsername()))  // Проверка имени
                .body("accessToken", notNullValue())  // Проверка наличия токенов
                .body("refreshToken", notNullValue());

        // Сохраняем токен для дальнейших тестов
        authToken = response.extract().path("accessToken");
    }

    @Test
    @Step("Попытка создать пользователя с уже зарегистрированным логином")
    public void testCreateUserWithExistingUsername() {
        // Попытка регистрации с существующим email
        String userPayload = "{ \"email\": \"" + existingEmail + "\", \"password\": \"12345\", \"name\": \"Existing User\" }";

        given()
                .contentType(ContentType.JSON)
                .body(userPayload)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(403)  // Код ошибки для существующего пользователя
                .body("success", equalTo(false))
                .body("error", equalTo("User already exists"));
    }

    @Test
    @Step("Создание пользователя без обязательного поля (email)")
    public void testCreateUserWithoutRequiredField() {
        // Попытка создать пользователя без email
        String userPayload = "{ \"name\": \"NewUser\", \"password\": \"12365\" }";  // Отсутствует email

        given()
                .contentType(ContentType.JSON)
                .body(userPayload)
                .when()
                .post("/auth/register")
                .then()
                .statusCode(403)  // Код ошибки из-за отсутствия обязательного поля
                .body("success", equalTo(false))
                .body("error", equalTo("Email, password and name are required fields"));
    }

    @Test
    @Step("Логин пользователя с правильными данными")
    public void testLoginWithValidCredentials() {
        // Сначала регистрируем нового пользователя
        testCreateUniqueUser();

        String loginPayload = "{ \"email\": \"" + user.getEmail() + "\", \"password\": \"" + user.getPassword() + "\" }";

        // Логин с правильными данными
        ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))  // Проверка email
                .body("user.name", equalTo(user.getUsername()))  // Проверка имени
                .body("accessToken", notNullValue())  // Проверка наличия токенов
                .body("refreshToken", notNullValue());

        // Сохраняем токен для дальнейших тестов
        authToken = response.extract().path("accessToken");
    }

    @Test
    @Step("Логин с неверным логином или паролем")
    public void testLoginWithInvalidCredentials() {
        // Попытка логина с неверными данными
        String loginPayload = "{ \"email\": \"wrong@example.com\", \"password\": \"wrongpassword\" }";

        given()
                .contentType(ContentType.JSON)
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(401)  // Ошибка 401 при неверных данных
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @Step("Изменение данных пользователя с авторизацией")
    public void testUpdateUserWithAuthorization() {
        // Логин с правильными данными для получения токена
        testLoginWithValidCredentials();

        String updatedUserPayload = "{ \"username\": \"updatedUser\", \"email\": \"newemail@example.com\" }";

        // Обновление данных пользователя с авторизацией
        ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + authToken)  // Передача токена в заголовке
                .body(updatedUserPayload)
                .when()
                .put("/auth/user")
                .then()
                .statusCode(200)  // Успешное обновление
                .body("success", equalTo(true))
                .body("user.username", equalTo("updatedUser"))  // Проверка нового имени
                .body("user.email", equalTo("newemail@example.com"));  // Проверка нового email
    }

    @Test
    @Step("Изменение данных пользователя без авторизации")
    public void testUpdateUserWithoutAuthorization() {
        String updatedUserPayload = "{ \"username\": \"updatedUser\", \"email\": \"newemail@example.com\" }";

        // Попытка обновления данных без авторизации
        given()
                .contentType(ContentType.JSON)
                .body(updatedUserPayload)
                .when()
                .put("/auth/user")
                .then()
                .statusCode(401)  // Ошибка 401 без авторизации
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void tearDown() {
        // Очистка данных после теста (удаление пользователя, если был токен)
        if (authToken != null) {
            given()
                    .header("Authorization", "Bearer " + authToken)  // Авторизация с использованием токена
                    .when()
                    .delete("/auth/user")
                    .then()
                    .statusCode(200);  // Успешное удаление
        }
    }
}
