package app.brickhouse.durak.model;

public class ToggleDraw extends Event {
    public ToggleDraw() {
        setType(Event.TOGGLE_DRAW);
    }

    private boolean player1Draw;
    private boolean player2Draw;

    public boolean isPlayer1Draw() {
        return player1Draw;
    }

    public void setPlayer1Draw(boolean player1Draw) {
        this.player1Draw = player1Draw;
    }

    public boolean isPlayer2Draw() {
        return player2Draw;
    }

    public void setPlayer2Draw(boolean player2Draw) {
        this.player2Draw = player2Draw;
    }
}
