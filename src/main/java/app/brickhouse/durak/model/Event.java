package app.brickhouse.durak.model;

public abstract class Event {

    public static final String MOVE = "MOVE";
    public static final String CHANGE_GAME_STATE = "CHANGE_GAME_STATE";
    public static final String GET_NAMES = "GET_NAMES";
    public static final String TOGGLE_DRAW = "TOGGLE_DRAW";
    public static final String TOGGLE_FORFEIT = "TOGGLE_FORFEIT";

    private String type;

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
