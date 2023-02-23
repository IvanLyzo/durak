package app.brickhouse.durak;

import java.util.ArrayList;
import java.util.List;

public class Model {
    public Game freeGame = null;

    public List<Game> activeGames = new ArrayList<>();


    public String[] keywords = new String[] {"Bulldog", "CrazyCat", "Captain"};
    private int number = 0;

    public int nextNumber() {
        number++;
        return number;
    }
}
