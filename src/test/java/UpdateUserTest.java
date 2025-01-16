import api.UserApi;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import types.User;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class UpdateUserTest extends AbstractBaseApi {

    @Before
    public void setUp() {
        initialize();

        // Создаем уникального пользователя
        createDefinedUser(user = getRandomUser());

        Assert.assertNotNull("Токен авторизации не задан для тестов", authToken);
    }

    @Test
    @DisplayName("Изменение имени пользователя с авторизацией")
    @Description("Изменение имени пользователя с авторизацией")
    public void testUpdateUserName() {
        User userUpdate = new User(null, "updatedUser");

        // Обновление данных пользователя с авторизацией
        UserApi.updateUser(userUpdate, authToken)
                .assertThat()
                .statusCode(SC_OK)  // Успешное обновление
                .body("success", equalTo(true))
                .body("user.name", equalTo("updatedUser"))  // Проверка нового имени
                .body("user.email", equalTo(user.getEmail()));  // email не изменился
    }

    @Test
    @DisplayName("Изменение email пользователя с авторизацией")
    @Description("Изменение email пользователя с авторизацией")
    public void testUpdateUserEmail() {
        String updatedEmail = getRandomEmail();
        User userUpdate = new User(updatedEmail, null);

        // Обновление данных пользователя с авторизацией
        UserApi.updateUser(userUpdate, authToken)
                .assertThat()
                .statusCode(SC_OK)  // Успешное обновление
                .body("success", equalTo(true))
                .body("user.name", equalTo(user.getName()))  // Имя не изменилось
                .body("user.email", equalTo(updatedEmail));  // Проверка нового email
    }

    @Test
    @DisplayName("Изменение email пользователя на существующий email с авторизацией")
    @Description("Изменение email пользователя на существующий email с авторизацией")
    public void testUpdateUserWithExistingEmail() {
        User existingUser = getRandomUser();

        ValidatableResponse createExistingUserResponse =
                UserApi.createUser(existingUser)
                        .assertThat()
                        .statusCode(SC_OK)
                        .body("success", equalTo(true))  // Успех
                        .body("user.email", equalTo(existingUser.getEmail()))  // Проверка email
                        .body("accessToken", notNullValue())  // Проверка наличия токенов
                        .body("refreshToken", notNullValue());

        String secondAuthToken = createExistingUserResponse.extract().path("accessToken");

        User userUpdate = new User(existingUser.getEmail(), null);

        // Обновление данных пользователя с авторизацией
        UserApi.updateUser(userUpdate, authToken)
                .assertThat()
                .statusCode(SC_FORBIDDEN)  // Неверные данные запроса на обновление
                .body("success", equalTo(false))
                .body("message", equalTo("User with such email already exists"));

        UserApi.deleteUser(secondAuthToken);
    }

    @Test
    @DisplayName("Изменение email пользователя на пустое значение с авторизацией")
    @Description("Изменение email пользователя на пустое значение с авторизацией")
    public void testUpdateUserEmptyEmail() {
        String updatedEmail = "";
        User userUpdate = new User(updatedEmail, null);

        // Обновление данных пользователя с авторизацией
        UserApi.updateUser(userUpdate, authToken)
                .assertThat()
                .statusCode(SC_BAD_REQUEST)  // Неверные данные запроса на обновление
                .body("success", equalTo(false));
    }

    @Test
    @DisplayName("Изменение данных пользователя без авторизации")
    @Description("Изменение данных пользователя без авторизации")
    public void testUpdateUserWithoutAuthorization() {
        User localUser = new User("newemail@example.com", null, "updatedUser");

        // Попытка обновления данных без авторизации
        UserApi.updateUser(localUser, null)
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
