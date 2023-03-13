package app.brickhouse.durak.model;

import java.util.ArrayList;
import java.util.List;

public class Events extends Response {

    public Events() {
        setType(Response.EVENTS_RESPONSE);
    }

    public List<Event> events = new ArrayList<>();
    private int startIndex;

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }
}
