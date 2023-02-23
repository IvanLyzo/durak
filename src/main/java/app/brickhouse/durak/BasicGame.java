package app.brickhouse.durak;

public abstract class BasicGame {

    public static final String STATE_PLAYER1_ATTACK = "PLAYER1_ATTACK";
    public static final String STATE_PLAYER2_ATTACK = "PLAYER2_ATTACK";
    public static final String STATE_PLAYER1_WON = "PLAYER1_WON";
    public static final String STATE_PLAYER2_WON = "PLAYER2_WON";
    public static final String STATE_DRAW = "DRAW";
    public static final String STATE_WAITING = "WAITING";


    private String gameState;

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }


}
