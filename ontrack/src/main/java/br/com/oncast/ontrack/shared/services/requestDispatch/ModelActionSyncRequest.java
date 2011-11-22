package br.com.oncast.ontrack.shared.services.requestDispatch;

import java.io.Serializable;
import java.util.List;

import br.com.oncast.ontrack.shared.model.actions.ModelAction;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

public class ModelActionSyncRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private UUID clientId;

	private List<ModelAction> actionList;

	private long projectId;

	// IMPORTANT A package-visible default constructor is necessary for serialization. Do not remove this.
	protected ModelActionSyncRequest() {}

	public ModelActionSyncRequest(final UUID clientId, final ProjectRepresentation projectRepresentation, final List<ModelAction> actionList) {
		this.projectId = projectRepresentation.getId();
		this.clientId = clientId;
		this.actionList = actionList;
	}

	public UUID getClientId() {
		return clientId;
	}

	public List<ModelAction> getActionList() {
		return actionList;
	}

	public long getProjectId() {
		return projectId;
	}
}
