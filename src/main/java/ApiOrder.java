import java.util.List;

public class ApiOrder {

    private String orderId;
    private List<String> ingredients; // Список ингредиентов
    private int userId;

    // Конструктор
    public ApiOrder(String orderId, List<String> ingredients, int userId) {
        this.orderId = orderId;
        this.ingredients = ingredients;
        this.userId = userId;
    }

    // Геттеры и сеттеры

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
