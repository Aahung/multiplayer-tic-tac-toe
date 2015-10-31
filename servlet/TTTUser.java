/*
	User class
	EE 4216 Group 4
*/


package ee4216;

public class TTTUser {
	private String _nickname;
	private UserType _userType;
	private String _imageURL;
	
	public enum UserType {
		WALKIN, FACEBOOK
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
}
