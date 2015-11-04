# Message Protocol

## Server -> Client
```json
msg = {
    "type": {"room"|"game"|"msg"},
    ..
}
```

## type=room
```json
msg = {
    "type": "room",
    "rooms": [{
    	"waiting": {true|false},
    	"owner": "..",
    	"player": {".."|undefined}
    	"player": {'.."|null}   ?
    }]
}
```

After receiving type=room message, it will update all rooms with the contents.

## type=game
```json
msg = {
    "type": "game",
    "subtype": {"update"|"result"}
}
```

### subtype=update
```json
msg = {
    "type": "game",
    "subtype": "update",
    "owner": [{0~8}, {0~8}, .., {0~8}],
    "player": [{0~8}, {0~8}, .., {0~8}]
}
```

### subtype=result
```json
msg = {
    "type": "game",
    "subtype": "result",
    "winner": {"owner"|"player"|"tie"}
}
```

### type=msg
```json
msg = {
    "type": "msg",
    "content": {"cancel"|"leave"|"pause"}
    
    //use cancel to end the game, room closed
    //leave: that user wants to leave the room, room open, waiting=1
    //pause: the user will come back soon
}
```
