import java.util.List;

public class ApiIngredients {
    private List<Ingredient> data;
    private String success;

    public ApiIngredients(List<Ingredient> data, String success) {
        this.data = data;
        this.success = success;
    }

    public List<Ingredient> getData() {
        return data;
    }

    public void setData(List<Ingredient> data) {
        this.data = data;
    }

    public String getSuccess() {
        return success;
    }

    public void setSuccess(String success) {
        this.success = success;
    }
}
