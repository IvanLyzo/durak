package app.brickhouse.durak.model;

public class Move extends Event {
    public static final String POSITION_HAND1 = "HAND1";
    public static final String POSITION_HAND2 = "HAND2";
    public static final String POSITION_FIELD = "FIELD";
    public static final String POSITION_DECK = "DECK";
    public static final String POSITION_OUT = "OUT";

    public Move() {
        setType(Event.MOVE);
    }

    private String card;
    private String from;
    private String to;

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
