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
            r.draw($('#room-list')[0]);
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
        
    } else if (msg.type == "msg") {
        if (msg.level == "alert")
            alert(msg.content);
        console.log(msg.content);
    } else if (msg.type == "command") {
        if (msg.command == "nickname_reserved") {
            // successfully registed the nickname
            _nickname = _nicknameCandidate;
            $('#signup-modal').foundation('reveal', 'close');
        }
    }
}

function createRoom() {
    onReceiveUpdate({
        "type": "room",
        "rooms": [{
            "waiting": 1,
            "owner": _nickname
        }]
    });
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
});