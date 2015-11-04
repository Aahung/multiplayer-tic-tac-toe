/*
	Console class: handle all the rooms
	EE 4216 Group 4
*/


package ee4216;

import java.util.*;
import org.json.simple.*;

import ee4216.TTTServlet;
import ee4216.TTTUser;
import ee4216.TTTGame;

public class TTTConsole {
	private TTTServlet _servlet;
	private List<TTTRoom> _rooms;
	private List<TTTUser> _users;

	public TTTConsole(TTTServlet servlet) {
		_servlet = servlet;
		_rooms = new ArrayList<TTTRoom>();
		_users = new ArrayList<TTTUser>();
	}

	// return true if user's nickname is available
	public TTTUser addUser(String nickname) {
		if (searchUser(nickname) == null) {
			TTTUser user = new TTTUser(nickname);
			_users.add(user);
			_servlet.onUserChange();
			return user;
		}
		return null;
	}

	public void removeUser(TTTUser user) {
		if (user == null) return;
		_users.remove(user);
		_servlet.onUserChange();
		// remove all the room whose owner is user and it is waiting
		TTTRoom ownRoom = getRoomByOwner(user);
		if (ownRoom != null && ownRoom.isWaiting()) {
			_rooms.remove(ownRoom);
		}
		for (TTTRoom room: _rooms) {
			if (room.getPlayer() == user) {
				room.escape(user);
			}
		}
		_servlet.onRoomChange();
	}

	public JSONArray dumpUsers() {
		JSONArray array = new JSONArray();

		for (TTTUser user: _users) {
			array.add(user.toJSONObject());
		}

		return array;
	}

	public JSONArray dumpRooms() {
		JSONArray array = new JSONArray();

		for (TTTRoom room: _rooms) {
			array.add(room.toJSONObject());
		}

		return array;
	}

	public TTTUser searchUser(String nickname) {
		for (TTTUser user: _users) {
			if (user.getNickname().equals(nickname)) {
				return user;
			}
		}
		return null;
	}

	public TTTRoom getRoomByOwner(TTTUser owner) {
		for (TTTRoom room: _rooms) {
			if (room.getOwner() == owner) {
				return room;
			}
		}
		return null;
	}

	public boolean createRoom(TTTUser user) {
		for (TTTRoom room: _rooms) {
			if (room.getOwner() == user) return false;
			if (room.getPlayer() == user) return false;
		}
		
		TTTRoom room = new TTTRoom(user);
		_rooms.add(room);
		_servlet.onRoomChange();
		return true;
	}

	public boolean joinGame(TTTUser player, TTTGame game) {
		return false;
	}

	public boolean moveGame(TTTUser owner, TTTUser mover, int dotIndex) {
		return false;
	}
}
