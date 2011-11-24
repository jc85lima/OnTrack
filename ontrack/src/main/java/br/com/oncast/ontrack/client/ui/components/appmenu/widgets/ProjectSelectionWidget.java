package br.com.oncast.ontrack.client.ui.components.appmenu.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.client.services.context.ProjectListChangeListener;
import br.com.oncast.ontrack.client.ui.generalwidgets.CommandMenuItem;
import br.com.oncast.ontrack.client.ui.generalwidgets.CustomCommandMenuItemFactory;
import br.com.oncast.ontrack.client.ui.generalwidgets.FiltrableCommandMenu;
import br.com.oncast.ontrack.client.ui.generalwidgets.PopupConfig.PopupAware;
import br.com.oncast.ontrack.client.ui.places.planning.PlanningPlace;
import br.com.oncast.ontrack.client.ui.places.projectCreation.ProjectCreationPlace;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

// FIXME Rodrigo: Make the pop up behavior optional.
public class ProjectSelectionWidget extends Composite implements HasCloseHandlers<ProjectSelectionWidget>, PopupAware {

	private static final ClientServiceProvider SERVICE_PROVIDER = ClientServiceProvider.getInstance();

	private static ProjectSelectionWidgetUiBinder uiBinder = GWT.create(ProjectSelectionWidgetUiBinder.class);

	interface ProjectSelectionWidgetUiBinder extends UiBinder<Widget, ProjectSelectionWidget> {}

	@UiField
	protected FiltrableCommandMenu projectSwitchingMenu;

	private final ProjectListChangeListener projectListChangeListener;

	private final boolean isPopUp;

	@UiFactory
	protected FiltrableCommandMenu createProjectSwitchCommandMenu() {
		return new FiltrableCommandMenu(new CustomCommandMenuItemFactory() {

			@Override
			public CommandMenuItem createCustomItem(final String inputText) {
				return new CommandMenuItem("Create new project '" + inputText + "'", new Command() {

					@Override
					public void execute() {
						createNewProject(inputText);
					}
				});
			}
		}, 700, 400, isPopUp);
	}

	// FIXME Rodrigo: Remove this argument; It is only used to configure FiltrableCommandMenu and should be removed from there.
	public ProjectSelectionWidget(final boolean isPopUp) {
		this.isPopUp = isPopUp;
		initWidget(uiBinder.createAndBindUi(this));

		this.projectListChangeListener = new ProjectListChangeListener() {

			@Override
			public void onProjectListChanged(final Set<ProjectRepresentation> projectRepresentations) {
				updateProjectMenuItens(projectRepresentations);
			}
		};
		registerProjectListChangeListener();
		registerCloseHandler();
	}

	private void registerCloseHandler() {
		projectSwitchingMenu.addCloseHandler(new CloseHandler<FiltrableCommandMenu>() {
			@Override
			public void onClose(final CloseEvent<FiltrableCommandMenu> event) {
				hide();
			}
		});
	}

	private void updateProjectMenuItens(final Set<ProjectRepresentation> projectRepresentations) {
		projectSwitchingMenu.setItems(buildUpdateProjectCommandMenuItemList(projectRepresentations));
		projectSwitchingMenu.focus();
	}

	private List<CommandMenuItem> buildUpdateProjectCommandMenuItemList(final Set<ProjectRepresentation> projectRepresentations) {
		final List<CommandMenuItem> projects = new ArrayList<CommandMenuItem>();

		for (final ProjectRepresentation representation : projectRepresentations)
			projects.add(createProjectMenuItem(representation));

		return projects;
	}

	private CommandMenuItem createProjectMenuItem(final ProjectRepresentation projectRepresentation) {
		return new CommandMenuItem(projectRepresentation.getName(), new Command() {

			@Override
			public void execute() {
				openProject(projectRepresentation);
			}
		});
	}

	private void openProject(final ProjectRepresentation projectRepresentation) {
		final PlanningPlace projectPlanningPlace = new PlanningPlace(projectRepresentation);
		SERVICE_PROVIDER.getApplicationPlaceController().goTo(projectPlanningPlace);
	}

	private void createNewProject(final String inputText) {
		final ProjectCreationPlace projectCreationPlace = new ProjectCreationPlace(inputText);
		SERVICE_PROVIDER.getApplicationPlaceController().goTo(projectCreationPlace);
	}

	@UiHandler("projectSwitchingMenu")
	protected void onAttachOrDetach(final AttachEvent event) {
		if (event.isAttached()) registerProjectListChangeListener();
		else unregisterProjectListChangeListener();
	}

	private void registerProjectListChangeListener() {
		SERVICE_PROVIDER.getProjectRepresentationProvider().registerProjectListChangeListener(projectListChangeListener);
	}

	private void unregisterProjectListChangeListener() {
		SERVICE_PROVIDER.getProjectRepresentationProvider().unregisterProjectListChangeListener(projectListChangeListener);
	}

	@Override
	public HandlerRegistration addCloseHandler(final CloseHandler<ProjectSelectionWidget> handler) {
		return addHandler(handler, CloseEvent.getType());
	}

	@Override
	public void show() {
		projectSwitchingMenu.show();
	}

	@Override
	public void hide() {
		projectSwitchingMenu.hide();
		CloseEvent.fire(this, this);
	}

	public void focus() {
		projectSwitchingMenu.focus();
	}
}
