import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import types.Login;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginUserTest extends AbstractBaseApi {

    @Before
    public void setUp() {
        setResource("/auth/login");

        // Создаем уникального пользователя
        createDefinedUser(user = getRandomUser());

        Assert.assertNotNull("Токен авторизации не задан для тестов", authToken);
    }

    @Test
    @DisplayName("Проверка входа пользователя")
    public void testLoginUser() {
        testLoginWithValidCredentials();
        testLoginWithInvalidCredentials();
    }

    @Step("Логин пользователя с существующим пользователем")
    public void testLoginWithValidCredentials() {
        // Сначала регистрируем нового пользователя
        createDefinedUser(getRandomUser());

        Login login = new Login(user.getEmail(), user.getPassword());

        // Логин с правильными данными
        ValidatableResponse response = given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(login)
                .log().all()
                .when()
                .post(resource)
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(user.getEmail()))  // Проверка email
                .body("user.name", equalTo(user.getName()))  // Проверка имени
                .body("accessToken", notNullValue())  // Проверка наличия токенов
                .body("refreshToken", notNullValue());

        // Сохраняем токен для дальнейших тестов
        authToken = response.extract().path("accessToken");
    }

    @Step("Логин с неверным логином или паролем")
    public void testLoginWithInvalidCredentials() {
        // Попытка логина с неверными данными
        Login login = new Login("wrong@example.com", "wrongpassword");

        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(login)
                .log().all()
                .when()
                .post(resource)
                .then()
                .log().all()
                .assertThat()
                .statusCode(401)  // Ошибка 401 при неверных данных
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        // Очистка данных после теста (удаление пользователя, если был токен)
        deleteActiveUser(user);
    }
}
