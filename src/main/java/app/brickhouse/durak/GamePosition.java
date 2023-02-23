package app.brickhouse.durak;

import java.util.ArrayList;
import java.util.List;

public class GamePosition extends BasicGame {

    private String player1;
    private String player2;

    private int nextMoveIndex;

    private String trumpSuit;

    public List<String> deck = new ArrayList<>();
    public List<String> out = new ArrayList<>();
    public List<String> hand1 = new ArrayList<>();
    public List<String> hand2 = new ArrayList<>();
    public List<String> field = new ArrayList<>();

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
}


