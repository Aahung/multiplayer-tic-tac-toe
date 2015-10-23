# Requirements

View the [original copy](https://docs.google.com/document/d/1UL-GkEOVLBIC0An_WnGQYBeD7sOL6j7G27shfjxjQBg/pub)

## Level 1 Requirements (items marked with * are exclusive for level 2)

- [ ] \*support one networked game at a time only
- [ ] \*any two players first sign in to the server will be matched to start a game
- [ ] \*any third player will be rejected until the current game session is ended
- [ ] players sign in by entering their name which must uniquely identify the player
- [ ] player names are displayed in the game
- [ ] run the regular Tic-Tac-Toe gameplay and determine the winner
- [ ] if one of the players leaves the game, the other player will be notified to end the session  (note that players may implicitly leave the game by closing the browser

## Level 2 Requirements

- [ ] support multiple games concurrently with a game room setting
- [ ] display all on-going games and opened games in the game room
- [ ] display the total number of players in the game room (including those wandering around)
- [ ] let players to open a new game, join an existing game, and cancel it as well

## Level 3 Requirements

- [ ] allow administrator to clear games and kick out players
- [ ] all communications must be done with asynchronous method (i.e. no page refresh for periodic polling)
- [ ] beautify your game with multimedia and CSS
- [ ] host your server in a public accessible domain

## Bonus Feature

- [ ] Integrate Facebook Login such that players can use Facebook account to sign in and their name and profile picture will be displayed in the game https://developers.facebook.com/docs/facebook-login/web
