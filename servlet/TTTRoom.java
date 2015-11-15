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
	private TTTCallback _onRoomStateChangeListener, _onGameChangeListener;

	public TTTRoom(TTTUser owner) {
		_state = State.WAITING;
		_owner = owner;
		_player = null;
		_game = null;
	}

	public void setOnRoomStateChangeListener(final TTTCallback1P<TTTRoom> onRoomStateChangeListener) {
		final TTTRoom _this = this;
		_onRoomStateChangeListener = new TTTCallback() {
			@Override public void call(Object sender) {
				onRoomStateChangeListener.call(sender, _this);
			}
		};
	}

	public void setOnGameChangeListener(final TTTCallback1P<TTTRoom> onGameChangeListener) {
		final TTTRoom _this = this;
		_onGameChangeListener = new TTTCallback() {
			@Override public void call(Object sender) {
				if (_game.checkResult() != 0) {
					// game states change
					if (_onRoomStateChangeListener != null)
						_onRoomStateChangeListener.call(_this);
				}
				onGameChangeListener.call(sender, _this);
			}
		};
	}

	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		obj.put("waiting", _state == State.WAITING);
		obj.put("owner", _owner.toJSONObject());
		if (_player != null)
			obj.put("player", _player.toJSONObject());

		return obj;
	}

	public TTTGame getGame() {
		return _game;
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

	public boolean move(TTTUser mover, int dotIndex) {
		if (getOwner() == mover) {
			return _game.ownerMove(dotIndex);
		} else if (getPlayer() == mover) {
			return _game.playerMove(dotIndex);
		} else {
			return false;
		}
	}

	public boolean join(TTTUser user) {
		if (_player == null) {
			_player = user;
			_state = State.PLAYING;
			if (_onRoomStateChangeListener != null)
				_onRoomStateChangeListener.call(this);
			// let play!
			_game = new TTTGame();
			_game.setOnGameChangeListener(_onGameChangeListener);
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
			_onRoomStateChangeListener.call(this);
		_game.reset();
	}
}
