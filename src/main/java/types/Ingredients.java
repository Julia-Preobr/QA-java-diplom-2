package types;

import java.util.List;

public class Ingredients {
    private List<Ingredient> data;
    private Boolean success;

    public Ingredients(List<Ingredient> data, Boolean success) {
        this.data = data;
        this.success = success;
    }

    public List<Ingredient> getData() {
        return data;
    }

    public void setData(List<Ingredient> data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
