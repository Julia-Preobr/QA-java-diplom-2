import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import types.Login;
import types.User;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;

public class UpdateUserTest extends AbstractBaseApi {

    @Before
    public void setUp() {
        setResource("/auth/user");

        // Создаем уникального пользователя
        createDefinedUser(user = getRandomUser());

        Assert.assertNotNull("Токен авторизации не задан для тестов", authToken);
    }

    @Test
    @DisplayName("Проверка обновления данных пользователя")
    public void testUpdateUser() {
        testUpdateUserWithAuthorization();
        testUpdateUserWithoutAuthorization();
    }

    @Step("Изменение данных пользователя с авторизацией")
    public void testUpdateUserWithAuthorization() {
        Login userUpdate = new User(user.getEmail(), null, "updatedUser");

        // Обновление данных пользователя с авторизацией
        ValidatableResponse response = given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .header("Authorization", authToken)  // Передача токена в заголовке
                .body(userUpdate)
                .log().all()
                .when()
                .patch(resource)
                .then()
                .log().all()
                .assertThat()
                .statusCode(SC_OK)  // Успешное обновление
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
                .patch(resource)
                .then()
                .log().all()
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)  // Ошибка 401 без авторизации
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void tearDown() {
        // Очистка данных после теста (удаление пользователя, если был токен)
        deleteDefinedUser(user);
    }
}
