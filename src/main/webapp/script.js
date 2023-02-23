let username;
let gamePosition = null;

window.addEventListener("load", getName);


let model = null;

const cWidth = 100;
const cHeight = 150;
const mGap = cWidth / 2;
const mOffset = cWidth / 2;

const CARDS = ["S6", "S7", "S8", "S9", "S0", "SJ", "SQ", "SK", "SA",
"C6", "C7", "C8", "C9", "C0", "CJ", "CQ", "CK", "CA",
"D6", "D7", "D8", "D9", "D0", "DJ", "DQ", "DK", "DA",
"H6", "H7", "H8", "H9", "H0", "HJ", "HQ", "HK", "HA"];


function initEngine(deskElement) {
    model = {
        collections: [{
            name: "hand2",
            layout: "hand",
            cards: []
        }, 
        {
            name: "hand1",
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
        deskElement: deskElement
    };

    for (let i = 0; i < CARDS.length; i++) {
        addCard("deck", CARDS[i]);
    }   
}

function recalculateCardPositions() {
    if (model == null || model.deskElement == null) {
        return;
    }

    let width = model.deskElement.clientWidth;
    let height = model.deskElement.clientHeight;

    let hand2 = model.collections.find(c => c.name == "hand2").cards;
    let hand1 = model.collections.find(c => c.name == "hand1").cards;

    let deck = model.collections.find(c => c.name == "deck").cards;
    let out = model.collections.find(c => c.name == "out").cards;

    let field = model.collections.find(c => c.name == "field").cards;

    layoutHand(0, 0, width, height / 4, username == model.player1 ? hand2 : hand1);
    layoutHand(0, height / 4 * 3, width, height / 4, username == model.player1 ? hand1 : hand2);

    layoutHand(0, height / 4, width / 4, height / 2, deck);
    layoutHand(width / 4 * 3, height / 4, width / 4, height / 2, out);
    
    layoutHand(width / 4, height / 4, width / 2, height / 2, field);
}

function layoutHand(wx, wy, ww, wh, cards) {
    if (cards.length == 0) {
        return;
    }
    else if (cards.length == 1) {
        let cardX = wx + ww / 2 - cWidth / 2;
        let cardY = 20 + wy;
        cards[0].element.style.transform = "translate(" + cardX + "px," + cardY + "px)";
        cards[0].element.style.zIndex = cards[0].order;
    }
    else if (cards.length > 1) {
        let gap = (ww - 2 * mOffset - cards.length * cWidth) / (cards.length - 1);
        let offset;

        if (gap <= mGap) {
            offset = mOffset;
        }
        else {
            gap = mGap;

            let totalLength = cards.length * cWidth + (cards.length - 1) * gap;
            offset = (ww - totalLength) / 2; 
        }

        for (let i = 0; i < cards.length; i++) {
            let cardX = wx + (offset + (cWidth + gap) * i);
            let cardY = (20 + wy);

            cards[i].element.style.transform = "translate(" + cardX + "px," + cardY + "px)";
            cards[i].element.style.zIndex = cards[i].order;
        }
    }
}

function addCard(collectionName, cardName) {
    let card = {
        name: cardName,
        order: 0
    };

    model.collections.find(c => c.name == collectionName).cards.push(card);

    let cardElement = document.createElement("div");
    card.element = cardElement;
    cardElement.classList.add("card");

    cardElement.id = cardName;
    cardElement.addEventListener('click', (e) => {
        initiateMove(cardName);
    });

    let imgElement = document.createElement("img");
    imgElement.src = "cards.png";
    cardElement.appendChild(imgElement);

    let scaleX = cWidth / 410;
    let scaleY = cHeight / 623;

    let translateX = -410;
    let translateY = 0;

    let suit = cardName[0];
    let nominal = cardName[1];

    switch (suit) {
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

    switch (nominal) {
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

    imgElement.style.transform = "scale(" + scaleX + "," + scaleY + ") translate(" + translateX + "px, " + translateY + "px)";

    model.deskElement.appendChild(cardElement);

    recalculateCardPositions();
}

function move(cardName, fromCollectionName, toCollectionName) {
    console.log('moving card ' + cardName + ' from ' + fromCollectionName + ' to ' + toCollectionName);

    let fromCollection = model.collections.find(c => c.name == fromCollectionName);
    let toCollection = model.collections.find(c => c.name == toCollectionName);

    let card = fromCollection.cards.find(c => c.name == cardName);

    if (card == null) {
        console.log("CARD " + cardName + " IS UNDEFINED");
        return;
    }

    if (toCollection.cards.length == 0) {
        card.order = 1;
    } 
    else {
        card.order = toCollection.cards[toCollection.cards.length - 1].order + 1;
    }

    fromCollection.cards = fromCollection.cards.filter(c => c.name != cardName);
    toCollection.cards.push(card);

    recalculateCardPositions();
}


//////


function getName() {
    fetch("api?command=enter")
        .then((response) => response.json())
            .then((data) => {
                const title = document.getElementById("title");

                title.innerHTML = data.name;

                username = data.name;
            });
}

function enterGame() {
    fetch("api?command=enterGame")
        .then((response) => response.json())
        .then((data) => {
            const loginDiv = document.getElementById("login");
            const gameDiv = document.getElementById("game");

            loginDiv.style.display = "none";
            gameDiv.style.display = "block";

            createClientModel(data);
            getGameHistory();
    });
}

function createClientModel(serverModel) {
    model =  {
        collections: [{
            name: "hand2",
            layout: "hand",
            cards: []
        }, 
        {
            name: "hand1",
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
        player2: serverModel.player2
    };

    for (let i in serverModel.deck) {
        addCard("deck", serverModel.deck[i]);
    }

    for (let i in serverModel.out) {
        addCard("out", serverModel.out[i]);
    }

    for (let i in serverModel.hand1) {
        addCard("hand1", serverModel.hand1[i]);
    }

    for (let i in serverModel.hand2) {
        addCard("hand2", serverModel.hand2[i]);
    }

    for (let i in serverModel.field) {
        addCard("field", serverModel.field[i]);
    }
}

function getGameHistory() {
    fetch("api?command=getGameHistory&startingIndex=" + model.nextMoveIndex)
        .then((response) => response.json())
        .then((data) => {
            updateGame(data);
        });

    setTimeout(getGameHistory, 1000);
}

function updateGame(gameHistory) {
    const field = document.getElementById("field");

    if (gameHistory.gameState == "WAITING") {
        field.innerHTML = "Waiting for other player!";
    } else {
        if (model.gameState == "WAITING") {
            // TODO: find out how to get names
        }

        for (let i = 0; i < gameHistory.moves.length; i++) {
            let move1 = gameHistory.moves[i];

            setTimeout(() => move(move1.card, move1.from, move1.to), 100 * i);
            model.nextMoveIndex++;
        }
    }
}

function initiateMove(cardName) {
    fetch("api?command=initiateMove&startingIndex=" + model.nextMoveIndex + "&card=" + cardName)
    .then((response) => response.json())
    .then((data) => {
        updateGame(data);
    })
}

function initiateButton(buttonName) {
    fetch("api?command=initiateButton&startingIndex=" + model.nextMoveIndex + "&buttonName=" + buttonName)
    .then((response) => response.json())
    .then((data) => {
        updateGame(data);
    })
}