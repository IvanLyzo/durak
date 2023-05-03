package app.brickhouse.durak;

import app.brickhouse.durak.model.Response;

public class User extends Response {

    public User() {
        setType(Response.USER_RESPONSE);
    }

    private String name;

    private boolean newUser;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }
}