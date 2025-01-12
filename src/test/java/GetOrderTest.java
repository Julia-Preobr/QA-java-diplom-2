import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;

public class GetOrderTest extends AbstractBaseApi {

    @Before
    public void setUp() {
        setResource("/orders");

        createDefinedUser(getRandomUser());

        Assert.assertNotNull("Токен авторизации не задан для тестов", authToken);
    }

    @Test
    @DisplayName("Получение заказов с авторизацией")
    @Description("Получение заказов с авторизацией")
    public void getOrdersWithAuth() {
        given()
                .filter(new AllureRestAssured())
                .header("Authorization", authToken)
                .when()
                .get(resource)  // Получаем заказ по ID
                .then()
                .log().all()
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("orders.size()", equalTo(0));  // Проверяем, что список существует, но пустой
    }

    @Test
    @DisplayName("Получение заказов без авторизации")
    @Description("Получение заказов без авторизации")
    public void getOrdersWithoutAuth() {
        given()
                .filter(new AllureRestAssured())
                .when()
                .get(resource)  // Получаем заказ по ID
                .then()
                .log().all()
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @After
    public void tearDown() throws Exception {
        // Очистка данных после теста (удаление пользователя, если был токен)
        if (authToken != null) {
            deleteDefinedUser(user);
        }
    }
}

