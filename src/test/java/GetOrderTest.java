import api.OrderApi;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import types.Order;

import java.util.List;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.equalTo;

public class GetOrderTest extends AbstractBaseApi {

    @Before
    public void setUp() {
        initialize();

        createDefinedUser(getRandomUser());

        Assert.assertNotNull("Токен авторизации не задан для тестов", authToken);
    }

    @Test
    @DisplayName("Получение заказов с авторизацией")
    @Description("Получение заказов с авторизацией")
    public void testGetOrdersWithAuth() {
        createTestOrder();

        OrderApi.getOrders(authToken)
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("orders.size()", equalTo(1));  // Проверяем, что список существует, но пустой
    }

    @Test
    @DisplayName("Получение заказов без авторизации")
    @Description("Получение заказов без авторизации")
    public void testGetOrdersWithoutAuth() {
        OrderApi.getOrders(null)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    private void createTestOrder() {
        Order newOrder = new Order();
        newOrder.setIngredients(List.of("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa72"));

        OrderApi.createOrder(newOrder, authToken)
                .assertThat()
                .statusCode(SC_OK)  // Ожидаемый статус код для успешного создания заказа
                .body("success", equalTo(true))  // Проверка успешного ответа
                .body("order.ingredients.size()", equalTo(2));  // Проверка размера ингредиентов
    }

    @After
    public void tearDown() throws Exception {
        // Очистка данных после теста (удаление пользователя, если был токен)
        if (authToken != null) {
            deleteDefinedUser(user);
        }
    }
}

