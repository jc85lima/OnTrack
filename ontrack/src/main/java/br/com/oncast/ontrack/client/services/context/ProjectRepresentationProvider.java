package br.com.oncast.ontrack.client.services.context;

import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.event.shared.HandlerRegistration;

public interface ProjectRepresentationProvider {

	ProjectRepresentation getCurrent();

	HandlerRegistration registerProjectListChangeListener(final ProjectListChangeListener projectListChangeListener);

	void createNewProject(final String projectName, final ProjectCreationListener projectCreationListener);

	void unregisterProjectListChangeListener(ProjectListChangeListener projectListChangeListener);

	void authorizeUser(String mail, ProjectAuthorizationCallback callback);

	ProjectRepresentation getProjectRepresentation(UUID projectReference);

}