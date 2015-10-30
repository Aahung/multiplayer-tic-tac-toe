var _nickname = undefined;
var _dots;

function validateNickname() {
    var nickname = $('#nickname-input').val();
    console.log('nickname: ' + nickname + ' got');
    if (!nickname || nickname.length == 0) {
        alert('Don\'t leave your nickname blank.');
        return;
    }

    // save nickname into the browser if localstorage is available
    if(typeof(Storage) !== "undefined") {
        localStorage.setItem('nickname', nickname);
    }

    _nickname = nickname;
    $('#signup-modal').foundation('reveal', 'close');
}

// initialization
$(document).foundation();
$(function() {

    // load nickname if has
    if(typeof(Storage) !== "undefined") {
        $('#nickname-input').val(localStorage.getItem('nickname'));
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
    $('#nickname-input').keyup(function (e) {
        if (e.keyCode == 13) {
            e.preventDefault();
            validateNickname();
        }
    });
});