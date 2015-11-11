/*
    Romm class
    EE 4216 Group 4
*/

function Room(waiting, owner) {
    this.waiting = waiting;
    this.owner = owner;
    if (!this.owner.image) this.owner.image = "images/profile-icon-50.png";
    this.player = undefined;
    this.element = undefined;
}

Room.prototype.setPlayer = function(player) {
    this.player = player;
    if (!this.player.image) this.player.image = "images/profile-icon-50.png";
};

// initialize template render
Room.prototype.render = doT.compile($('#room-template').html());

// draw into the html
Room.prototype.draw = function(roomList, onClickListener) {
    var html = Room.prototype.render(this);
    this.element = $(html);
    var owner = this.owner;
    $(this.element).click(function(e) {
        if( e.target != this ) 
            return;
    	onClickListener(owner);
    });
    $(roomList).append(this.element);
};