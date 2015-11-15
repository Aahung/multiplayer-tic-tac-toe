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
            if (room.player) r.setPlayer(room.player);
            r.draw($('#room-list')[0], joinRoom);
        }
        __updateAdminControls();
    } else if (msg.type == "user") {
        // update users
        $('#user-list').empty();
        for (var i = 0; i < msg.users.length; ++i) {
            var user = msg.users[i];
            var u = new User(user.nickname, user.image, user.type);
            u.draw($('#user-list')[0]);
        }
        $("#user-count-label").text(msg.users.length);
        __updateAdminControls();
    } else if (msg.type == "the_game") {
        drawCanvas(msg.game.owner, msg.game.player);
        if (msg.game.result != 0) {
            if (msg.room.owner.nickname == _nickname && msg.game.result == 1
                || msg.room.player.nickname == _nickname && msg.game.result == -1) {
                $('#win-modal').foundation('reveal', 'open');
            } else {
                $('#lose-modal').foundation('reveal', 'open');
            }
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
        } else if (msg.command == "nickname_exist") {
            alert("This name is already been taken, sorry.");
        } else if (msg.command == "room_created") {
            $('#game-block').fadeIn();
        } else if (msg.command == "room_joined") {
            $('#game-block').fadeIn();
        } else if (msg.command == "room_quited") {
            $('#game-block').fadeOut();
        } else if (msg.command == "kicked_game") {
            _nickname = undefined;
            $('#signup-modal').foundation('reveal', 'open');
        }

        // start to handle admin methods
        else if (msg.command == "admin_authed") {
            $("#admin-login-modal").foundation('reveal', 'close');
            __updateAdminControls();
        }
    }
}

function joinRoom(owner) {
    // send to server to join the room
    var msg = {
        "type": "command",
        "command": "join_room",
        "owner": owner.nickname
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

    nickname = escape(nickname); // just in case

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
    var img = document.getElementById("canvas");
    var pat = ctx.createPattern(img,"no-repeat");

    ctx.lineWidth = 2;
    ctx.fillStyle = pat;
    ctx.fillRect(0, 0, c.width, c.height);

    // draw grid
    ctx.beginPath();
    ctx.moveTo(0, c.height / 3.0);
    ctx.lineTo(c.width, c.height / 3.0);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(0, 2 * c.height / 3.0);
    ctx.lineTo(c.width, 2 * c.height / 3.0);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(c.width / 3.0, 0);
    ctx.lineTo(c.width / 3.0, c.height);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(2 * c.width / 3.0, 0);
    ctx.lineTo(2 * c.width / 3.0, c.height);
    ctx.stroke();

    // draw owner circle
    ctx.lineWidth = 10;
    var markRadius = Math.min(c.height, c.width) / 8.0;
    for (var i = 0; i < ownerDots.length; ++i) {
        var row = Math.floor(ownerDots[i] / 3);
        var col = ownerDots[i] - 3 * row;
        var centerX = (2 * col) * c.width / 6.0;
        var centerY = ( 2 * row) * c.height / 6.0; 
    
        ctx.drawImage(document.getElementById('circle'), centerX, centerY, c.width / 3, c.height / 3);
    }

    // draw the cross
    for (var i = 0; i < playerDots.length; ++i) {
        var row = Math.floor(playerDots[i] / 3);
        var col = playerDots[i] - 3 * row;
        var centerX = (2 * col) * c.width / 6.0;
        var centerY = ( 2 * row) * c.height / 6.0; 
        ctx.drawImage(document.getElementById('cross'), centerX, centerY, c.width / 3, c.height / 3);
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
    Admin
    all admin functions start with __
    to better distinguished from other functions
*/

function __updateAdminControls() {
    if (_nickname == "__admin__") {
        $(".admin-only").show();
    } else {
        $(".admin-only").hide();
    }
}

function __validateAdminPassword() {
    var password = $('#admin-password').val();
    var msg = {
        "type": "admin",
        "command": "auth",
        "password": password 
    };

    if (webSocketReady()) {
        webSocketSend(msg);
    }
}

function __kickoutUserFromConsole(nickname) {
    var msg = {
        "type": "admin",
        "command": "kick_user_console",
        "nickname": nickname 
    };

    if (webSocketReady()) {
        webSocketSend(msg);
    }
}

function __kickoutUserFromGame(nickname) {
    var msg = {
        "type": "admin",
        "command": "kick_user_game",
        "nickname": nickname 
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

    // admin
    $('#admin-button').click(function() {
        _nickname = "__admin__";
        $('#admin-login-modal').foundation('reveal', 'open');
        // focus on the input field
        $('#admin-password').focus();
    });

    $('#admin-login-button').click(__validateAdminPassword);
    $('#admin-password').keypress(function (e) {
        if (e.keyCode == 13) {
            e.preventDefault();
            __validateAdminPassword();
        }
    });
});