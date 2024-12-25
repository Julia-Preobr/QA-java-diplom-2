import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import types.User;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

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

    @After
    public void tearDown() {
        // Очистка данных после теста (удаление пользователя, если был токен)
        deleteActiveUser(user);
    }
}
