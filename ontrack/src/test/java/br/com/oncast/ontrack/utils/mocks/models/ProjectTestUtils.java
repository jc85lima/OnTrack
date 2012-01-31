package br.com.oncast.ontrack.utils.mocks.models;

import java.util.ArrayList;
import java.util.List;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.ProjectAuthorization;
import br.com.oncast.ontrack.shared.model.project.Project;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

public class ProjectTestUtils {

	public static final String DEFAULT_PROJECT_NAME = "Default project";

	public static Project createProject() {
		return createProject(getDefaultRepresentation(), getDefaultScope(), getDefaultRelease());
	}

	public static Project createProject(final Scope scope, final Release release) {
		return createProject(getDefaultRepresentation(), scope, release);
	}

	public static Project createProject(final ProjectRepresentation projectRepresentation, final Scope scope, final Release release) {
		final Project project = new Project(projectRepresentation, scope, release);
		return project;
	}

	public static ProjectContext createProjectContext() {
		return new ProjectContext(createProject());
	}

	public static ProjectContext createProjectContext(final Scope scope, final Release release) {
		return new ProjectContext(createProject(scope, release));
	}

	public static ProjectRepresentation createRepresentation() {
		return getDefaultRepresentation();
	}

	public static ProjectRepresentation createRepresentation(final long projectId) {
		return createRepresentation(projectId, DEFAULT_PROJECT_NAME);
	}

	public static ProjectRepresentation createRepresentation(final long projectId, final String projectName) {
		return new ProjectRepresentation(projectId, projectName);
	}

	public static ProjectRepresentation createRepresentation(final String projectName) {
		return new ProjectRepresentation(projectName);
	}

	private static ProjectRepresentation getDefaultRepresentation() {
		return createRepresentation(1);
	}

	private static Scope getDefaultScope() {
		return new Scope(getDefaultRepresentation().getName(), new UUID("0"));
	}

	private static Release getDefaultRelease() {
		return new Release(getDefaultRepresentation().getName(), new UUID("release0"));
	}

	public static List<ProjectRepresentation> createRepresentationList(final int size) {
		final List<ProjectRepresentation> list = new ArrayList<ProjectRepresentation>();
		return list;
	}

	public static ProjectAuthorization createAuthorization() {
		return new ProjectAuthorization(UserTestUtils.createUser(), createRepresentation());
	}

	public static List<ProjectAuthorization> createAuthorizations(final int numberOfAuthorizations) {
		final List<ProjectAuthorization> auths = new ArrayList<ProjectAuthorization>();
		for (int i = 0; i < numberOfAuthorizations; i++) {
			auths.add(createAuthorization());
		}
		return auths;
	}

}