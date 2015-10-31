/*
    main game js
    EE 4216 Group 4
*/
var _nickname;

function onReceiveUpdate(msg) {
    if (msg.type == "room") {
        // update rooms
        $('#room-list').empty();
        for (var i = 0; i < msg.rooms.length; ++i) {
            var room = msg.rooms[i];
            var r = new Room(room.waiting, room.owner);
            if (!room.waiting) r.player = room.player;
            r.draw($('#room-list')[0]);
        }
    } else if (msg.type == "game") {
        
    } else if (msg.type == "msg") {
        alert(msg.content);
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

    _nickname = nickname;
    $('#signup-modal').foundation('reveal', 'close');
}

// initialization
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
});