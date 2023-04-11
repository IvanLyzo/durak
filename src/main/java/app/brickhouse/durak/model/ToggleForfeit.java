package app.brickhouse.durak.model;

public class ToggleForfeit extends Event {

    public ToggleForfeit() {
        setType(Event.TOGGLE_FORFEIT);
    }

    private String initiator;

    public String getInitiator() {
        return initiator;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }
}
