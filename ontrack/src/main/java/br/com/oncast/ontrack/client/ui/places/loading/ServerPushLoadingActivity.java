package br.com.oncast.ontrack.client.ui.places.loading;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.services.alerting.AlertConfirmationListener;
import br.com.oncast.ontrack.client.ui.generalwidgets.ProjectMessagePanel;
import br.com.oncast.ontrack.client.ui.generalwidgets.ProjectMessageView;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ServerPushLoadingActivity extends AbstractActivity {
	private static final ClientServices SERVICE_PROVIDER = ClientServices.get();

	private final ServerPushLoadingMessages messages = GWT.create(ServerPushLoadingMessages.class);

	private Place destinationPlace;

	protected ServerPushLoadingActivity() {}

	public ServerPushLoadingActivity(final Place destinationPlace) {
		this.destinationPlace = destinationPlace;
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		validateConnection();

		final ProjectMessageView view = new ProjectMessagePanel();
		panel.setWidget(view);
		view.setMainMessage(messages.establishingConnection());

		SERVICE_PROVIDER.serverPush().onConnected(new ServerPushConnectionCallback() {
			@Override
			public void connected() {
				validateConnection();
				SERVICE_PROVIDER.getServerPushClientService().removeConnectionListener(this);
			}

			@Override
			public void uncaughtExeption(final Throwable cause) {
				treatUnexpectedFailure(cause, messages.couldNotConnectToServer());
			}
		});
		SERVICE_PROVIDER.alerting().setAlertingParentWidget(view.asWidget());
	}

	@Override
	public void onStop() {
		SERVICE_PROVIDER.alerting().clearAlertingParentWidget();
	}

	private void validateConnection() {
		if (!SERVICE_PROVIDER.serverPush().isConnected()) return;
		SERVICE_PROVIDER.placeController().goTo(destinationPlace);
	}

	private void treatUnexpectedFailure(final Throwable cause, final String message) {
		// TODO +++Treat communication failure.
		cause.printStackTrace();
		SERVICE_PROVIDER.alerting().showErrorWithConfirmation(message, new AlertConfirmationListener() {
			@Override
			public void onConfirmation() {
				Window.Location.reload();
			}
		});
	}

}
