import api.UserApi;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import types.Login;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class LoginUserTest extends AbstractBaseApi {

    @Before
    public void setUp() {
        initialize();

        // Создаем уникального пользователя
        createDefinedUser(user = getRandomUser());

        Assert.assertNotNull("Токен авторизации не задан для тестов", authToken);
    }

    @Test
    @DisplayName("Логин пользователя с существующим пользователем")
    @Description("Логин пользователя с существующим пользователем")
    public void testLoginUser() {
        // Сначала регистрируем нового пользователя
        createDefinedUser(getRandomUser());

        Login login = new Login(user.getEmail(), user.getPassword());

        // Логин с правильными данными
        ValidatableResponse response =
                UserApi.loginUser(login)
                        .assertThat()
                        .statusCode(SC_OK)
                        .body("success", equalTo(true))
                        .body("user.email", equalTo(user.getEmail()))  // Проверка email
                        .body("user.name", equalTo(user.getName()))  // Проверка имени
                        .body("accessToken", notNullValue())  // Проверка наличия токенов
                        .body("refreshToken", notNullValue());

        // Сохраняем токен для дальнейших тестов
        authToken = response.extract().path("accessToken");
    }

    @Test
    @DisplayName("Логин с неверным логином")
    @Description("Логин с неверным логином")
    public void testLoginWithInvalidUsername() {
        // Попытка логина с неверными данными
        Login login = new Login("wrong@example.com", user.getPassword());

        UserApi.loginUser(login)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)  // Ошибка 401 при неверных данных
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Логин с неверным паролем")
    @Description("Логин с неверным паролем")
    public void testLoginWithInvalidPassword() {
        // Попытка логина с неверными данными
        Login login = new Login(user.getEmail(), "wrongpassword");

        UserApi.loginUser(login)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)  // Ошибка 401 при неверных данных
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @After
    public void tearDown() {
        // Очистка данных после теста (удаление пользователя, если был токен)
        deleteDefinedUser(user);
    }
}
