import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import types.Login;
import types.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class CreateUserTest extends AbstractBaseApi {

    private String existingEmail = "Преображенскася_11@yandex.ru"; // Существующий email для тестов

    @Before
    public void setUp() {
        setResource("/auth/register");

        // Генерация уникальных данных для пользователя
        user = getRandomUser();
    }

    @Test
    @DisplayName("Проверка создания пользователя")
    public void testCreateUser() {
        testCreateUniqueUser();
        deleteActiveUser(user);
        testCreateUserWithExistingUsername();
        testCreateUserWithoutRequiredField();
    }

    @Step("Создание уникального пользователя")
    public void testCreateUniqueUser() {
        createDefinedUser(getRandomUser());
    }

    @Step("Попытка создать пользователя с уже зарегистрированным логином")
    public void testCreateUserWithExistingUsername() {
        // Попытка регистрации с существующим email
        User existingUser = new User(existingEmail, user.getPassword(), user.getName());

        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(existingUser)
                .log().all()
                .when()
                .post(resource)
                .then()
                .log().all()
                .assertThat()
                .statusCode(403)  // Код ошибки для существующего пользователя
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Step("Создание пользователя без обязательного поля (email)")
    public void testCreateUserWithoutRequiredField() {
        // Попытка создать пользователя без email
        User localUser = new User(null, "12365", "NewUser");  // Отсутствует email

        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(localUser)
                .log().all()
                .when()
                .post(resource)
                .then()
                .log().all()
                .assertThat()
                .statusCode(403)  // Код ошибки из-за отсутствия обязательного поля
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
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
        testCreateUniqueUser();

        Login login = new Login(user.getEmail(), user.getPassword());

        // Логин с правильными данными
        ValidatableResponse response = given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(login)
                .log().all()
                .when()
                // TODO other test
                .post("/auth/login")
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
                // TODO other test
                .post("/auth/login")
                .then()
                .log().all()
                .assertThat()
                .statusCode(401)  // Ошибка 401 при неверных данных
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Проверка обновления данных пользователя")
    public void testUpdateUser() {
        testUpdateUserWithAuthorization();
        testUpdateUserWithoutAuthorization();
    }

    @Step("Изменение данных пользователя с авторизацией")
    public void testUpdateUserWithAuthorization() {
        // Логин с правильными данными для получения токена
        testLoginWithValidCredentials();

        Login userUpdate = new User(user.getEmail(), null, "updatedUser");

        // Обновление данных пользователя с авторизацией
        ValidatableResponse response = given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .header("Authorization", authToken)  // Передача токена в заголовке
                .body(userUpdate)
                .log().all()
                .when()
                // TODO other test
                .patch("/auth/user")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)  // Успешное обновление
                .body("success", equalTo(true))
                .body("user.name", equalTo("updatedUser"))  // Проверка нового имени
                .body("user.email", equalTo(user.getEmail()));  // Проверка нового email
    }

    @Step("Изменение данных пользователя без авторизации")
    public void testUpdateUserWithoutAuthorization() {
        User localUser = new User("newemail@example.com", null, "updatedUser");

        // Попытка обновления данных без авторизации
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(localUser)
                .log().all()
                .when()
                // TODO other test
                .patch("/auth/user")
                .then()
                .log().all()
                .assertThat()
                .statusCode(401)  // Ошибка 401 без авторизации
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void tearDown() {
        // Очистка данных после теста (удаление пользователя, если был токен)
        deleteActiveUser(user);
    }
}
