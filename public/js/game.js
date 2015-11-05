/*
    main game js
    EE 4216 Group 4
*/

/*
    Websocket
*/

var _ws;

function webSocketReady() {
    if (_ws && _ws.readyState == 1)
        return true;
    initWebSocket();
    alert("Please check your network connection.");
    return false;
}

function webSocketSend(msg) {
    _ws.send(JSON.stringify(msg));
}

var onWebSocketOpen = function() {
    console.log("[WebSocket] connected.");
};

var onWebSocketClose = function() {
    console.log("[WebSocket] disconnected.");
};

var onWebSocketMessage = function(e) {
    console.log("[WebSocket] reveived message:");
    console.log(e.data);
    onReceiveMessage(JSON.parse(e.data));
};

function initWebSocket() {
    var loc = window.location, new_uri;
    if (loc.protocol === "https:") {
        new_uri = "wss:";
    } else {
        new_uri = "ws:";
    }
    new_uri += "//" + loc.host;
    new_uri += "/api";

    _ws = new WebSocket(new_uri);

    // setup the listeners
    _ws.onmessage = onWebSocketMessage;
    _ws.onopen = onWebSocketOpen;
    _ws.onclose = onWebSocketClose;
}

/*
    interactions
*/

var _nickname, _nicknameCandidate;

function onReceiveMessage(msg) {
    if (msg.type == "room") {
        // update rooms
        $('#room-list').empty();
        for (var i = 0; i < msg.rooms.length; ++i) {
            var room = msg.rooms[i];
            var r = new Room(room.waiting, room.owner);
            if (!room.waiting) r.player = room.player;
            r.draw($('#room-list')[0], joinRoom);
        }
    } else if (msg.type == "user") {
        // update users
        $('#user-list').empty();
        for (var i = 0; i < msg.users.length; ++i) {
            var user = msg.users[i];
            var u = new User(user.nickname, user.image, user.type);
            u.draw($('#user-list')[0]);
        }
    } else if (msg.type == "game") {
        if (msg.subtype == "update") {
            drawCanvas(msg.owner, msg.player);
        }
    } else if (msg.type == "msg") {
        if (msg.level == "alert")
            alert(msg.content);
        console.log(msg.content);
    } else if (msg.type == "command") {
        if (msg.command == "nickname_reserved") {
            // successfully registed the nickname
            _nickname = _nicknameCandidate;
            $('#signup-modal').foundation('reveal', 'close');
        } else if (msg.command == "room_created") {
            $('#game-block').fadeIn();
        } else if (msg.command == "room_joined") {
            $('#game-block').fadeIn();
        } else if (msg.command == "room_quited") {
            $('#game-block').fadeOut();
        }
    }
}

function joinRoom(ownerNickname) {
    // send to server to join the room
    var msg = {
        "type": "command",
        "command": "join_room",
        "owner": ownerNickname
    };

    if (webSocketReady()) {
        webSocketSend(msg);
    }
}

function createRoom() {
    // send to server to verify
    var msg = {
        "type": "command",
        "command": "create_room" 
    };

    if (webSocketReady()) {
        webSocketSend(msg);
    }
}

function quitRoom() {
    // send to server to quit the room
    var msg = {
        "type": "command",
        "command": "quit_room"
    };

    if (webSocketReady()) {
        webSocketSend(msg);
    }
}

function validateNickname() {
    var nickname = $('#nickname-input').val();
    console.log('nickname: ' + nickname + ' got');
    if (!nickname || nickname.length == 0) {
        alert('Don\'t leave your nickname blank.');
        return;
    }

    // save nickname into the browser if localstorage is available
    if(typeof(Storage) !== "undefined") {
        try {
            localStorage.setItem('nickname', nickname);
        } catch (e) {
            console.log(e);
        }
    }

    _nicknameCandidate = nickname;
    // send to server to verify
    var msg = {
        "type": "init",
        "nickname": _nicknameCandidate
    };

    if (webSocketReady()) {
        webSocketSend(msg);
    }
}

function drawCanvas(ownerDots, playerDots) {
    var c = document.getElementById("game-canvas");
    var ctx = c.getContext("2d");
    ctx.fillStyle = "#ffffff";
    ctx.fillRect(0, 0, c.width, c.height);

    // draw grid
    ctx.lineWidth = 2;
    ctx.moveTo(0, c.height / 3.0);
    ctx.lineTo(c.width, c.height / 3.0);
    ctx.stroke();
    ctx.moveTo(0, 2 * c.height / 3.0);
    ctx.lineTo(c.width, 2 * c.height / 3.0);
    ctx.stroke();
    ctx.moveTo(c.width / 3.0, 0);
    ctx.lineTo(c.width / 3.0, c.height);
    ctx.stroke();
    ctx.moveTo(2 * c.width / 3.0, 0);
    ctx.lineTo(2 * c.width / 3.0, c.height);
    ctx.stroke();

    // draw owner circle
    ctx.lineWidth = 10;
    var markRadius = Math.min(c.height, c.width) / 8.0;
    for (var i = 0; i < ownerDots.length; ++i) {
        var row = Math.floor(ownerDots[i] / 3);
        var col = ownerDots[i] - 3 * row;
        var centerX = (1 + 2 * col) * c.width / 6.0;
        var centerY = (1 + 2 * row) * c.height / 6.0;
        ctx.beginPath();
        ctx.arc(centerX, centerY, markRadius, 0, 2*Math.PI);
        ctx.stroke();
    }

    // draw the cross
    for (var i = 0; i < playerDots.length; ++i) {
        var row = Math.floor(playerDots[i] / 3);
        var col = playerDots[i] - 3 * row;
        var centerX = (1 + 2 * col) * c.width / 6.0;
        var centerY = (1 + 2 * row) * c.height / 6.0;
        ctx.moveTo(centerX - markRadius, centerY - markRadius);
        ctx.lineTo(centerX + markRadius, centerY + markRadius);
        ctx.stroke();
        ctx.moveTo(centerX + markRadius, centerY - markRadius);
        ctx.lineTo(centerX - markRadius, centerY + markRadius);
        ctx.stroke();
    }
}

function onCanvasMouseUp(event) {
    var c = document.getElementById("game-canvas");
    var mouseX, mouseY;
    if ( event.offsetX == null ) { // Firefox
        mouseX = event.originalEvent.layerX;
        mouseY = event.originalEvent.layerY;
    } else {                       // Other browsers
        mouseX = event.offsetX;
        mouseY = event.offsetY;
    }
    var row = Math.floor(3 * mouseY / c.height);
    var col = Math.floor(3 * mouseX / c.width);

    var dotIndex = row * 3 + col;
    console.log("dot " + dotIndex + " clicked.");

    // send to server to verify the move
    var msg = {
        "type": "command",
        "command": "move_game",
        "dot_index": dotIndex
    };

    if (webSocketReady()) {
        webSocketSend(msg);
    }
}

/* 
    initialization
*/

$(document).foundation();
$(function() {

    // load nickname if has
    if(typeof(Storage) !== "undefined") {
        try {
            $('#nickname-input').val(localStorage.getItem('nickname'));
        } catch (e) {
            console.log(e);
        }
    }

    // modal
    $('#signup-modal').foundation('reveal', 'open');
    $(document).on('closed.fndtn.reveal', '[data-reveal]', function (e) {
        var modal = $(this);
        if (modal.attr('id') == 'signup-modal') {
            if (!_nickname) {
                $('#signup-modal').foundation('reveal', 'open');
            }
        }
    });

    // input enter trigger
    $('#nickname-input').keypress(function (e) {
        if (e.keyCode == 13) {
            e.preventDefault();
            validateNickname();
        }
    });

    // init websocket
    initWebSocket();

    // add listener to canvas
    var canvas = document.getElementById("game-canvas");
    canvas.addEventListener("mouseup", onCanvasMouseUp, false);
});