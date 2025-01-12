import api.UserApi;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Test;
import types.User;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.Matchers.equalTo;

public class CreateUserTest extends AbstractBaseApi {

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Создание уникального пользователя")
    public void testCreateUniqueUser() {
        createDefinedUser(user = getRandomUser());
        deleteDefinedUser(user);
    }

    @Test
    @DisplayName("Попытка создать пользователя с уже зарегистрированным логином")
    public void testCreateUserWithExistingUsername() {
        createDefinedUser(user = getRandomUser());

        // Попытка регистрации с существующим email
        User existingUser = user;

        UserApi.createUser(existingUser)
                .assertThat()
                .statusCode(SC_FORBIDDEN)  // Код ошибки для существующего пользователя
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));

        deleteDefinedUser(user);
    }

    @Test
    @DisplayName("Попытка создать пользователя без email")
    public void testCreateUserWithoutEmail() {
        stepCreateUserWithoutRequiredField(new User(null, "12365", "NewUser"));
    }

    @Test
    @DisplayName("Попытка создать пользователя без name")
    public void testCreateUserWithoutName() {
        stepCreateUserWithoutRequiredField(new User("ahjgsdfjhgasf@yandex.ru", "12365", null));
    }

    @Test
    @DisplayName("Попытка создать пользователя без password")
    public void testCreateUserWithoutPassword() {
        stepCreateUserWithoutRequiredField(new User("ahjgsdfjhgasf@yandex.ru", null, "NewUser"));
    }

    @Step("Создание пользователя без обязательного поля (user: {0})")
    public void stepCreateUserWithoutRequiredField(User localUser) {
        UserApi.createUser(localUser)
                .assertThat()
                .statusCode(SC_FORBIDDEN)  // Код ошибки из-за отсутствия обязательного поля
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        // Очистка данных после теста (удаление пользователя, если был токен)
        deleteDefinedUser(user);
    }
}
