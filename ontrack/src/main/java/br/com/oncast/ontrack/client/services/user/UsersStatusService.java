package br.com.oncast.ontrack.client.services.user;

import br.com.oncast.ontrack.client.services.user.UsersStatusServiceImpl.UsersStatusChangeListener;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;

import java.util.SortedSet;

import com.google.gwt.event.shared.HandlerRegistration;

public interface UsersStatusService {

	HandlerRegistration register(UsersStatusChangeListener usersStatusChangeListener);

	SortedSet<UserRepresentation> getActiveUsers();

	SortedSet<UserRepresentation> getOnlineUsers();

	UserStatus getStatus(UserRepresentation userRepresentation);

	HandlerRegistration registerListenerForSpecificUser(UserRepresentation user, UserSpecificStatusChangeListener listener);

}
