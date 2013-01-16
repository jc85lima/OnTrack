package br.com.oncast.ontrack.client.services.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import br.com.drycode.api.web.gwt.dispatchService.client.DispatchCallback;
import br.com.drycode.api.web.gwt.dispatchService.client.DispatchService;
import br.com.drycode.api.web.gwt.dispatchService.shared.responses.VoidResult;
import br.com.oncast.ontrack.client.services.context.ContextProviderService;
import br.com.oncast.ontrack.client.services.serverPush.ServerPushClientService;
import br.com.oncast.ontrack.client.services.serverPush.ServerPushEventHandler;
import br.com.oncast.ontrack.client.ui.events.ScopeAddMemberSelectionEvent;
import br.com.oncast.ontrack.client.ui.events.ScopeRemoveMemberSelectionEvent;
import br.com.oncast.ontrack.client.ui.events.ScopeSelectionEvent;
import br.com.oncast.ontrack.client.ui.events.ScopeSelectionEventHandler;
import br.com.oncast.ontrack.shared.model.ModelBeanNotFoundException;
import br.com.oncast.ontrack.shared.model.color.Color;
import br.com.oncast.ontrack.shared.model.color.ColorPack;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;
import br.com.oncast.ontrack.shared.model.user.exceptions.UserNotFoundException;
import br.com.oncast.ontrack.shared.services.requestDispatch.UserScopeSelectionMulticastRequest;
import br.com.oncast.ontrack.shared.services.user.UserClosedProjectEvent;
import br.com.oncast.ontrack.shared.services.user.UserSelectedScopeEvent;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.event.shared.EventBus;

public class ColorProviderServiceImpl implements ColorProviderService {

	private static final double SCOPE_COLOR_ALPHA = 0.4;
	private HashMap<UserRepresentation, Scope> userSelectionMap;
	private HashMap<UserRepresentation, Color> userColorMap;
	private HashMap<Scope, Color> scopeColorMap;

	private final ColorPicker colorPicker;
	private final EventBus eventBus;
	private final ColorPackPicker colorPackPicker;

	public ColorProviderServiceImpl(final DispatchService requestDispatchService, final ContextProviderService contextProviderService,
			final ServerPushClientService serverPushClientService, final EventBus eventBus, final UsersStatusService usersStatusServiceImpl,
			final ColorPicker colorPicker, final ColorPackPicker colorPackPicker) {
		this.colorPicker = colorPicker;
		this.colorPackPicker = colorPackPicker;
		this.eventBus = eventBus;
		userSelectionMap = new HashMap<UserRepresentation, Scope>();
		userColorMap = new HashMap<UserRepresentation, Color>();
		scopeColorMap = new HashMap<Scope, Color>();

		eventBus.addHandler(ScopeSelectionEvent.getType(), new ScopeSelectionEventHandler() {
			private Scope selectedScope;

			@Override
			public void onScopeSelectionRequest(final ScopeSelectionEvent event) {
				if (selectedScope != null && selectedScope.equals(event.getTargetScope())) return;

				selectedScope = event.getTargetScope();
				notifyScopeSelectionToServer();
			}

			private void notifyScopeSelectionToServer() {
				requestDispatchService.dispatch(new UserScopeSelectionMulticastRequest(selectedScope.getId()), new DispatchCallback<VoidResult>() {
					@Override
					public void onSuccess(final VoidResult result) {}

					@Override
					public void onTreatedFailure(final Throwable caught) {}

					@Override
					public void onUntreatedFailure(final Throwable caught) {}
				});
			}
		});

		serverPushClientService.registerServerEventHandler(UserSelectedScopeEvent.class, new ServerPushEventHandler<UserSelectedScopeEvent>() {
			@Override
			public void onEvent(final UserSelectedScopeEvent event) {
				try {
					final ProjectContext context = contextProviderService.getCurrent();
					final UserRepresentation member = context.findUser(event.getUserId());

					removePreviousSelection(member);

					final Scope scope = context.findScope(event.getScopeId());
					userSelectionMap.put(member, scope);
					eventBus.fireEvent(new ScopeAddMemberSelectionEvent(member, scope, getSelectionColorFor(member)));
				}
				catch (final ModelBeanNotFoundException e) {
					GWT.log("Error handling UserSelectedScopeEvent!", e);
				}
			}

		});

		serverPushClientService.registerServerEventHandler(UserClosedProjectEvent.class, new ServerPushEventHandler<UserClosedProjectEvent>() {
			@Override
			public void onEvent(final UserClosedProjectEvent event) {
				try {
					final ProjectContext context = contextProviderService.getCurrent();
					final UserRepresentation member = context.findUser(event.getUserId());
					removePreviousSelection(member);
				}
				catch (final UserNotFoundException e) {
					GWT.log("UserClosedProjectEventHandler Failed", e);
				}
			}
		});
	}

	private void removePreviousSelection(final UserRepresentation member) {
		final Scope previousSelection = userSelectionMap.remove(member);
		if (previousSelection != null) {
			eventBus.fireEvent(new ScopeRemoveMemberSelectionEvent(member, previousSelection));
		}
	}

	@Override
	public synchronized Color getSelectionColorFor(final UserRepresentation user) {
		if (!userColorMap.containsKey(user)) userColorMap.put(user, colorPicker.pick());
		return userColorMap.get(user);
	}

	@Override
	public List<Selection> getMembersSelectionsFor(final Scope scope) {
		final List<Selection> selections = new ArrayList<Selection>();
		if (!userSelectionMap.containsValue(scope)) return selections;

		for (final Entry<UserRepresentation, Scope> e : userSelectionMap.entrySet()) {
			if (e.getValue().equals(scope)) {
				final UserRepresentation user = e.getKey();
				selections.add(new Selection(user, getSelectionColorFor(user)));
			}
		}

		return selections;
	}

	@Override
	public Color getColorFor(final Scope scope) {
		if (!scopeColorMap.containsKey(scope)) scopeColorMap.put(scope, colorPicker.pick(SCOPE_COLOR_ALPHA));
		return scopeColorMap.get(scope);
	}

	@Override
	public Color pickColor() {
		return colorPicker.pick();
	}

	public ColorPack pickColorPack() {
		return colorPackPicker.pick();
	}
}
