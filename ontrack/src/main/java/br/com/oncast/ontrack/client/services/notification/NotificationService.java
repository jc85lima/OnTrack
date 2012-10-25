package br.com.oncast.ontrack.client.services.notification;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import br.com.drycode.api.web.gwt.dispatchService.client.DispatchCallback;
import br.com.drycode.api.web.gwt.dispatchService.client.DispatchService;
import br.com.oncast.ontrack.client.services.alerting.ClientAlertingService;
import br.com.oncast.ontrack.client.services.authentication.AuthenticationService;
import br.com.oncast.ontrack.client.services.authentication.UserAuthenticationListener;
import br.com.oncast.ontrack.client.services.serverPush.ServerPushClientService;
import br.com.oncast.ontrack.shared.services.notification.Notification;
import br.com.oncast.ontrack.shared.services.notification.NotificationCreatedEvent;
import br.com.oncast.ontrack.shared.services.notification.NotificationCreatedEventHandler;
import br.com.oncast.ontrack.shared.services.requestDispatch.NotificationListRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.NotificationListResponse;
import br.com.oncast.ontrack.shared.services.requestDispatch.NotificationReadStateRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.NotificationReadStateResponse;

public class NotificationService {

	private final DispatchService dispatchService;
	private final ClientAlertingService alertingService;
	private final Set<NotificationListChangeListener> notificationListChangeListeners = new HashSet<NotificationListChangeListener>();
	private final Set<NotificationReadStateChangeListener> notificationReadStateChangeListeners = new HashSet<NotificationReadStateChangeListener>();
	private final List<Notification> availableNotifications = new LinkedList<Notification>();
	private boolean notificationListAvailability;

	public NotificationService(final DispatchService dispatchService, final ServerPushClientService serverPushClientService,
			final AuthenticationService authenticationService, final ClientAlertingService alertingService) {

		this.dispatchService = dispatchService;
		this.alertingService = alertingService;

		authenticationService.registerUserAuthenticationListener(new UserAuthenticationListener() {
			@Override
			public void onUserLoggedIn() {
				updateAvailableNotifications();
			}

			@Override
			public void onUserLoggedOut() {
				availableNotifications.clear();
				notificationListAvailability = false;

				notifyNotificationListContentChange();
				notifyNotificationListAvailabilityChange();
			}

			@Override
			public void onUserInformationLoaded() {
				updateAvailableNotifications();
			}
		});

		serverPushClientService.registerServerEventHandler(NotificationCreatedEvent.class, new NotificationCreatedEventHandler() {

			@Override
			public void onEvent(final NotificationCreatedEvent event) {
				final Notification newNotification = event.getNotification();
				if (availableNotifications.contains(newNotification)) return;
				availableNotifications.add(0, newNotification);

				notifyNotificationListContentChange();
			}
		});

		if (authenticationService.isUserAvailable()) updateAvailableNotifications();
	}

	public void updateNotificationReadState(final Notification notification, final boolean readState, final NotificationReadStateUpdateCallback callback) {
		dispatchService.dispatch(new NotificationReadStateRequest(notification, readState), new DispatchCallback<NotificationReadStateResponse>() {

			@Override
			public void onSuccess(final NotificationReadStateResponse result) {
				callback.notificationReadStateUpdated();
				notifyNotificationReadStateChangeListeners(notification, readState);
			}

			@Override
			public void onTreatedFailure(final Throwable caught) {
				callback.onError();
			}

			@Override
			public void onUntreatedFailure(final Throwable caught) {
				callback.onError();
			}
		});
	}

	private void updateAvailableNotifications() {
		dispatchService.dispatch(new NotificationListRequest(), new DispatchCallback<NotificationListResponse>() {

			@Override
			public void onSuccess(final NotificationListResponse response) {
				availableNotifications.clear();
				availableNotifications.addAll(response.getNotificationList());
				notificationListAvailability = true;

				notifyNotificationListContentChange();
				notifyNotificationListAvailabilityChange();
			}

			@Override
			public void onTreatedFailure(final Throwable caught) {}

			@Override
			public void onUntreatedFailure(final Throwable caught) {
				// TODO +++Treat fatal error. Could not load project list...
				alertingService.showWarning("Notification list unavailable. Verify your connection.");
			}
		});
	}

	private void notifyNotificationListAvailabilityChange() {
		for (final NotificationListChangeListener listener : notificationListChangeListeners)
			notifyListenerForNotificationListAvailabilityChange(listener);
	}

	private void notifyNotificationListContentChange() {
		for (final NotificationListChangeListener listener : notificationListChangeListeners)
			notifyListenerForNotificationListChange(listener);
	}

	private void notifyListenerForNotificationListChange(final NotificationListChangeListener listener) {
		listener.onNotificationListChanged(availableNotifications);
	}

	protected void notifyListenerForNotificationListAvailabilityChange(final NotificationListChangeListener listener) {
		listener.onNotificationListAvailabilityChange(notificationListAvailability);
	}

	public void registerNotificationListChangeListener(final NotificationListChangeListener notificationListChangeListener) {
		if (notificationListChangeListeners.contains(notificationListChangeListener)) return;
		notificationListChangeListeners.add(notificationListChangeListener);
		notifyListenerForNotificationListChange(notificationListChangeListener);
		notifyListenerForNotificationListAvailabilityChange(notificationListChangeListener);
	}

	public void unregisterNotificationListChangeListener(final NotificationListChangeListener notificationListChangeListener) {
		notificationListChangeListeners.remove(notificationListChangeListener);
	}

	public void registerNotificationReadStateChangeListener(final NotificationReadStateChangeListener notificationReadStateChangeListener) {
		notificationReadStateChangeListeners.add(notificationReadStateChangeListener);
	}

	protected void notifyNotificationReadStateChangeListeners(final Notification notification, final boolean readState) {
		for (final NotificationReadStateChangeListener listener : notificationReadStateChangeListeners) {
			listener.readStateChanged(notification, readState);
		}
	}
}