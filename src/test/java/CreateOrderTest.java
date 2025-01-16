import api.OrderApi;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import types.Order;

import java.util.Collections;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;

public class CreateOrderTest extends AbstractBaseApi {

    @Before
    public void setUp() {
        initialize();

        createDefinedUser(getRandomUser());

        Assert.assertNotNull("Токен авторизации не задан для тестов", authToken);
    }


    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Создание заказа с авторизацией")
    public void testCreateOrderWithAuth() {
        Order newOrder = new Order();
        newOrder.setIngredients(List.of("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa72"));

        // Выполнение POST-запроса для создания заказа с авторизацией
        Response response =
                OrderApi.createOrder(newOrder, authToken)
                        .assertThat()
                        .statusCode(SC_OK)  // Ожидаемый статус код для успешного создания заказа
                        .body("success", equalTo(true))  // Проверка успешного ответа
                        .body("order.ingredients.size()", equalTo(2))  // Проверка размера ингредиентов
                        .extract()
                        .response();

        // Используем переменную response для извлечения ID заказа и других данных из ответа
        String orderId = response.jsonPath().getString("order._id");  // Извлекаем ID нового заказа

        OrderApi.getOrders(authToken)
                .assertThat()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("orders[0]._id", equalTo(orderId));  // Проверяем, что полученный заказ имеет тот же ID
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Создание заказа без авторизации")
    public void testCreateOrderWithoutAuth() {
        Order newOrder = new Order();
        newOrder.setIngredients(List.of("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa72"));

        // Выполнение POST-запроса для создания заказа без авторизации
        OrderApi.createOrder(newOrder, null)
                .assertThat()
                .statusCode(SC_UNAUTHORIZED)  // Ожидаемый статус код для неавторизованного пользователя
                .body("success", equalTo(false))  // Проверка, что ответ не успешен
                .body("message", equalTo("You should be authorised"));  // Проверка сообщения об ошибке
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Создание заказа с неверным хешем ингредиентов")
    public void testCreateOrderWithInvalidIngredientsHash() {
        Order newOrder = new Order();
        newOrder.setIngredients(List.of("invalid_hash_1", "invalid_hash_2"));

        // Выполнение POST-запроса для создания заказа с неверными ингредиентами
        OrderApi.createOrder(newOrder, authToken)
                .assertThat()
                .statusCode(SC_INTERNAL_SERVER_ERROR);  // Ожидаемый статус код для неправильных данных
    }

    @Test
    @DisplayName("Создание заказа с ингредиентами")
    @Description("Создание заказа с ингредиентами")
    public void testCreateOrderWithIngredients() {
        Order newOrder = new Order();
        newOrder.setIngredients(List.of("61c0c5a71d1f82001bdaaa6d", "61c0c5a71d1f82001bdaaa72"));

        // Отправка POST-запроса для создания заказа
        OrderApi.createOrder(newOrder, authToken)
                .assertThat()
                .statusCode(SC_OK)  // Ожидаемый код ответа при успешном создании заказа
                .body("success", equalTo(true))  // Проверка, что заказ успешно создан
                .body("order.ingredients.size()", equalTo(2));  // Проверка, что количество ингредиентов в заказе равно
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Создание заказа без ингредиентов")
    public void testCreateOrderWithoutIngredients() {
        Order newOrder = new Order();
        newOrder.setIngredients(Collections.emptyList());

        // Отправка POST-запроса для создания заказа
        OrderApi.createOrder(newOrder, authToken)
                .assertThat()
                .statusCode(SC_BAD_REQUEST)  // Ожидаемый код ошибки при отсутствии ингредиентов
                .body("success", equalTo(false)) // Проверка, что заказ не был создан
                .body("message", equalTo("Ingredient ids must be provided")); // Проверка сообщения об ошибке
    }

    @After
    public void tearDown() throws Exception {
        // Очистка данных после теста (удаление пользователя, если был токен)
        if (authToken != null) {
            deleteDefinedUser(user);
        }
    }
}

