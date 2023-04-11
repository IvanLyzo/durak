package app.brickhouse.durak.model;

import app.brickhouse.durak.Game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {
    public Game freeGame = null;

    public List<Game> activeGames = new ArrayList<>();

    public Map<String, Game> usersGame = new HashMap<>();

    public String[] keywords = new String[] {"Bulldog", "CrazyCat", "Captain"};
    private int number = 0;

    public int nextNumber() {
        number++;
        return number;
    }
}
