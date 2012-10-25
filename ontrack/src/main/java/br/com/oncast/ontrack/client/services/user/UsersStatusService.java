package br.com.oncast.ontrack.client.services.user;

import java.util.SortedSet;

import br.com.oncast.ontrack.client.services.user.UsersStatusServiceImpl.UsersStatusChangeListener;
import br.com.oncast.ontrack.shared.model.user.User;

import com.google.gwt.event.shared.HandlerRegistration;

public interface UsersStatusService {

	HandlerRegistration register(UsersStatusChangeListener usersStatusChangeListener);

	SortedSet<User> getActiveUsers();

	SortedSet<User> getOnlineUsers();

}