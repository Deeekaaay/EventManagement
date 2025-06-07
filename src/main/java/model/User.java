package model;

public class User {
	private String username;
	private String password;
	private String preferredName;
	private String role;
	private int userId;

	public User() {
	}

	public User(String username, String password, String preferredName, String role) {
		this.username = username;
		this.password = password;
		this.preferredName = preferredName;
		this.role = role;
	}

	public String getPreferredName() {
		return preferredName;
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
}
