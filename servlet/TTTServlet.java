/*
    Servlet control ALL
    borrow backbone from @chitan's gist on https://gist.github.com/chitan/3063774
    EE 4216 Group 4
*/

package ee4216;

import javax.servlet.http.HttpServletRequest;

import java.util.*;
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

import ee4216.TTTGame;
import ee4216.TTTRoom;
import ee4216.TTTUser;
import ee4216.TTTConsole;

public class TTTServlet extends WebSocketServlet{
    private static final long serialVersionUID = 1L;
    private static ArrayList<TTTMessageInbound> mmiList = new ArrayList<TTTMessageInbound>();

    private TTTConsole _gameConsole;

    public TTTServlet() {
        _gameConsole = new TTTConsole();
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
            _gameConsole.removeUser(user);
            mmiList.remove(this);

            // broadcast user info
            broadcastMessage(getUserListAsJSONObject().toString());
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
                        // fail
                        myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"nickname_reserved\"}"));
                        // broadcast user info to all users

                        JSONObject usersObj = new JSONObject();
                        usersObj.put("type", "user");
                        usersObj.put("users", _gameConsole.dumpUsers());

                        broadcastMessage(usersObj.toString());
                    } else {
                        myoutbound.writeTextMessage(CharBuffer.wrap("{\"type\":\"command\",\"command\":\"nickname_exist\"}"));
                    }
                }
                
            } catch (ParseException e) {
                System.out.println("position: " + e.getPosition());
                System.out.println(e);
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        @Override
        public void onBinaryMessage(ByteBuffer bb) throws IOException{
        }
    }
}
