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

    private class TTTMessageInbound extends MessageInbound{
        WsOutbound myoutbound;

        @Override
        public void onOpen(WsOutbound outbound){
            try {
                System.out.println("Open Client.");
                this.myoutbound = outbound;
                mmiList.add(this);
                outbound.writeTextMessage(CharBuffer.wrap("Hello!"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(int status){
            System.out.println("Close Client.");
            mmiList.remove(this);
        }

        @Override
        public void onTextMessage(CharBuffer cb) throws IOException{
            System.out.println("Accept Message : "+ cb);
            String message = cb.toString();
            JSONParser parser = new JSONParser();
            try {
                Object obj = parser.parse(message);
                for(TTTMessageInbound mmib: mmiList){
                    CharBuffer buffer = CharBuffer.wrap(obj.get("type").toString());
                    mmib.myoutbound.writeTextMessage(buffer);
                    mmib.myoutbound.flush();
                }
            } catch (ParseException e) {
                System.out.println("position: " + e.getPosition());
                System.out.println(e);
            }
        }

        @Override
        public void onBinaryMessage(ByteBuffer bb) throws IOException{
        }
    }
}
