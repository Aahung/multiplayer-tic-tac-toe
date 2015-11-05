/*
	Room class
	EE 4216 Group 4
*/


package ee4216;

import java.util.*;
import org.json.simple.*;
import ee4216.*;

public class TTTRoom {
	public enum State {
		WAITING, PLAYING
	}

	private State _state;
	private TTTUser _owner, _player;
	private TTTGame _game;
	private TTTCallback1P<TTTRoom> _onRoomStateChangeListener;

	public TTTRoom(TTTUser owner) {
		_state = State.WAITING;
		_owner = owner;
		_player = null;
		_game = null;
	}

	public void setOnRoomStateChangeListener(final TTTCallback1P<TTTRoom> onRoomStateChangeListener) {
		_onRoomStateChangeListener = onRoomStateChangeListener;
	}

	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		obj.put("waiting", _state == State.WAITING);
		obj.put("owner", _owner.getNickname());
		if (_player != null)
			obj.put("player", _player.getNickname());

		return obj;
	}

	public TTTUser getOwner() {
		return _owner;
	}

	public TTTUser getPlayer() {
		return _player;
	}

	public boolean isWaiting() {
		return _state == State.WAITING;
	}

	public boolean join(TTTUser user) {
		if (_player == null) {
			_player = user;
			_state = State.PLAYING;
			if (_onRoomStateChangeListener != null)
				_onRoomStateChangeListener.call(this, this);
			return true;
		}
		return false;
	}

	public void escape(TTTUser user) {
		if (_state == State.WAITING) return;
		if (_owner == user) {
			_owner = _player;
			_player = null;
			_state = State.WAITING;
		} else if (_player == user) {
			_player = null;
			_state = State.WAITING;
		}
		if (_onRoomStateChangeListener != null)
			_onRoomStateChangeListener.call(this, this);
	}
}
