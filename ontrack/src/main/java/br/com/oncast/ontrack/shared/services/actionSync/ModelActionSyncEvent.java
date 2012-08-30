package br.com.oncast.ontrack.shared.services.actionSync;

import java.util.List;

import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.services.serverPush.ServerPushEvent;

public class ModelActionSyncEvent implements ServerPushEvent {

	private static final long serialVersionUID = 1L;

	private List<ModelAction> actionList;

	private UUID projectId;

	private ActionContext actionContext;

	// IMPORTANT The default constructor is used by GWT and by Mind map converter to construct new scopes. Do not remove this.
	protected ModelActionSyncEvent() {}

	public ModelActionSyncEvent(final UUID projectId, final List<ModelAction> actionList, final ActionContext actionContext) {
		this.actionList = actionList;
		this.actionContext = actionContext;
		this.projectId = projectId;
	}

	public List<ModelAction> getActionList() {
		return actionList;
	}

	public UUID getProjectId() {
		return projectId;
	}

	public ActionContext getActionContext() {
		return actionContext;
	}
}