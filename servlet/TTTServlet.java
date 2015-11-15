/*
    Servlet control ALL
    borrow backbone from @chitan's gist on https://gist.github.com/chitan/3063774
    EE 4216 Group 4
*/

package ee4216;

import javax.servlet.http.HttpServletRequest;

import java.util.*;
import java.text.SimpleDateFormat;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WebSocketServlet;
import org.apache.catalina.websocket.WsOutbound;

import ee4216.*;

public class TTTServlet extends WebSocketServlet{
    private static final long serialVersionUID = 1L;
    private static ArrayList<TTTMessageInbound> mmiList = new ArrayList<TTTMessageInbound>();
    private static Map<TTTUser, TTTMessageInbound> _userToTTTMIB = new HashMap<TTTUser, TTTMessageInbound>();

    private TTTConsole _gameConsole;

    // callbacks
    public TTTCallback _onRoomChangeListener, _onUserChangeListener;
    public TTTCallback1P<TTTRoom> _onRoomStateChangeListener, _onGameChangeListener;

    public TTTServlet() {

        // init callback
        _onRoomChangeListener = new TTTCallback() {
            @Override
            public void call(Object sender) {
                broadcastMessage(getRoomListAsJSONObject().toString());
                System.out.println("onRoomChange called from object: " + Integer.toHexString(System.identityHashCode(sender)));
            }
        };

        _onUserChangeListener = new TTTCallback() {
            @Override
            public void call(Object sender) {
                broadcastMessage(getUserListAsJSONObject().toString());
                System.out.println("onRoomChange called from object: " + Integer.toHexString(System.identityHashCode(sender)));
            }
        };

        _onRoomStateChangeListener = new TTTCallback1P<TTTRoom>() {
            @Override
            public void call(Object sender, TTTRoom room) {
                TTTUser owner = room.getOwner();
                TTTUser player = room.getPlayer();

                JSONObject roomObj = new JSONObject();
                roomObj.put("type", "the_room");
                roomObj.put("room", room.toJSONObject());

                try {
                    _userToTTTMIB.get(owner).myoutbound.writeTextMessage(CharBuffer.wrap(roomObj.toString()));
                    if (player != null)
                        _userToTTTMIB.get(player).myoutbound.writeTextMessage(CharBuffer.wrap(roomObj.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("onRoomStateChange called from object: " + Integer.toHexString(System.identityHashCode(sender)));
            }
        };

        _onGameChangeListener = new TTTCallback1P<TTTRoom>() {
            @Override
            public void call(Object sender, TTTRoom room) {
                TTTUser owner = room.getOwner();
                TTTUser player = room.getPlayer();

                JSONObject roomObj = new JSONObject();
                roomObj.put("type", "the_game");
                roomObj.put("game", room.getGame().toJSONObject());
                roomObj.put("room", room.toJSONObject());

                try {
                    _userToTTTMIB.get(owner).myoutbound.writeTextMessage(CharBuffer.wrap(roomObj.toString()));
                    if (player != null)
                        _userToTTTMIB.get(player).myoutbound.writeTextMessage(CharBuffer.wrap(roomObj.toString()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("onGameChange called from object: " + Integer.toHexString(System.identityHashCode(sender)));
            }
        };

        _gameConsole = new TTTConsole();
        _gameConsole.setOnRoomChangeListener(_onRoomChangeListener);
        _gameConsole.setOnUserChangeListener(_onUserChangeListener);
        _gameConsole.setOnRoomStateChangeListener(_onRoomStateChangeListener);
        _gameConsole.setOnGameChangeListener(_onGameChangeListener);

        // setup beacon sending task
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new SendBeaconTask(), 0, 10000);
    }

    @Override
    protected StreamInbound createWebSocketInbound(String protocol, HttpServletRequest request){
        return new TTTMessageInbound();
    }

    private void broadcastMessage(String msg) {
        try {
            for(TTTMessageInbound mmib: mmiList){
                CharBuffer buffer = CharBuffer.wrap(msg);
                mmib.myoutbound.writeTextMessage(buffer);
                mmib.myoutbound.flush();
                String nickname = null;
                if (mmib.user != null) nickname = mmib.user.getNickname();
                System.out.println(String.format("broadcasting to %s, nickname %s",
                                                 Integer.toHexString(System.identityHashCode(mmib)), 
                                                 nickname));
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /*
        Beacon sending
    */
    class SendBeaconTask extends TimerTask {
        public void run() {
            sendBeacon();
        }
    }

    private void sendBeacon() {
        // send the server time to all clients
        // just to keep the connection active
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date());
        broadcastMessage("{\"type\":\"msg\",\"level\":\"log\",\"content\":\"Server time " + timeStamp + ".\"}");
    }

    private void quitRoom(TTTUser user) {
        TTTRoom room = _gameConsole.getRoomByUser(user);

        if (room != null) {
            // notify another user in the room
            TTTUser theOtherUser = null;
            if (room.getOwner() == user) {
                theOtherUser = room.getPlayer();
            } else {
                theOtherUser = room.getOwner();
            }
            TTTMessageInbound theOtherMessageInBound = _userToTTTMIB.get(theOtherUser);
            if (theOtherMessageInBound != null) {
                try {
                    theOtherMessageInBound.myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"msg\",\"level\":\"alert\",\"content\":\"The other player quits.\"}"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            _gameConsole.quitRoom(user, room);
        }
    }

    private JSONObject getUserListAsJSONObject() {
        JSONObject usersObj = new JSONObject();
        usersObj.put("type", "user");
        usersObj.put("users", _gameConsole.dumpUsers());

        return usersObj;
    }

    private JSONObject getRoomListAsJSONObject() {
        JSONObject usersObj = new JSONObject();
        usersObj.put("type", "room");
        usersObj.put("rooms", _gameConsole.dumpRooms());

        return usersObj;
    }

    private String JSONMessage(String content, String level) {
        JSONObject obj = new JSONObject();
        
        obj.put("type", "msg");
        obj.put("level", level);
        obj.put("content", content);

        return obj.toString(); 
    }

    private class TTTMessageInbound extends MessageInbound {
        WsOutbound myoutbound;
        public TTTUser user;
        private final static String _adminPassword = "eeee4216";
        private boolean _isAdmin = false;

        @Override
        public void onOpen(WsOutbound outbound){
            try {
                System.out.println("Open Client.");
                this.myoutbound = outbound;
                mmiList.add(this);
                outbound.writeTextMessage(CharBuffer.wrap(JSONMessage("Hello!", "log")));

                // send the user list to him
                outbound.writeTextMessage(CharBuffer.wrap(getUserListAsJSONObject().toString())); 
                // send the room list to him
                outbound.writeTextMessage(CharBuffer.wrap(getRoomListAsJSONObject().toString())); 

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(int status) {
            if (user != null)
                System.out.println(String.format("Close client %s, nickname %s",
                                                 Integer.toHexString(System.identityHashCode(this)), 
                                                 user.getNickname()));
            else 
                System.out.println(String.format("broadcasting to %s",
                                                 Integer.toHexString(System.identityHashCode(this))));
            
            mmiList.remove(this);
            quitRoom(user); // try to quit any room the user is currently in
            TTTUser userToRemove = user;
            user = null;
            _gameConsole.removeUser(userToRemove);
            _userToTTTMIB.remove(userToRemove);
        }

        @Override
        public void onTextMessage(CharBuffer cb) throws IOException{
            System.out.println("Accept Message : "+ cb);
            String message = cb.toString();
            JSONParser parser = new JSONParser();
            try {
                JSONObject obj = (JSONObject)parser.parse(message);
                String type = obj.get("type").toString();
                System.out.println("type: " + type);
                if (type.equals("init")) {
                    String nickname = obj.get("nickname").toString();
                    System.out.println("nickname: " + nickname);
                    // check if there is a same nickname
                    user = _gameConsole.addUser(nickname);
                    if (user != null) {
                        _userToTTTMIB.put(user, this);
                        myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"nickname_reserved\"}"));
                    } else {
                        myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"nickname_exist\"}"));
                    }
                } else if (type.equals("command")) {
                    if (user == null) {
                        myoutbound.writeTextMessage(CharBuffer.wrap(JSONMessage("Sign up first.", "alert")));
                    } else {
                        String command = obj.get("command").toString();
                        if (command.equals("create_room")) {
                            // create room
                            if (!_gameConsole.createRoom(user)) {
                                myoutbound.writeTextMessage(CharBuffer.wrap(JSONMessage("Please quit a room first.", "alert")));
                            } else {
                                broadcastMessage(getRoomListAsJSONObject().toString());
                                myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"room_created\"}"));
                            }
                        } else if (command.equals("join_room")) {
                            String ownerNickname = obj.get("owner").toString();
                            TTTUser owner = _gameConsole.searchUser(ownerNickname);
                            if (owner == null) {
                                myoutbound.writeTextMessage(CharBuffer.wrap(JSONMessage("No room with owner " + ownerNickname + ".", "alert")));
                            } else {
                                TTTRoom room = _gameConsole.getRoomByOwner(owner);
                                if (room == null) {
                                    myoutbound.writeTextMessage(CharBuffer.wrap(JSONMessage("No room with owner " + ownerNickname + ".", "alert")));
                                } else {
                                    if (_gameConsole.joinRoom(user, room)) {
                                        myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"room_joined\"}"));
                                    } else {
                                        myoutbound.writeTextMessage(CharBuffer.wrap(JSONMessage("Cannot join the room.", "alert")));
                                    }
                                }
                            }
                        } else if (command.equals("quit_room")) {
                            quitRoom(user);
                            myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"room_quited\"}"));
                        } else if (command.equals("move_game")) {
                            int dotIndex = ((Long)obj.get("dot_index")).intValue();
                            TTTRoom room = _gameConsole.getRoomByUser(user);
                            if (!_gameConsole.moveGame(user, room, dotIndex))
                                myoutbound.writeTextMessage(CharBuffer.wrap(JSONMessage("The move is invalid.", "alert")));
                        } 
                    }
                } else if (type.equals("admin")) {
                    // admin methods
                    String command = obj.get("command").toString();
                    if (command.equals("auth")) {
                        String password = obj.get("password").toString();
                        if (password.equals(_adminPassword)) {
                            _isAdmin = true;
                            myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"admin_authed\"}"));
                        } else {
                            myoutbound.writeTextMessage(CharBuffer.wrap(JSONMessage("Password is invalid.", "alert")));
                        }
                    } else {
                        if (_isAdmin) {
                            if (command.equals("kick_user_console") || command.equals("kick_user_game")) {
                                String nickname = obj.get("nickname").toString();
                                TTTUser theUser = _gameConsole.searchUser(nickname);
                                if (theUser != null) {
                                    TTTMessageInbound theMessageInBound = _userToTTTMIB.get(theUser);
                                    if (command.equals("kick_user_console")) {
                                        theMessageInBound.user = null;
                                        theMessageInBound.myoutbound.writeTextMessage(CharBuffer.wrap(JSONMessage("You are kicked out by admin.", "alert")));
                                        theMessageInBound.myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"kicked_game\"}"));
                                        _userToTTTMIB.remove(theUser);
                                        _gameConsole.removeUser(theUser);
                                    } else if (command.equals("kick_user_game")) {
                                        TTTRoom theRoom = _gameConsole.getRoomByUser(theUser);
                                        _gameConsole.quitRoom(theUser, theRoom);
                                        theMessageInBound.myoutbound.writeTextMessage(CharBuffer.wrap(JSONMessage("You are kicked out from the room by admin.", "alert")));
                                        theMessageInBound.myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"room_quited\"}"));
                                    }
                                }
                            }
                        } else {
                            myoutbound.writeTextMessage(CharBuffer.wrap(JSONMessage("Unauthorized.", "alert")));
                        }
                    }
                }
                
            } catch (ParseException e) {
                System.out.println("position: " + e.getPosition());
                e.printStackTrace(System.out);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        @Override
        public void onBinaryMessage(ByteBuffer bb) throws IOException{
        }
    }
}
