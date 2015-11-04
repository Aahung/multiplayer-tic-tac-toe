/*
	Room class
	EE 4216 Group 4
*/


package ee4216;

import ee4216.TTTUser;
import ee4216.TTTGame;

public class TTTRoom {
	public enum State {
		WAITING, PLAYING
	}

	private State _state;
	private TTTUser _owner, _player;
	private TTTGame _game;

	public TTTRoom(TTTUser owner) {
		_state = State.WAITING;
		_owner = owner;
		_player = null;
		_game = null;
	}

	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		obj.put("waiting", _state == State.WAITING);
		obj.put("owner", _owner.getNickname());
		if (_player != null)
			obj.put("player", _player.getNickname());

		return obj;
	}
}
