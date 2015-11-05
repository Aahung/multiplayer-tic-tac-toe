/*
	Console class: handle all the rooms
	EE 4216 Group 4
*/


package ee4216;

import java.util.*;
import org.json.simple.*;

import ee4216.*;

public class TTTConsole {
	private List<TTTRoom> _rooms;
	private List<TTTUser> _users;
	private static Map<TTTUser, TTTRoom> _userToRoom = new HashMap<TTTUser, TTTRoom>();

	private TTTCallback _onRoomChangeListener, _onUserChangeListener;
	private TTTCallback1P<TTTRoom> _onRoomStateChangeListener, _onGameChangeListener;

	public TTTConsole() {
		_rooms = new ArrayList<TTTRoom>();
		_users = new ArrayList<TTTUser>();
	}

	public void setOnRoomChangeListener(final TTTCallback onRoomChangeListener) {
		_onRoomChangeListener = onRoomChangeListener;
	}

	public void setOnUserChangeListener(final TTTCallback onUserChangeListener) {
		_onUserChangeListener = onUserChangeListener;
	}

	public void setOnRoomStateChangeListener(final TTTCallback1P<TTTRoom> onRoomStateChangeListener) {
		_onRoomStateChangeListener = new TTTCallback1P<TTTRoom>() {
			@Override public void call(Object sender, TTTRoom room) {
				if (_onRoomChangeListener != null)
					_onRoomChangeListener.call(sender);
				onRoomStateChangeListener.call(sender, room);
			}
		};
	}

	public void setOnGameChangeListener(final TTTCallback1P<TTTRoom> onGameChangeListener) {
		_onGameChangeListener = new TTTCallback1P<TTTRoom>() {
			@Override public void call(Object sender, TTTRoom room) {
				if (room.getGame().checkResult() != 0) {
					// game states change
					if (_onRoomStateChangeListener != null)
						_onRoomStateChangeListener.call(sender, room);
				}
				onGameChangeListener.call(sender, room);
			}
		};
	}

	// return true if user's nickname is available
	public TTTUser addUser(String nickname) {
		if (searchUser(nickname) == null) {
			TTTUser user = new TTTUser(nickname);
			_users.add(user);
			if (_onUserChangeListener != null)
				_onUserChangeListener.call(this);
			return user;
		}
		return null;
	}

	public void removeUser(TTTUser user) {
		if (user == null) return;
		_users.remove(user);
		if (_onUserChangeListener != null)
			_onUserChangeListener.call(this);
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
		if (_onRoomChangeListener != null)
			_onRoomChangeListener.call(this);
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

	public TTTRoom getRoomByUser(TTTUser user) {
		return _userToRoom.get(user);
	}

	public boolean createRoom(TTTUser user) {
		for (TTTRoom room: _rooms) {
			if (room.getOwner() == user) return false;
			if (room.getPlayer() == user) return false;
		}
		
		TTTRoom room = new TTTRoom(user);
		room.setOnRoomStateChangeListener(_onRoomStateChangeListener);
		room.setOnGameChangeListener(_onGameChangeListener);
		_rooms.add(room);
		_userToRoom.put(user, room);
		if (_onRoomChangeListener != null)
			_onRoomChangeListener.call(this);
		return true;
	}

	public boolean joinRoom(TTTUser player, TTTRoom room) {
		if (room.join(player)) {
			_userToRoom.put(player, room);
			return true;
		} else {
			return false;
		}
	}

	public void quitRoom(TTTUser user, TTTRoom room) {
		room.escape(user);
		if (room.getOwner() == user) {
			_userToRoom.remove(user);
			_rooms.remove(room);
		}
		if (_onRoomChangeListener != null)
			_onRoomChangeListener.call(this);
	}

	public boolean moveGame(TTTUser mover, TTTRoom room, int dotIndex) {
		return room.move(mover, dotIndex);
	}
}
