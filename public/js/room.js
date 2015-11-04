/*
    Romm class
    EE 4216 Group 4
*/

function Room(waiting, owner) {
    this.waiting = waiting;
    this.owner = owner;
    this.player = undefined;
    this.element = undefined;
}

// initialize template render
Room.prototype.render = doT.compile($('#room-template').html());

// draw into the html
Room.prototype.draw = function(roomList) {
    var html = Room.prototype.render(this);
    this.element = $(html);
    $(roomList).append(this.element);
};