package app.brickhouse.durak;

import app.brickhouse.durak.model.*;
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
public class DurakServlet extends HttpServlet {

    private static final Random random = new Random();

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

        // create command
        Command command = createCommand(req);

        // process command and get instance of Events
        Response response = processCommand(command);

        // send response back to client
        sendResponse(response, resp);

    }

    private Command createCommand(HttpServletRequest req) {
        Command command = new Command();
        command.setUsername(getNameFromCookie(req));

        command.setCommand(req.getParameter("command"));

        String startIndexStr = req.getParameter("startIndex");
        if (startIndexStr != null) {
            command.setStartIndex(Integer.parseInt(startIndexStr));
        }

        command.setCard(req.getParameter("card"));

        command.setButtonName(req.getParameter("buttonName"));

        return command;
    }


    private Response processCommand(Command command) {

        Response response;
        switch(command.getCommand()) {
            case "enter":
                response = enter(command);
                break;
            case "enterGame":
                response = enterGame(command);
                break;
            case "getEvents":
                response = getEvents(command);
                break;
            case "initiateMove":
                response = initiateMove(command);
                break;
            case "initiateButton":
                response = initiateButton(command);
                break;
            default:
                throw new RuntimeException("Unrecognized command: " + command.getCommand());
        }

        return response;
    }

    private User enter(Command command) {
        User user = new User();

        if (command.getUsername() == null) {
            user.setName(generateName());
            user.setNewUser(true);
        } else {
            user.setName(command.getUsername());
            user.setNewUser(false);
        }

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

    private String generateName() {
        return model.keywords[random.nextInt(model.keywords.length)] + model.nextNumber();
    }

    private GamePosition enterGame(Command command) {
        System.out.println("Entering game");
        // get name of the user
        String name = command.getUsername();

        if (name == null) {
            // if it's null, cookie must have been cleared after entering
            throw new RuntimeException();
        }

        System.out.println("User: " + name);
        GamePosition response;
        if (model.usersGame.get(name) == null ||
                (model.usersGame.get(name).gamePosition.getGameState().equals(GamePosition.STATE_DRAW)) ||
                (model.usersGame.get(name).gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_WON)) ||
                (model.usersGame.get(name).gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_WON))) {
            if (model.freeGame == null) {
                model.freeGame = new Game();

                System.out.println("Created free game " + model.freeGame + " in the model " + model);
                initGame(model.freeGame);

                model.freeGame.gamePosition.setPlayer1(name);
                response = model.freeGame.gamePosition;

                model.usersGame.put(name, model.freeGame);
            } else {
                System.out.println("Game exists, joining");
                model.freeGame.gamePosition.setPlayer2(name);

                model.activeGames.add(model.freeGame);
                System.out.println("Cards in deck before copyPosition: " + model.freeGame.gamePosition.deck.size());
                response = copyPosition(model.freeGame.gamePosition);

                startGame(model.freeGame);

                System.out.println("Cards in deck after copyPosition: " + model.freeGame.gamePosition.deck.size());

                model.usersGame.put(name, model.freeGame);

                System.out.println("About to reset free game");
                model.freeGame = null;
            }
        } else {
            response = model.usersGame.get(name).gamePosition;
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

    private void initGame(Game game) {
        Collections.addAll(game.gamePosition.deck, INITIAL_DECK);
        Collections.shuffle(game.gamePosition.deck);

        System.out.println("First card in deck: " + game.gamePosition.deck.get(0));

        String trumpSuit = String.valueOf(game.gamePosition.deck.get(0).charAt(0));
        game.gamePosition.setTrumpSuit(trumpSuit);

        changeGameState(game, GamePosition.STATE_WAITING);

        System.out.println("Trump suit: " + game.gamePosition.getTrumpSuit());
    }

    private void startGame(Game game) {
        GetNames getNames = new GetNames();
        getNames.setName1(game.gamePosition.getPlayer1());
        getNames.setName2(game.gamePosition.getPlayer2());
        game.moves.add(getNames);

        for (int i = 0; i < 6; i++) {
            moveCardFromDeck("hand1", game);
        }
        for (int i = 0; i < 6; i++) {
            moveCardFromDeck("hand2", game);
        }

        String firstAttacker = random.nextBoolean() ? GamePosition.STATE_PLAYER1_ATTACK : GamePosition.STATE_PLAYER2_ATTACK;
        changeGameState(game, firstAttacker);

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

    private Events getEvents(Command command) {
        String name = command.getUsername();
        Game game = getGameWithName(name);

        Events events = new Events();
        events.setStartIndex(command.getStartIndex());

        for (int i = command.getStartIndex(); i < game.moves.size(); i++) {
            log1("About to add new move " + game.moves.get(i) + " with start index " + i);
            events.events.add(game.moves.get(i));
        }

        return events;
    }

    private Game getGameWithName(String name) {
        return model.usersGame.get(name);
    }

    private Events initiateMove(Command command) {
        String name = command.getUsername();
        Game game = getGameWithName(name);

        // check game state
        if (game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_ATTACK) ||
                game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_ATTACK) ||
                game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_PICKUP) ||
                game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_PICKUP)) {

            // detect which user makes a move
            List<String> myHand;
            List<String> hisHand;

            String myHandName;
            String hisHandName;

            boolean playerOne = game.gamePosition.getPlayer1().equals(command.getUsername());
            boolean attacking;
            boolean enemyPickingUp;

            if (playerOne) {
                myHand = game.gamePosition.hand1;
                hisHand = game.gamePosition.hand2;
                myHandName = "hand1";
                hisHandName = "hand2";
                attacking = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_ATTACK);
                enemyPickingUp = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_PICKUP);
            } else {
                myHand = game.gamePosition.hand2;
                hisHand = game.gamePosition.hand1;
                myHandName = "hand2";
                hisHandName = "hand1";
                attacking = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_ATTACK);
                enemyPickingUp = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_PICKUP);
            }

            // make a move
            String card = command.getCard();
            if (myHand.contains(card)) {
                if (attacking) {
                    if (game.gamePosition.field.size() % 2 == 0) {
                        if (game.gamePosition.field.size() == 0) {
                            makeMove(myHandName, myHand, "field", game.gamePosition.field, card, game);
                        } else {
                            boolean found = false;
                            for (String fieldCard : game.gamePosition.field) {
                                // check if our card has the same nominal as fieldCard
                                if (fieldCard.charAt(1) == card.charAt(1)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                makeMove(myHandName, myHand, "field", game.gamePosition.field, card, game);
                            } else {
                                System.out.println("Can't make move with card " + card + ", there is no such nominal on the field");
                            }
                        }
                    } else {
                        System.out.println("Player " + name + " is attacking but there are odd number of cards on the field");
                    }
                } else if (enemyPickingUp) {
                    if (game.gamePosition.getPickUpLeft() > 0) {
                        boolean found = false;
                        for (String pickUpCard : game.gamePosition.pickUp) {
                            // check if our card has the same nominal as pickUpCard
                            if (pickUpCard.charAt(1) == card.charAt(1)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            makeMove(myHandName, myHand, hisHandName, hisHand, card, game);
                            game.gamePosition.setPickUpLeft(game.gamePosition.getPickUpLeft() - 1);
                        } else {
                            System.out.println("Can't make move with card " + card + ", there is no such nominal in the pickUp pile");
                        }
                    }
                } else {
                    // we are defending
                    if (game.gamePosition.field.size() % 2 == 1) {
                        String attackingCard = game.gamePosition.field.get(game.gamePosition.field.size() - 1);
                        char trumpSuit = game.gamePosition.getTrumpSuit().charAt(0);

                        if (card.charAt(0) == trumpSuit && attackingCard.charAt(0) != trumpSuit) {
                            makeMove(myHandName, myHand, "field", game.gamePosition.field, card, game);
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

                            if (attackIndex < defendingIndex) {
                                makeMove(myHandName, myHand, "field", game.gamePosition.field, card, game);
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

        return getEvents(command);
    }

    private void makeMove(String fromName, List<String> from, String toName, List<String> to, String card, Game game) {
        System.out.println("Moving " + card + " from " + fromName + " to " + toName);

        from.remove(card);
        to.add(card);

        Move move = new Move();
        move.setFrom(fromName);
        move.setTo(toName);
        move.setCard(card);

        game.moves.add(move);
    }

    private Events initiateButton(Command command) {
        System.out.println("Command " + command + " is executing");

        Game game = getGameWithName(command.getUsername());

        // detect which user makes a move
        List<String> myHand;
        List<String> hisHand;

        String myHandName;
        String hisHandName;

        List<String> pickUp = game.gamePosition.pickUp;
        String pickUpName = "pickUp";

        boolean playerOne = game.gamePosition.getPlayer1().equals(command.getUsername());
        boolean attacking;
        boolean enemyPickingUp;

        if (playerOne) {
            myHand = game.gamePosition.hand1;
            hisHand = game.gamePosition.hand2;
            myHandName = "hand1";
            hisHandName = "hand2";
            attacking = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_ATTACK);
            enemyPickingUp = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_PICKUP);
        } else {
            myHand = game.gamePosition.hand2;
            hisHand = game.gamePosition.hand1;
            myHandName = "hand2";
            hisHandName = "hand1";
            attacking = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_ATTACK);
            enemyPickingUp = game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_PICKUP);
        }

        if (command.getButtonName().equals("toggleDraw")) {
            toggleDraw(game, playerOne);
        } else if (command.getButtonName().equals("toggleForfeit")) {
            toggleForfeit(game, playerOne);
        } else if (game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_ATTACK) ||
                game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_ATTACK) ||
                game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_PICKUP) ||
                game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER2_PICKUP)) {

            System.out.println("The enemy is picking up: " + enemyPickingUp);

            if (command.getButtonName().equals("endTurn")) {
                if (game.gamePosition.field.size() % 2 == 1) {
                    System.out.println("Can't end turn until opponent defends");
                } else {
                    if (attacking) {
                        if (game.gamePosition.field.size() == 0) {
                            System.out.println("Can't end turn with no cards put down");
                        } else {
                            List<String> fieldCopy = new ArrayList<>(game.gamePosition.field);

                            for (String card : fieldCopy) {
                                makeMove("field", game.gamePosition.field, "out", game.gamePosition.out, card, game);
                            }

                            handoutCards(game, myHand, myHandName, hisHand, hisHandName);

                            if (myHand.size() == 0 && hisHand.size() == 0) {
                                changeGameState(game, GamePosition.STATE_DRAW);
                            } else if (myHand.size() == 0) {
                                changeGameState(game, playerOne ? GamePosition.STATE_PLAYER1_WON : GamePosition.STATE_PLAYER2_WON);
                            } else if (hisHand.size() == 0) {
                                changeGameState(game, playerOne ? GamePosition.STATE_PLAYER2_WON : GamePosition.STATE_PLAYER1_WON);
                            } else {
                                changeGameState(game, game.gamePosition.getGameState().equals(GamePosition.STATE_PLAYER1_ATTACK) ?
                                        GamePosition.STATE_PLAYER2_ATTACK : GamePosition.STATE_PLAYER1_ATTACK);
                            }
                        }
                    } else if (enemyPickingUp) {
                        List<String> pickUpCopy = new ArrayList<>(game.gamePosition.pickUp);

                        for (String card : pickUpCopy) {
                            makeMove("pickUp", game.gamePosition.pickUp, hisHandName, hisHand, card, game);
                        }

                        handoutCards(game, myHand, myHandName, hisHand, hisHandName);

                        if (myHand.size() == 0 && hisHand.size() == 0) {
                            changeGameState(game, GamePosition.STATE_DRAW);
                        } else if (myHand.size() == 0) {
                            changeGameState(game, playerOne ? GamePosition.STATE_PLAYER1_WON : GamePosition.STATE_PLAYER2_WON);
                        } else if (hisHand.size() == 0) {
                            changeGameState(game, playerOne ? GamePosition.STATE_PLAYER2_WON : GamePosition.STATE_PLAYER1_WON);
                        } else {
                            changeGameState(game, playerOne ? GamePosition.STATE_PLAYER1_ATTACK : GamePosition.STATE_PLAYER2_ATTACK);
                        }
                    }
                }
            } else if (command.getButtonName().equals("pickUp")) {
                if (!attacking) {
                    if (game.gamePosition.field.size() == 0) {
                        System.out.println("Can't pick up with no cards on the field");
                    } else if (game.gamePosition.field.size() % 2 == 0) {
                        System.out.println("Can't pick up defended cards");
                    } else {
                        System.out.println("Executing pickup");
                        List<String> cardsCopy = new ArrayList<>(game.gamePosition.field);

                        changeGameState(game, playerOne ? GamePosition.STATE_PLAYER1_PICKUP : GamePosition.STATE_PLAYER2_PICKUP);

                        for (String card : cardsCopy) {
                            makeMove("field", game.gamePosition.field, pickUpName, pickUp, card, game);
                        }

                        game.gamePosition.setPickUpLeft(hisHand.size() + pickUp.size() / 2 - (pickUp.size() + 1) / 2);
                    }
                } else {
                    System.out.println("Can't pick up on your attack turn");
                }
            }
        }

        return getEvents(command);
    }

    private void handoutCards(Game game, List<String> myHand, String myHandName, List<String> hisHand, String hisHandName) {
        while (myHand.size() < 6 && game.gamePosition.deck.size() > 0) {
            makeMove("deck", game.gamePosition.deck, myHandName, myHand,
                    game.gamePosition.deck.get(game.gamePosition.deck.size() - 1), game);
        }

        while (hisHand.size() < 6 && game.gamePosition.deck.size() > 0) {
            makeMove("deck", game.gamePosition.deck, hisHandName, hisHand,
                    game.gamePosition.deck.get(game.gamePosition.deck.size() - 1), game);
        }
    }

    private void changeGameState(Game game, String gameState) {
        System.out.println("Changing game state to " + gameState);

        game.gamePosition.setGameState(gameState);

        ChangeGameState changeState = new ChangeGameState();
        changeState.setGameState(gameState);
        game.moves.add(changeState);
    }

    private void toggleDraw(Game game, boolean playerOne) {
        log1("Entered the toggleDraw, we are playerOne: " + playerOne);

        if (playerOne) {
            game.gamePosition.setPlayer1Draw(!game.gamePosition.isPlayer1Draw());
            log1("Changed player1Draw to " + game.gamePosition.isPlayer1Draw());
        } else {
            game.gamePosition.setPlayer2Draw(!game.gamePosition.isPlayer2Draw());
            log1("Changed player2Draw to " + game.gamePosition.isPlayer2Draw());
        }

        ToggleDraw toggleDraw = new ToggleDraw();

        toggleDraw.setPlayer1Draw(game.gamePosition.isPlayer1Draw());
        toggleDraw.setPlayer2Draw(game.gamePosition.isPlayer2Draw());

        game.moves.add(toggleDraw);
        log1("Changed toggleDraw of player1 to " + toggleDraw.isPlayer1Draw() + ", and toggleDraw of player2 to " + toggleDraw.isPlayer2Draw());

        if (game.gamePosition.isPlayer1Draw() && game.gamePosition.isPlayer2Draw()) {
            changeGameState(game, GamePosition.STATE_DRAW);
        }
    }

    private void toggleForfeit(Game game, boolean playerOne) {
        ToggleForfeit toggleForfeit = new ToggleForfeit();

        if (playerOne) {
            changeGameState(game, GamePosition.STATE_PLAYER2_WON);
            toggleForfeit.setInitiator("player1");

            log1("Game ended, player 1 forfeited");
        } else {
            changeGameState(game, GamePosition.STATE_PLAYER1_WON);
            toggleForfeit.setInitiator("player2");

            log1("Game ended, player 2 forfeited");
        }
    }

    private void sendResponse(Response response, HttpServletResponse resp) {
        if (response != null) {
            // send response
            ObjectMapper mapper = new ObjectMapper();

            try {
                String json = mapper.writeValueAsString(response);
                ServletOutputStream out = resp.getOutputStream();
                out.println(json);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // set cookie
            if (response instanceof User) {
                Cookie cookie = new Cookie("name", ((User) response).getName());
                resp.addCookie(cookie);
            }
        }
    }

    private void log1(String message) {

        System.out.println(new Date() + ": " + message);
    }
}