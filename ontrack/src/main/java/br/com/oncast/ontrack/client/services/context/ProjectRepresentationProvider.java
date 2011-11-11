package br.com.oncast.ontrack.client.services.context;

import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;

public class ProjectRepresentationProvider {

	ProjectRepresentation projectRepresentation;

	public ProjectRepresentation getCurrentProjectRepresentation() {
		// FIXME Analize if it is better to throw a new exception or to create a representation of a non existing / non persisted project (id = 0).
		if (projectRepresentation == null) return new ProjectRepresentation(0);
		return projectRepresentation;
	}

	public void setProjectRepresentation(final ProjectRepresentation projectRepresentation) {
		this.projectRepresentation = projectRepresentation;
	}
}
