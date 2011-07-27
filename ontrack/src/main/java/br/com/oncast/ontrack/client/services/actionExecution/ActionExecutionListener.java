package br.com.oncast.ontrack.client.services.actionExecution;

import java.util.Set;

import br.com.oncast.ontrack.shared.model.actions.ModelAction;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

public interface ActionExecutionListener {

	// TODO +Verify the removal of the context from the methods signature.
	void onActionExecution(ModelAction action, ProjectContext context, Set<UUID> inferenceInfluencedScopeSet);

}
