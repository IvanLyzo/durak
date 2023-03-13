package app.brickhouse.durak.model;

public class ChangeGameState extends Event {

    public ChangeGameState() {
        setType(Event.CHANGE_GAME_STATE);
    }

    private String gameState;

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }
}
