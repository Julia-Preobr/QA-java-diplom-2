package types;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User extends Login {
    @JsonProperty("name")
    private String name;

    public User(String email, String password, String name) {
        super(email, password);
        this.name = name;
    }

    public User(User from) {
        super(from.getEmail(), from.getPassword());
        this.name = from.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "{name: " + name + "; email: " + getEmail() + "; password: " +
                (getPassword() == null ? null : "***") + "}";
    }
}
