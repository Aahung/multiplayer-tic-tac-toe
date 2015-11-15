/*
	User class
	EE 4216 Group 4
*/


package ee4216;

import org.json.simple.*;

public class TTTUser {
	private String _nickname;
	private UserType _userType;
	private String _imageURL;
	
	public enum UserType {
		WALKIN, FACEBOOK
	}

	public void setImageURL(String imageURL) {
		_imageURL = imageURL;
	}

	public TTTUser(String nickname) {
		_nickname = nickname;
		_userType = UserType.WALKIN;
		// TODO: setup image placeholder
	}

	public TTTUser(String nickname, String imageURL) {
		_nickname = nickname;
		_userType = UserType.FACEBOOK;
		_imageURL = imageURL;
	}

	public JSONObject toJSONObject() {
		JSONObject obj = new JSONObject();
		
		obj.put("nickname", _nickname);
		obj.put("type", String.format("%s", _userType));
		obj.put("image", _imageURL);

		return obj;
	}

	public String getNickname() {
		return _nickname;
	}
}
