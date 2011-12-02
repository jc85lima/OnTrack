package br.com.oncast.ontrack.client.ui.places.login;

import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.client.services.authentication.UserAuthenticationCallback;
import br.com.oncast.ontrack.shared.model.user.User;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class LoginActivity extends AbstractActivity implements LoginView.Presenter {

	private static final ClientServiceProvider SERVICE_PROVIDER = ClientServiceProvider.getInstance();
	private final UserAuthenticationCallback authenticationCallback;
	private final LoginView view;

	public LoginActivity(final Place destinationPlace) {
		this.view = new LoginPanel(this);

		this.authenticationCallback = new UserAuthenticationCallback() {

			@Override
			public void onUserAuthenticatedSuccessfully(final User user) {
				view.enable();
				SERVICE_PROVIDER.getApplicationPlaceController().goTo(destinationPlace);
			}

			@Override
			public void onUnexpectedFailure(final Throwable caught) {
				// TODO Improve feedback message.
				view.enable();
				view.setErrorMessage("Unexpected error.");
			}

			@Override
			public void onIncorrectCredentialsFailure() {
				// TODO Improve feedback message.
				view.enable();
				view.setErrorMessage("Incorrect user or password.");
			}
		};
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		panel.setWidget(view.asWidget());
	}

	@Override
	public void onStop() {}

	@Override
	public void onAuthenticationRequest(final String username, final String password) {
		if (!isValidEmail(username)) {
			view.setErrorMessage("Please provide a valid e-mail.");
			return;
		}

		view.disable();
		SERVICE_PROVIDER.getAuthenticationService().authenticate(username, password, authenticationCallback);
	}

	private boolean isValidEmail(final String email) {
		if (email.trim().equals("")) return false;
		return true;
	}
}
