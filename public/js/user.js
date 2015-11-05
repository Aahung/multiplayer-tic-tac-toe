/*
    User class
    EE 4216 Group 4
*/

function User(nickname, image, type) {
    this.nickname = nickname;
    this.image = image;
    this.type = type;
}

// initialize template render
User.prototype.render = doT.compile($('#user-template').html());

// draw into the html
User.prototype.draw = function(userList) {
    var html = User.prototype.render(this);
    this.element = $(html);
    $(userList).append(this.element);
};