/*
    Romm class
    EE 4216 Group 4
*/

function Room(waiting, owner) {
    this.waiting = waiting;
    this.owner = owner;
    this.player = undefined;
	this.element=undefined;
    this.viewers = [];
}

// join player
Room.prototype.join = function(player) {
    if (this.waiting) {
        // join the game
		this.player=player;
		this.waiting=0;
		onReceiveUpdate({
			"type": "room",
			"rooms": [{
			"waiting": 0,
            "owner": this.owner,
			"player": this.player,
			}]
		});
		
    } else {
        // view the game
		viewers.push(player);
    }
};

// initialize template render
Room.prototype.render = doT.compile($('#room-template').html());

// draw into the html
Room.prototype.draw = function(roomList) {
    var html = Room.prototype.render(this);
    this.element = $(html);
    $(roomList).append(this.element);
};
