package br.com.oncast.ontrack.client.services.authentication;

public interface AuthenticationService {

	void authenticate(String login, String password, UserAuthenticationCallback callback);

	void logout(UserLogoutCallback dispatchCallback);

	void changePassword(String oldPassword, String newPassword, UserPasswordChangeCallback callback);
}
