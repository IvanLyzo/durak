package app.brickhouse.durak.model;

public class Response {

    public static final String USER_RESPONSE = "USER_RESPONSE";
    public static final String GAME_POSITION_RESPONSE = "GAME_POSITION_RESPONSE";
    public static final String EVENTS_RESPONSE = "EVENTS_RESPONSE";

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
