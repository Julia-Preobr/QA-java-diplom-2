package types;

public class User extends Login {
    private String name;

    // Конструктор
    public User(String email, String password, String name) {
        super(email, password);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " (email: " + getEmail() + ")";
    }
}
