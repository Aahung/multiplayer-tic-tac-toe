/*
	Console class: handle all the rooms
	EE 4216 Group 4
*/


package ee4216;

import java.util.*;
import org.json.simple.*;

import ee4216.TTTUser;
import ee4216.TTTGame;

public class TTTConsole {
	private List<TTTRoom> _rooms;
	private List<TTTUser> _users;

	public TTTConsole() {
		_rooms = new ArrayList<TTTRoom>();
		_users = new ArrayList<TTTUser>();
	}

	// return true if user's nickname is available
	public boolean addUser(String nickname) {
		if (searchUser(nickname) == null) {
			_users.add(new TTTUser(nickname));
			return true;
		}
		return false;
	}


	public JSONArray dumpUsers() {
		JSONArray array = new JSONArray();

		for (TTTUser user: _users) {
			array.add(user.toJSONObject());
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

	public TTTUser getUserByConnectionId(String connectionId) {
		return null;
	}

	public TTTGame getGameByOwner(TTTUser owner) {
		return null;
	}

	public boolean addGame(TTTUser user) {
		return false;
	}

	public boolean joinGame(TTTUser player, TTTGame game) {
		return false;
	}

	public boolean moveGame(TTTUser owner, TTTUser mover, int dotIndex) {
		return false;
	}
}
