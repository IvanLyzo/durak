package app.brickhouse.durak;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet("/api")
public class CheckersServlet extends HttpServlet {

    private static Random random = new Random();

    public static final String[] INITIAL_DECK = new String[] {
            "S6", "S7", "S8", "S9", "S0", "SJ", "SQ", "SK", "SA",
            "C6", "C7", "C8", "C9", "C0", "CJ", "CQ", "CK", "CA",
            "D6", "D7", "D8", "D9", "D0", "DJ", "DQ", "DK", "DA",
            "H6", "H7", "H8", "H9", "H0", "HJ", "HQ", "HK", "HA"
    };

    public static final char[] NOMINAL_ORDER = new char[] {
            '6', '7', '8', '9', '0', 'J', 'Q', 'K', 'A'
    };

    private final Model model = new Model();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String command = req.getParameter("command");
        String startingIndex = req.getParameter("startingIndex");
        String card = req.getParameter("card");
        String buttonName = req.getParameter("buttonName");

        Object response = null;
        switch(command) {
            case "enter":
                response = enter(req, resp);
                break;
            case "enterGame":
                response = enterGame(req);
                break;
            case "getGamePosition":
                response = getGamePosition(req);
                break;
            case "getGameHistory":
                response = getGameHistory(req, Integer.parseInt(startingIndex));
                break;
            case "initiateMove":
                response = initiateMove(req, card, Integer.parseInt(startingIndex));
                break;
            case "initiateButton":
                response = initiateButton(req, buttonName, Integer.parseInt(startingIndex));
                break;
            default:
                throw new RuntimeException("Unrecognized command: " + command);
        }

        if (response != null) {
            ObjectMapper mapper = new ObjectMapper();

            try {
                String json = mapper.writeValueAsString(response);
                ServletOutputStream out = resp.getOutputStream();
                out.println(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private User enter(HttpServletRequest req, HttpServletResponse resp) {
        String name = getNameFromCookie(req);

        if (name == null) {
            name = generateName(resp);
        }

        User user = new User();
        user.setName(name);

        return user;
    }

    private String getNameFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("name")) {
                    return(cookie.getValue());
                }
            }
        }

        return null;
    }

    private String generateName(HttpServletResponse resp) {
        String name = model.keywords[random.nextInt(model.keywords.length)] + model.nextNumber();

        Cookie cookie = new Cookie("name", name);
        resp.addCookie(cookie);

        return name;
    }



    private GamePosition enterGame(HttpServletRequest req) {
        System.out.println("Entering game");
        // get name of the user
        String name = getNameFromCookie(req);

        if (name == null) {
            // if it's null, cookie must have been cleared after entering
            throw new RuntimeException();
        }

        System.out.println("User: " + name);
        GamePosition response;

        if (model.freeGame == null) {
            model.freeGame = new Game();

            System.out.println("Created free game " + model.freeGame + " in the model " + model);
            initGame(model.freeGame.gamePosition);

            model.freeGame.gamePosition.setPlayer1(name);
            response = model.freeGame.gamePosition;
        } else {
            System.out.println("Game exists, joining");
            model.freeGame.gamePosition.setPlayer2(name);

            model.activeGames.add(model.freeGame);
            System.out.println("Cards in deck before copyPosition: " + model.freeGame.gamePosition.deck.size());
            response = copyPosition(model.freeGame.gamePosition);

            startGame(model.freeGame);

            System.out.println("Cards in deck after copyPosition: " + model.freeGame.gamePosition.deck.size());

            System.out.println("About to reset free game");
            model.freeGame = null;
        }

        System.out.println("About to return response: " + response);
        System.out.println("Cards in deck before return: " + response.deck.size());
        return response;
    }

    private GamePosition copyPosition(GamePosition position) {
        GamePosition positionCopy = new GamePosition();

        positionCopy.deck = new ArrayList<>(position.deck);
        positionCopy.out = new ArrayList<>(position.out);
        positionCopy.hand1 = new ArrayList<>(position.hand2);
        positionCopy.hand2 = new ArrayList<>(position.hand2);
        positionCopy.field = new ArrayList<>(position.field);

        positionCopy.setNextMoveIndex(position.getNextMoveIndex());
        positionCopy.setPlayer1(position.getPlayer1());
        positionCopy.setPlayer2(position.getPlayer2());
        positionCopy.setGameState(position.getGameState());
        positionCopy.setTrumpSuit(position.getTrumpSuit());

        return positionCopy;
    }

    private void initGame(GamePosition game) {
        Collections.addAll(game.deck, INITIAL_DECK);
        Collections.shuffle(game.deck);

        System.out.println("First card in deck: " + game.deck.get(0));

        String trumpSuit = String.valueOf(game.deck.get(0).charAt(0));
        game.setTrumpSuit(trumpSuit);

        System.out.println("Trump suit: " + game.getTrumpSuit());
    }

    private void startGame(Game game) {
        for (int i = 0; i < 6; i++) {
            moveCardFromDeck("hand1", game);
        }

        for (int i = 0; i < 6; i++) {
            moveCardFromDeck("hand2", game);
        }

        game.gamePosition.setGameState(random.nextBoolean() ? GamePosition.STATE_PLAYER1_ATTACK : GamePosition.STATE_PLAYER2_ATTACK);
        game.gamePosition.setNextMoveIndex(0);
    }

    private void moveCardFromDeck(String handName, Game game) {
        String card = game.gamePosition.deck.remove(game.gamePosition.deck.size() - 1);

        Move move = new Move();
        move.setCard(card);
        move.setFrom("deck");
        move.setTo(handName);

        game.moves.add(move);
        List<String> hand = handName.equals("hand1") ? game.gamePosition.hand1 : game.gamePosition.hand2;
        hand.add(card);

        System.out.println("Removing card " + card + " from deck and adding to " + hand);
    }

    private GamePosition getGamePosition(HttpServletRequest req) {
        String name = getNameFromCookie(req);
        return getGameWithName(name).gamePosition;
    }

    private GameHistory getGameHistory(HttpServletRequest req, int startingIndex) {
        String name = getNameFromCookie(req);
        Game game = getGameWithName(name);

        GameHistory gameHistory = new GameHistory();

        for (int i = startingIndex; i < game.moves.size(); i++) {
            gameHistory.moves.add(game.moves.get(i));
        }

        gameHistory.setGameState(game.gamePosition.getGameState());

        return gameHistory;
    }

    private Game getGameWithName(String name) {
        Game game = null;

        for (Game currentGame : model.activeGames) {
            if (currentGame.gamePosition.getPlayer1().equals(name) || currentGame.gamePosition.getPlayer2().equals(name)) {
                game = currentGame;
            }
        }

        if (game == null) {
            if (model.freeGame.gamePosition.getPlayer1().equals(name)) {
                game = model.freeGame;
            } else {
                throw new RuntimeException("Game not found for the player with the given name: " + name);
            }
        }

        return game;
    }

    private GameHistory initiateMove(HttpServletRequest req, String card, int startingIndex) {
        String name = getNameFromCookie(req);
        Game game = getGameWithName(name);

        // check game state
        if (game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_ATTACK) ||
                game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_ATTACK)) {

            // detect which user makes a move
            List<String> myHand;
            String myHandName;
            boolean playerOne = game.gamePosition.getPlayer1().equals(name);
            boolean attacking;
            if (playerOne) {
                myHand = game.gamePosition.hand1;
                myHandName = "hand1";
                attacking = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_ATTACK);
            } else {
                myHand = game.gamePosition.hand2;
                myHandName = "hand2";
                attacking = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_ATTACK);
            }

            // make a move
            if (myHand.contains(card)) {

                // check if this is my move
                if (attacking) {

                    if (game.gamePosition.field.size() % 2 == 0) {
                        if (game.gamePosition.field.size() == 0) {
                            makeMove(myHand, card, myHandName, game);
                        } else {
                            boolean found = false;
                            for (String fieldCard : game.gamePosition.field) {
                                // check if our card has the same nominal as fieldCard
                                if (fieldCard.charAt(1) == card.charAt(1)) {
                                    found = true;
                                }
                            }
                            if (found) {
                                makeMove(myHand, card, myHandName, game);
                            } else {
                                System.out.println("Can't make move with card " + card + ", there is no such nominal on the field");
                            }
                        }
                    } else {
                        System.out.println("Player " + name + " is attacking but there are odd number of cards on the field");
                    }

                } else {
                    // we are defending
                    if (game.gamePosition.field.size() % 2 == 1) {
                        String attackingCard = game.gamePosition.field.get(game.gamePosition.field.size() - 1);
                        char trumpSuit = game.gamePosition.getTrumpSuit().charAt(0);

                        if (card.charAt(0) == trumpSuit && attackingCard.charAt(0) != trumpSuit) {
                            makeMove(myHand, card, myHandName, game);
                        } else if (card.charAt(0) == attackingCard.charAt(0)) {
                            int attackIndex = 0;
                            int defendingIndex = 0;

                            for (int i = 0; i < NOMINAL_ORDER.length; i++) {
                                System.out.println("Entered the loop for the " + i + "");
                                if (NOMINAL_ORDER[i] == attackingCard.charAt(1)) {
                                    attackIndex = i;
                                    break;
                                }
                            }

                            for (int i = 0; i < NOMINAL_ORDER.length; i++) {
                                if (NOMINAL_ORDER[i] == card.charAt(1)) {
                                    defendingIndex = i;
                                    break;
                                }
                            }

                            System.out.println("Attack index " + attackIndex + ", defending index " + defendingIndex);

                            if (attackIndex < defendingIndex) {
                                makeMove(myHand, card, myHandName, game);
                            } else {
                                System.out.println("Nominal of defending card has to surpass that of attacking card " +
                                        "and be of suit as previous placed card");
                            }
                        } else {
                            System.out.println("Can't defend in this situation, must pick up");
                        }
                    } else {
                        System.out.println("Player " + name + " is defending but there is nothing to defend");
                    }
                }

            }
        }

        return getGameHistory(req, startingIndex);
    }

    private void makeMove(List<String> myHand, String card, String myHandName, Game game) {
        //TODO: REUSE NEW METHOD
        myHand.remove(card);

        Move move = new Move();
        move.setCard(card);
        move.setFrom(myHandName);
        move.setTo("field");

        game.moves.add(move);
        game.gamePosition.field.add(card);
    }

    private void makeMove2(String fromName, List<String> from, String toName, List<String> to, String card, Game game) {
        from.remove(card);
        to.add(card);

        Move move = new Move();
        move.setFrom(fromName);
        move.setTo(toName);
        move.setCard(card);

        game.moves.add(move);
    }

    private GameHistory initiateButton(HttpServletRequest req, String buttonName, int startingIndex) {
        String name = getNameFromCookie(req);
        Game game = getGameWithName(name);

        if (game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_ATTACK) ||
                game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_ATTACK)) {

            // detect which user makes a move
            // TODO: REMOVE DUPLICATE
            List<String> myHand;
            List<String> hisHand;
            String myHandName;
            String hisHandName;
            boolean playerOne = game.gamePosition.getPlayer1().equals(name);
            boolean attacking;
            if (playerOne) {
                myHand = game.gamePosition.hand1;
                hisHand = game.gamePosition.hand2;
                myHandName = "hand1";
                hisHandName = "hand2";
                attacking = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_ATTACK);
            } else {
                myHand = game.gamePosition.hand2;
                hisHand = game.gamePosition.hand1;
                myHandName = "hand2";
                hisHandName = "hand1";
                attacking = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_ATTACK);
            }

            if (buttonName.equals("pass")) {
                if (attacking) {
                    if (game.gamePosition.field.size() == 0) {
                        System.out.println("Can't pass with no cards put down");
                    } else if (game.gamePosition.field.size() % 2 == 1) {
                        System.out.println("Can't pass until opponent defends");
                    } else {
                        List<String> newCards = new ArrayList<>(game.gamePosition.field);
                        for (String card : newCards) {
                            makeMove2("field", game.gamePosition.field, "out", game.gamePosition.out, card, game);
                        }

                        while (myHand.size() < 6 && game.gamePosition.deck.size() > 0) {
                            makeMove2("deck", game.gamePosition.deck, myHandName, myHand,
                                    game.gamePosition.deck.get(game.gamePosition.deck.size() - 1), game);
                        }

                        while (hisHand.size() < 6 && game.gamePosition.deck.size() > 0) {
                            makeMove2("deck", game.gamePosition.deck, hisHandName, hisHand,
                                    game.gamePosition.deck.get(game.gamePosition.deck.size() - 1), game);
                        }

                        game.gamePosition.setGameState(game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_ATTACK) ?
                                GamePosition.STATE_PLAYER2_ATTACK : GamePosition.STATE_PLAYER1_ATTACK);
                    }
                } else {
                    System.out.println("Can't pass on opponent's turn");
                }
            } else if (buttonName.equals("pick")) {
                if (!attacking) {
                    if (game.gamePosition.field.size() == 0) {
                        System.out.println("Can't pick up with no cards on the field");
                    } else if (game.gamePosition.field.size() % 2 == 0) {
                        System.out.println("Can't pick up defended cards");
                    } else {
                        List<String> newCards = new ArrayList<>(game.gamePosition.field);
                        for (String card : newCards) {
                            makeMove2("field", game.gamePosition.field, myHandName, myHand, card, game);
                        }

                        while (hisHand.size() < 6 && game.gamePosition.deck.size() > 0) {
                            makeMove2("deck", game.gamePosition.deck, hisHandName, hisHand,
                                    game.gamePosition.deck.get(game.gamePosition.deck.size() - 1), game);
                        }
                    }
                } else {
                    System.out.println("Can't pick up on your attack turn");
                }
            }
        }



        return getGameHistory(req, startingIndex);
    }
}