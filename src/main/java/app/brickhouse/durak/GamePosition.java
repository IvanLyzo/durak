package app.brickhouse.durak;

import app.brickhouse.durak.model.Response;

import java.util.ArrayList;
import java.util.List;

public class GamePosition extends Response {

    public static final String STATE_PLAYER1_ATTACK = "PLAYER1_ATTACK";
    public static final String STATE_PLAYER2_ATTACK = "PLAYER2_ATTACK";
    public static final String STATE_PLAYER1_PICKUP = "PLAYER1_PICKUP";
    public static final String STATE_PLAYER2_PICKUP = "PLAYER2_PICKUP";
    public static final String STATE_PLAYER1_WON = "PLAYER1_WON";
    public static final String STATE_PLAYER2_WON = "PLAYER2_WON";
    public static final String STATE_DRAW = "DRAW";
    public static final String STATE_WAITING = "WAITING";

    public GamePosition() {
        setType(Response.GAME_POSITION_RESPONSE);
    }

    private String gameState;

    private String player1;
    private String player2;

    private int nextMoveIndex;

    private String trumpSuit;

    private int pickUpLeft;

    public List<String> deck = new ArrayList<>();
    public List<String> out = new ArrayList<>();
    public List<String> hand1 = new ArrayList<>();
    public List<String> hand2 = new ArrayList<>();
    public List<String> pickUp = new ArrayList<>();
    public List<String> field = new ArrayList<>();

    public String getGameState() {
        return gameState;
    }

    public void setGameState(String gameState) {
        this.gameState = gameState;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public int getNextMoveIndex() {
        return nextMoveIndex;
    }

    public void setNextMoveIndex(int nextMoveIndex) {
        this.nextMoveIndex = nextMoveIndex;
    }

    public String getTrumpSuit() {
        return trumpSuit;
    }

    public void setTrumpSuit(String trumpSuit) {
        this.trumpSuit = trumpSuit;
    }

    public int getPickUpLeft() {
        return pickUpLeft;
    }

    public void setPickUpLeft(int pickUpLeft) {
        this.pickUpLeft = pickUpLeft;
    }
}


