let myUsername;
let hisUsername;

let gameActive;

let gamePosition = null;

window.addEventListener("load", getName);

let model = null;

const cWidth = 100;
const cHeight = 150;

const CARDS = ["S6", "S7", "S8", "S9", "S0", "SJ", "SQ", "SK", "SA",
    "C6", "C7", "C8", "C9", "C0", "CJ", "CQ", "CK", "CA",
    "D6", "D7", "D8", "D9", "D0", "DJ", "DQ", "DK", "DA",
    "H6", "H7", "H8", "H9", "H0", "HJ", "HQ", "HK", "HA"];

function recalculateCardPositions()
{
    if (model == null || model.deskElement == null)
    {
        return;
    }

    let width = model.deskElement.clientWidth;
    let height = model.deskElement.clientHeight;

    let hand2 = model.collections.find(c => c.name == "hand2").cards;
    let hand1 = model.collections.find(c => c.name == "hand1").cards;
    let deck = model.collections.find(c => c.name == "deck").cards;
    let out = model.collections.find(c => c.name == "out").cards;
    let field = model.collections.find(c => c.name == "field").cards;
    let pickUp = model.collections.find(c => c.name == "pickUp").cards;

    let nameHeight = height / 8;
    let handHeight = height / 4;
    let fieldHeight = height / 4;

    layoutDeck(0, nameHeight + handHeight, width / 4, fieldHeight, deck);
    layoutOut(width / 4 * 3, nameHeight + handHeight, width / 4, fieldHeight, out, true);
    layoutField(width / 4, nameHeight + handHeight, width / 2, fieldHeight, field, true);

    layoutPlayerUI(0, 0, width, nameHeight, hisUsername, "hisName");
    layoutPlayerUI(0, nameHeight + handHeight * 2 + fieldHeight, width, nameHeight, myUsername, "myName");

    // if game state is pickUp, draw pickUp pile
    if (model.gameState == "PLAYER1_PICKUP")
    {
        if (myUsername == model.player1)
        {
            layoutHand(0, nameHeight, width, handHeight, hand2, false);

            layoutHand(0, nameHeight + handHeight + fieldHeight, width / 2, handHeight, hand1, true);
            layoutHand(width / 2, nameHeight + handHeight + fieldHeight, width / 2, handHeight, pickUp, true);
        } else
        {
            layoutHand(0, nameHeight, width / 2, handHeight, hand1, false);
            layoutHand(width / 2, nameHeight, width / 2, handHeight, pickUp, true);

            layoutHand(0, nameHeight + handHeight + fieldHeight, width, handHeight, hand2, true);
        }
    } else if (model.gameState == "PLAYER2_PICKUP")
    {
        if (myUsername == model.player1)
        {
            layoutHand(0, nameHeight, width / 2, handHeight, hand2, false);
            layoutHand(width / 2, nameHeight, width / 2, handHeight, pickUp, true);

            layoutHand(0, nameHeight + handHeight + fieldHeight, width, handHeight, hand1, true);
        } else
        {
            layoutHand(0, nameHeight, width, handHeight, hand1, false);

            layoutHand(0, nameHeight + handHeight + fieldHeight, width / 2, handHeight, hand2, true);
            layoutHand(width / 2, nameHeight + handHeight + fieldHeight, width / 2, handHeight, pickUp, true);
        }
    } else
    {
        if (myUsername == model.player1)
        {
            layoutHand(0, nameHeight, width, handHeight, hand2, false);
            layoutHand(0, nameHeight + handHeight + fieldHeight, width, handHeight, hand1, true);
        } else
        {
            layoutHand(0, nameHeight, width, handHeight, hand1, false);
            layoutHand(0, nameHeight + handHeight + fieldHeight, width, handHeight, hand2, true);
        }
    }

    layoutEndgamePanel();
}

function layoutHand(wx, wy, ww, wh, cards, drawFront)
{
    const mGap = cWidth / 2;
    const mOffset = cWidth / 2;

    if (cards.length == 0)
    {
        return;
    }
    else if (cards.length == 1)
    {
        let cardX = wx + ww / 2 - cWidth / 2;
        let cardY = 20 + wy;
        cards[0].element.style.transform = "translate(" + cardX + "px," + cardY + "px)";
        cards[0].element.style.zIndex = cards[0].order;
    }
    else if (cards.length > 1)
    {
        let gap = (ww - 2 * mOffset - cards.length * cWidth) / (cards.length - 1);
        let offset;

        if (gap <= mGap)
        {
            offset = mOffset;
        }
        else
        {
            gap = mGap;

            let totalLength = cards.length * cWidth + (cards.length - 1) * gap;
            offset = (ww - totalLength) / 2;
        }

        for (let i = 0; i < cards.length; i++)
        {
            let cardX = wx + (offset + (cWidth + gap) * i);
            let cardY = 20 + wy;

            cards[i].element.style.transform = "translate(" + cardX + "px," + cardY + "px)";
            cards[i].element.style.zIndex = cards[i].order;
        }
    }

    for (let i = 0; i < cards.length; i++)
    {
        let imgElement = cards[i].element.children[0];
        drawCard(drawFront, imgElement, cards[i].name);
    }
}

function layoutPlayerUI(wx, wy, ww, wh, text, id)
{
    // position name
    const element = document.getElementById(id);

    if (model.gameState == "WAITING" && id == "hisName")
    {
        element.innerHTML = "...";
    }
    else
    {
        element.innerHTML = text;
    }

    element.style.color = "rgb(0, 0, 0)";
    if ((myUsername == model.player1 && model.gameState == "PLAYER1_ATTACK") || (myUsername == model.player2 && model.gameState == "PLAYER2_ATTACK")
        && id == "myName")
    {
        element.style.color = "rgb(255, 0, 0)";
    }
    else if ((hisUsername == model.player1 && model.gameState == "PLAYER1_ATTACK") || (hisUsername == model.player2 && model.gameState == "PLAYER2_ATTACK")
        && id == "hisName")
    {
        element.style.color = "rgb(255, 0, 0)";
    }

    let textX = wx + (ww - element.clientWidth) / 2;
    let textY = wy;
    element.style.transform = "translate(" + textX + "px, " + textY + "px)";

    // position buttons
    if (id == "myName")
    {
        const pickUpButton = document.getElementById("pickUp");
        const endTurnButton = document.getElementById("endTurn");

        const buttonX = wx + ww / 2;
        const buttonY = textY + pickUpButton.clientHeight * 2;
        const buttonOffset = element.clientWidth * 1.5;

        log("ButtonX: " + buttonX + ", buttonY: " + buttonY);

        pickUpButton.style.transform = "translate(" + (buttonX - buttonOffset - pickUpButton.clientWidth / 2) + "px, " + buttonY + "px)";
        endTurnButton.style.transform = "translate(" + (buttonX + buttonOffset - endTurnButton.clientWidth / 2) + "px, " + buttonY + "px)";
    }
}


function layoutDeck(wx, wy, ww, wh, cards)
{
    const offsetY = 2;

    if (cards.length == 0)
    {
        return;
    }
    else if (cards.length == 1)
    {
        let cardX = wx + ww / 2 - cWidth / 2;
        let cardY = wy;
        cards[0].element.style.transform = "translate(" + cardX + "px," + cardY + "px) rotate(90deg)";
        cards[0].element.style.zIndex = cards[0].order;
    }
    else if (cards.length > 1)
    {
        for (let i = 1; i < cards.length; i++)
        {
            let cardX = wx + ww / 2 - cWidth / 2;
            let cardY = 20 + wy - offsetY * i;

            cards[i].element.style.transform = "translate(" + cardX + "px," + cardY + "px)";
            cards[i].element.style.zIndex = cards[i].order;
        }

        let cardX = wx + ww / 2 - cWidth / 2 + cHeight / 2;
        let cardY = wy;

        cards[0].element.style.transform = "translate(" + cardX + "px," + cardY + "px) rotate(90deg)";
        cards[0].element.style.zIndex = cards[0].order;
    }

    for (let i = 1; i < cards.length; i++)
    {
        let imgElement = cards[i].element.children[0];
        drawCard(false, imgElement, cards[i].name);
    }

    let imgElement = cards[0].element.children[0];
    drawCard(true, imgElement, cards[0].name);
}

function layoutOut(wx, wy, ww, wh, cards)
{
    const offsetY = 2;

    if (cards.length == 0)
    {
        return;
    }

    for (let i = 0; i < cards.length; i++)
    {
        let cardX = wx + ww / 2 - cWidth / 2;
        let cardY = 20 + wy - offsetY * i;

        cards[i].element.style.transform = "translate(" + cardX + "px," + cardY + "px)";
        cards[i].element.style.zIndex = cards[i].order;
    }

    for (let i = 0; i < cards.length; i++)
    {
        let imgElement = cards[i].element.children[0];
        drawCard(false, imgElement, cards[i].name);
    }
}

function layoutField(wx, wy, ww, wh, cards)
{
    const mGap = cWidth / 2;
    const mOffset = cWidth / 2;

    const cardOffsetX = cWidth / 2;

    if (cards.length == 0)
    {
        return;
    }

    let gap = (ww - 2 * mOffset - cards.length * cWidth) / (cards.length - 1);
    let offset;

    if (gap <= mGap)
    {
        offset = mOffset;
    }
    else
    {
        gap = mGap;

        let totalLength = cards.length * cWidth + (cards.length - 1) * gap;
        offset = (ww - totalLength) / 2;
    }

    for (let i = 0; i < cards.length; i++)
    {
        let cardX;
        let cardY;

        if (i % 2 == 0)
        {
            cardX = wx + (offset + (cWidth + gap) * i);
            cardY = 20 + wy - cHeight / 4;
        }
        else
        {
            cardX = wx + (offset + (cWidth + gap) * (i - 1)) + cardOffsetX;
            cardY = 20 + wy + cHeight / 4;
        }

        cards[i].element.style.transform = "translate(" + cardX + "px," + cardY + "px)";
        cards[i].element.style.zIndex = cards[i].order;
    }

    for (let i = 0; i < cards.length; i++)
    {
        let imgElement = cards[i].element.children[0];
        drawCard(true, imgElement, cards[i].name);
    }
}

function layoutEndgamePanel()
{
    let panel = document.getElementById("endgamePanel");

    let panelW = model.deskElement.clientWidth / 4;
    let panelH = model.deskElement.clientHeight / 2;
    panel.style.width = panelW + "px";
    panel.style.height = panelH + "px";

    let panelX = model.deskElement.clientWidth / 2 - panelW / 2;
    let panelY = model.deskElement.clientHeight / 2 - panelH / 2;

    log("PanelX " + panelX + ", panelY " + panelY);

    panel.style.transform = "translate(" + panelX + "px, " + panelY + "px)";
}

function addCard(collectionName, cardName)
{
    let card = {
        name: cardName,
        order: 0
    };

    model.collections.find(c => c.name == collectionName).cards.push(card);

    let cardElement = document.createElement("div");
    card.element = cardElement;
    cardElement.classList.add("card");

    cardElement.id = cardName;
    cardElement.addEventListener('click', () =>
    {
        initiateMove(cardName);
    });

    let imgElement = document.createElement("img");
    cardElement.appendChild(imgElement);

    model.deskElement.appendChild(cardElement);

    recalculateCardPositions();
}

function drawCard(drawFront, imgElement, cardName)
{
    let scaleX;
    let scaleY;

    let translateX;
    let translateY;

    let imgSrc;

    // figure out info
    if (drawFront == true)
    {
        imgSrc = "cards.png";

        scaleX = cWidth / 410;
        scaleY = cHeight / 623;

        let suit = cardName[0];
        let nominal = cardName[1];

        switch (suit)
        {
            case 'C':
                translateY = 0;
                break;
            case 'H':
                translateY = -623;
                break;
            case 'S':
                translateY = -623 * 2;
                break;
            case 'D':
                translateY = -623 * 3;
                break;
        }

        switch (nominal)
        {
            case 'A':
                translateX = 0;
                break;
            case '6':
                translateX = -410 * 5;
                break;
            case '7':
                translateX = -410 * 6;
                break;
            case '8':
                translateX = -410 * 7;
                break;
            case '9':
                translateX = -410 * 8;
                break;
            case '0':
                translateX = -410 * 9;
                break;
            case 'J':
                translateX = -410 * 10;
                break;
            case 'Q':
                translateX = -410 * 11;
                break;
            case 'K':
                translateX = -410 * 12;
                break;
        }
    } else
    {
        imgSrc = "back.png";

        scaleX = cWidth / 400;
        scaleY = cHeight / 600;

        translateX = 0;
        translateY = 0;
    }

    // apply transformations if needed
    if (imgElement.src != imgSrc)
    {
        imgElement.style.transform = "scaleX(0, 0)";
        imgElement.src = imgSrc;
        imgElement.style.transform = "scale(" + scaleX + "," + scaleY + ") translate(" + translateX + "px, " + translateY + "px)";
    }
}

function move(cardName, fromCollectionName, toCollectionName)
{
    log("Entered the move function with cardName: " + cardName + ", fromCollectionName: " + fromCollectionName + ", toCollectionName:" + toCollectionName);

    let fromCollection = model.collections.find(c => c.name == fromCollectionName);
    log("Found fromCollection " + fromCollectionName + " with name " + fromCollection.name + " using the name " + fromCollectionName);
    let toCollection = model.collections.find(c => c.name == toCollectionName);
    log("Found toCollection " + toCollectionName + " with name " + toCollection.name + " using the name " + toCollectionName);

    let card = fromCollection.cards.find(c => c.name == cardName);

    if (card == null)
    {
        log("card with card name " + cardName + " can't be found in collection " + toCollectionName);
        return;
    }

    if (toCollection.cards.length == 0)
    {
        card.order = 1;
    }
    else
    {
        card.order = toCollection.cards[toCollection.cards.length - 1].order + 1;
    }

    fromCollection.cards = fromCollection.cards.filter(c => c.name != cardName);
    log("After removing card from " + fromCollectionName + ", the collection now includes " + fromCollection.cards);
    toCollection.cards.push(card);
    log("Added card to " + toCollectionName + ", which now includes " + toCollection.cards);

    recalculateCardPositions();
}

function finishGame(gameState)
{
    let panelMessage = document.getElementById("message");

    if (gameState == "DRAW")
    {
        panelMessage.innerHTML = "It was a draw";
    }
    else if (gameState == "PLAYER1_WON")
    {
        panelMessage.innerHTML = "Player 1 won the game";
    }
    else if (gameState == "PLAYER2_WON")
    {
        panelMessage.innerHTML = "Player 2 won the game";
    }

    document.getElementById("endgamePanel").style.display = "block";
}

function initDesk()
{
    const loginDiv = document.getElementById("login");
    loginDiv.style.display = "none";

    const gameDiv = document.getElementById("game");
    gameDiv.style.display = "block";

    const drawButton = document.getElementById("toggleDraw");
    drawButton.innerHTML = "Toggle Draw";

    document.getElementById("endgamePanel").style.display = "none";

    gameActive = true;
}

function returnToLobby()
{
    document.getElementById("login").style.display = "block";
    document.getElementById("game").style.display = "none";
    document.getElementById("endgamePanel").style.display = "none";

    document.getElementById("toggleDraw").innerHTML = "Toggle Draw";

    gameActive = false;
}


//////


function getName()
{
    fetch("api?command=enter")
        .then((response) => response.json())
        .then((data) =>
        {
            const title = document.getElementById("title");

            title.innerHTML = data.name;

            myUsername = data.name;
        });
}

function enterGame()
{
    fetch("api?command=enterGame")
        .then((response) => response.json())
        .then((data) =>
        {
            initDesk();
            createClientModel(data);
            getEvents();
        });
}

function createClientModel(serverModel)
{
    let cards = document.getElementsByClassName("card");
    let cardsLength = cards.length;

    for (let i = 0; i < cardsLength; i++)
    {
        document.getElementById("desk").removeChild(cards[0]);
    }

    model = {
        collections: [{
            name: "hand1",
            layout: "hand",
            cards: []
        },
        {
            name: "hand2",
            layout: "hand",
            cards: []
        },
        {
            name: "pickUp",
            layout: "hand",
            cards: []
        },
        {
            name: "deck",
            layout: "hand",
            cards: []
        },
        {
            name: "out",
            layout: "hand",
            cards: []
        },
        {
            name: "field",
            layout: "hand",
            cards: []
        }],
        deskElement: document.getElementById("desk"),
        nextMoveIndex: serverModel.nextMoveIndex,
        player1: serverModel.player1,
        player2: serverModel.player2,
        gameState: serverModel.gameState
    };

    for (let i in serverModel.deck)
    {
        addCard("deck", serverModel.deck[i]);
    }

    for (let i in serverModel.out)
    {
        addCard("out", serverModel.out[i]);
    }

    for (let i in serverModel.hand1)
    {
        addCard("hand1", serverModel.hand1[i]);
    }

    for (let i in serverModel.hand2)
    {
        addCard("hand2", serverModel.hand2[i]);
    }

    for (let i in serverModel.pickUp)
    {
        addCard("pickUp1", serverModel.pickUp[i]);
    }

    for (let i in serverModel.field)
    {
        addCard("field", serverModel.field[i]);
    }
}

function getEvents()
{
    if (gameActive == false)
    {
        return;
    }

    fetch("api?command=getEvents&startIndex=" + model.nextMoveIndex)
        .then((response) => response.json())
        .then((data) =>
        {
            updateGame(data);
        });

    setTimeout(getEvents, 1000);
}

function updateGame(response)
{
    if (response.type == "EVENTS_RESPONSE")
    {
        for (let i = 0; i < response.events.length; i++)
        {
            let current = response.events[i];
            log("Current event is " + current.type + " with move index of " + model.nextMoveIndex);

            if (response.startIndex + i < model.nextMoveIndex)
            {
                continue;
            }
            else if (response.startIndex + i > model.nextMoveIndex)
            {
                throw new Error("RECIEVED INDEX WITH VALUE " + (response.startIndex + 1) + ", EXPECTED INDEX WITH VALUE " + model.nextMoveIndex);
            }

            if (current.type == "MOVE")
            {
                setTimeout(() => move(current.card, current.from, current.to), 100 * i);
            }
            else if (current.type == "CHANGE_GAME_STATE")
            {
                if (current.gameState == "DRAW")
                {
                    gameActive = false;
                    finishGame("DRAW");
                }
                else if (current.gameState == "PLAYER1_WON")
                {
                    gameActive = false;
                    finishGame("PLAYER1_WON");
                }
                else if (current.gameState == "PLAYER2_WON")
                {
                    gameActive = false;
                    finishGame("PLAYER2_WON");
                }

                model.gameState = current.gameState;
            }
            else if (current.type == "GET_NAMES")
            {
                if (current.name1 == myUsername)
                {
                    hisUsername = current.name2;
                    model.player2 = hisUsername;
                }
                else
                {
                    hisUsername = current.name1;
                    model.player1 = hisUsername;
                }
            }
            else if (current.type = "TOGGLE_DRAW")
            {
                let drawButton = document.getElementById("toggleDraw");

                if (current.player1Draw == true || current.player2Draw == true)
                {
                    drawButton.innerHTML = "Toggle Draw (1/2)";
                    log("Requested Draw");
                } else if (current.player1Draw == false && current.player2Draw == false)
                {
                    drawButton.innerHTML = "Toggle Draw";
                } else
                {
                    drawButton.innerHTML = "Toggle Draw (2/2)";
                    log("Both players agreed to draw");
                }
            }

            model.nextMoveIndex++;
        }
    }
}

function initiateMove(cardName)
{
    if (gameActive == false)
    {
        return;
    }

    fetch("api?command=initiateMove&startIndex=" + model.nextMoveIndex + "&card=" + cardName)
        .then((response) => response.json())
        .then((data) =>
        {
            updateGame(data);
        });
}

function initiateButton(buttonName)
{
    if (gameActive == false)
    {
        return;
    }

    fetch("api?command=initiateButton&startIndex=" + model.nextMoveIndex + "&buttonName=" + buttonName)
        .then((response) => response.json())
        .then((data) =>
        {
            updateGame(data);
        });
}


function log(message)
{
    console.log(new Date() + ": " + message);
}