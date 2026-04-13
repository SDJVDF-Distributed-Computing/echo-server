package smp.model;

import java.util.UUID;

public class User {
    private UUID id;
    private String username;
    private String password;

    public User(String username, String password) {
        setId(UUID.randomUUID());
        setUsername(username);
        setPassword(password);
    }

    public UUID getId() {
        return id;
    }

    private void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
