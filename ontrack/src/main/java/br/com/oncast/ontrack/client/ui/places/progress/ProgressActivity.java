package br.com.oncast.ontrack.client.ui.places.progress;

import java.util.ArrayList;
import java.util.List;

import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.client.ui.components.appmenu.ApplicationMenuShortcutMapping;
import br.com.oncast.ontrack.client.ui.components.appmenu.widgets.ApplicationMenuItem;
import br.com.oncast.ontrack.client.ui.components.appmenu.widgets.ReleaseSelectionWidget;
import br.com.oncast.ontrack.client.ui.components.progresspanel.KanbanActionSyncController;
import br.com.oncast.ontrack.client.ui.components.progresspanel.KanbanActionSyncController.Display;
import br.com.oncast.ontrack.client.ui.keyeventhandler.ShortcutService;
import br.com.oncast.ontrack.client.ui.places.UndoRedoShortCutMapping;
import br.com.oncast.ontrack.client.ui.places.planning.PlanningPlace;
import br.com.oncast.ontrack.shared.model.kanban.Kanban;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.release.exceptions.ReleaseNotFoundException;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.RootPanel;

public class ProgressActivity extends AbstractActivity {

	private static final ClientServiceProvider SERVICE_PROVIDER = ClientServiceProvider.getInstance();

	private KanbanActionSyncController kanbanActionSyncController;
	private ProgressView view;
	private ProjectContext projectContext;
	private Release release;

	private final List<HandlerRegistration> registrations;

	private final UUID requestedProjectId;
	private final UUID requestedReleaseId;

	public ProgressActivity(final ProgressPlace place) {
		requestedProjectId = place.getRequestedProjectId();
		requestedReleaseId = place.getRequestedReleaseId();

		registrations = new ArrayList<HandlerRegistration>();
		ClientServiceProvider.getInstance().getClientMetricService().onBrowserLoadStart();

		try {
			projectContext = SERVICE_PROVIDER.getContextProviderService().getProjectContext(requestedProjectId);
			release = projectContext.findRelease(requestedReleaseId);
			final Kanban kanban = projectContext.getKanban(release);
			view = new ProgressPanel(release, kanban);
			view.getKanbanPanel().setActionExecutionService(SERVICE_PROVIDER.getActionExecutionService());

			kanbanActionSyncController = new KanbanActionSyncController(SERVICE_PROVIDER.getActionExecutionService(), release, new Display() {

				@Override
				public void update() {
					updateViewData();
				}

				@Override
				public void exit() {
					exitToPlanningPlace();
				}

				@Override
				public void updateReleaseInfo() {
					updateCustomApplicationMenus();
				}
			}, ClientServiceProvider.getInstance().getClientErrorMessages());
		}
		catch (final ReleaseNotFoundException e) {

			ClientServiceProvider.getInstance().getClientMetricService().onBrowserLoadEnd();
			exitToPlanningPlace();
		}
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		if (view == null) throw new RuntimeException("The view wasnt initialized correctly.");

		view.getApplicationMenu().setProjectName(projectContext.getProjectRepresentation().getName());
		view.getApplicationMenu().setBackButtonVisibility(true);

		updateViewData();
		updateCustomApplicationMenus();

		panel.setWidget(view);

		kanbanActionSyncController.registerActionExecutionListener();
		view.registerActionExecutionHandler(SERVICE_PROVIDER.getActionExecutionService());

		registrations.add(ShortcutService.register(RootPanel.get(), view.getApplicationMenu(), ApplicationMenuShortcutMapping.values()));
		registrations.add(ShortcutService.register(RootPanel.get(), SERVICE_PROVIDER.getActionExecutionService(), UndoRedoShortCutMapping.values()));
		SERVICE_PROVIDER.getClientAlertingService().setAlertingParentWidget(view.getAlertingPanel());

		ClientServiceProvider.getInstance().getClientMetricService().onBrowserLoadEnd();
	}

	@Override
	public void onStop() {
		view.unregisterActionExecutionHandler(SERVICE_PROVIDER.getActionExecutionService());
		kanbanActionSyncController.unregisterActionExecutionListener();
		for (final HandlerRegistration registration : registrations) {
			registration.removeHandler();
		}
		SERVICE_PROVIDER.getClientAlertingService().clearAlertingParentWidget();
	}

	protected void updateViewData() {
		view.getKanbanPanel().update(projectContext.getKanban(release));
	}

	private void exitToPlanningPlace() {
		final UUID projectId = SERVICE_PROVIDER.getProjectRepresentationProvider().getCurrent().getId();
		SERVICE_PROVIDER.getApplicationPlaceController().goTo(new PlanningPlace(projectId));
	}

	private void updateCustomApplicationMenus() {
		view.getApplicationMenu().clearCustomMenuItems().addCustomMenuItem(createReleaseMenuItem(), new ReleaseSelectionWidget());
	}

	private ApplicationMenuItem createReleaseMenuItem() {
		return new ApplicationMenuItem(release.getFullDescription(), true);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final ProgressActivity other = (ProgressActivity) obj;
		if (requestedProjectId == null) {
			if (other.requestedProjectId != null) return false;
		}
		else if (!requestedProjectId.equals(other.requestedProjectId)) return false;
		if (requestedReleaseId == null) {
			if (other.requestedReleaseId != null) return false;
		}
		else if (!requestedReleaseId.equals(other.requestedReleaseId)) return false;
		return true;
	}

}