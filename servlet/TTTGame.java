/*
	Game class
	EE 4216 Group 4
*/


package ee4216;

import java.util.*;
import org.json.simple.*;

public class TTTGame {
	// _dots save the states of the game
	// _dots = 1 means owner
	//       = -1 means player

	// 2d to 1d table
	// _dots[0] [0-2]: row1: 0 1 2
	// _dots[1] [0-2]: row2: 3 4 5
	// _dots[2] [0-2]: row3: 6 7 8
	private int[][] _dots = new int[3][3];
	static int OWNER_MARK = 1;
	static int PLAYER_MARK = -1;

	private int turn = OWNER_MARK; // owner first


	private TTTCallback _onGameChangeListener;

	public TTTGame() {
		for (int i = 0; i < 3; ++i)
			for (int j = 0; j < 3; ++j)
				_dots[i][j] = 0;
	}

	public void setOnGameChangeListener(final TTTCallback onGameChangeListener) {
		_onGameChangeListener = onGameChangeListener;
		// call to initialize canvas
		if (_onGameChangeListener != null)
			_onGameChangeListener.call(this);
	}

	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		JSONArray ownerJSONArray = new JSONArray(), playerJSONArray = new JSONArray();
		for (int i: getOwnerDots()) ownerJSONArray.add(i);
		for (int i: getPlayerDots()) playerJSONArray.add(i);
		obj.put("owner", ownerJSONArray);
		obj.put("player", playerJSONArray);
		obj.put("result", checkResult());

		return obj;
	}

	// return 1d array of 0-8 index dots
	private int[] getDots(int mark) {
		int count = 0;
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				if (_dots[i][j] == mark) count++;
			}
		}
		int[] dots = new int[count];
		int index = 0;
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 3; ++j) {
				if (_dots[i][j] == mark) {
					dots[index++] = 3 * i + j;
				}
			}
		}
		return dots;
	}

	public int[] getOwnerDots() {
		return getDots(OWNER_MARK);
	}

	public int[] getPlayerDots() {
		return getDots(PLAYER_MARK);
	}

	static int[][] lines = new int[][] {
		// row line
		{0, 1, 2},
		{3, 4, 5},
		{6, 7, 8},
		// column line
		{0, 3, 6},
		{1, 4, 7},
		{2, 5, 8},
		// cross line
		{0, 4, 8},
		{2, 4, 6}
	}; 

	private boolean checkResult(int mark) {
		int[] dots = getDots(mark);
		for (int i = 0; i < 8; ++i) {
			int i_dots = 0;
			int i_lines = 0;
			while (i_lines < 3 && i_dots < dots.length) {
				if (dots[i_dots] == lines[i][i_lines]) i_lines++;
				i_dots++;
			}
			if (i_lines == 3) {
				return true;
			}
		}
		return false;
	}

	public int checkResult() {
		if (checkResult(OWNER_MARK)) return OWNER_MARK;
		else if (checkResult(PLAYER_MARK)) return PLAYER_MARK;
		else return 0;
	}

	private boolean move(int mark, int dotIndex) {
		int i = dotIndex / 3;
		int j = dotIndex % 3;
		if (_dots[i][j] != 0 || turn != mark || checkResult() != 0) return false;
		_dots[i][j] = mark;
		if (_onGameChangeListener != null)
			_onGameChangeListener.call(this);
		turn = -turn;
		return true;
	}

	// return true if successfully do
	public boolean ownerMove(int dotIndex) {
		return move(OWNER_MARK, dotIndex);
	}

	public boolean playerMove(int dotIndex) {
		return move(PLAYER_MARK, dotIndex);
	}
}
