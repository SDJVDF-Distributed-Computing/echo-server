package smp.store;

import smp.model.User;

import java.util.concurrent.ConcurrentHashMap;

public class UserStore {
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public UserStore() {
        addUser("alice", "pass123");
        addUser("bob",   "pass456");
        addUser("carol", "pass789");
    }

    public void addUser(String username, String password) {
        User user = new User(username, password);
        users.put(username, user);
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public boolean verify(String username, String password) {
        User user = users.get(username);
        if (user == null) return false;
        return user.getPassword().equals(password);
    }

    public User getUser(String username) {
        return users.get(username);
    }
}
