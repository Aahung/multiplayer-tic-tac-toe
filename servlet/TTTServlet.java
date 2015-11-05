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
    public TTTCallback1P<TTTRoom> _onRoomStateChangeListener;

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
                System.out.println("onRoomChange called from object: " + Integer.toHexString(System.identityHashCode(sender)));
            }
        };

        _gameConsole = new TTTConsole();
        _gameConsole.setOnRoomChangeListener(_onRoomChangeListener);
        _gameConsole.setOnUserChangeListener(_onUserChangeListener);
        _gameConsole.setOnRoomStateChangeListener(_onRoomStateChangeListener);

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

    private class TTTMessageInbound extends MessageInbound {
        WsOutbound myoutbound;
        TTTUser user;

        @Override
        public void onOpen(WsOutbound outbound){
            try {
                System.out.println("Open Client.");
                this.myoutbound = outbound;
                mmiList.add(this);
                outbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"msg\",\"level\":\"log\",\"content\":\"Hello!\"}"));

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
            TTTUser userToRemove = user;
            user = null;
            _gameConsole.removeUser(userToRemove);
            _userToTTTMIB.remove(userToRemove);
            mmiList.remove(this);
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
                    String command = obj.get("command").toString();
                    if (command.equals("create_room")) {
                        // create room
                        if (user != null) {
                            if (!_gameConsole.createRoom(user)) {
                                myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"msg\",\"level\":\"alert\",\"content\":\"Please quit a room first.\"}"));
                            } else {
                                broadcastMessage(getRoomListAsJSONObject().toString());
                                myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"room_created\"}"));
                            }
                        } else {
                            myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"msg\",\"level\":\"alert\",\"content\":\"Sign up first.\"}"));
                        }
                    } else if (command.equals("join_room")) {
                        String ownerNickname = obj.get("owner").toString();
                        TTTUser owner = _gameConsole.searchUser(ownerNickname);
                        if (owner == null) {
                            myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"msg\",\"level\":\"alert\",\"content\":\"No room with owner " + ownerNickname + ".\"}"));
                        } else {
                            TTTRoom room = _gameConsole.getRoomByOwner(owner);
                            if (room == null) {
                                myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"msg\",\"level\":\"alert\",\"content\":\"No room with owner " + ownerNickname + ".\"}"));
                            } else {
                                if (_gameConsole.joinRoom(user, room)) {
                                    myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"room_joined\"}"));
                                } else {
                                    myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"msg\",\"level\":\"alert\",\"content\":\"Cannot join the room.\"}"));
                                }
                            }
                        }
                    } else if (command.equals("quit_room")) {
                        TTTRoom room = _gameConsole.getRoomByUser(user);
                        _gameConsole.quitRoom(user, room);
                        myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"room_quited\"}"));
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
