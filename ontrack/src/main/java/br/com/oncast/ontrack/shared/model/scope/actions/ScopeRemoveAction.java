package br.com.oncast.ontrack.shared.model.scope.actions;

import java.util.ArrayList;
import java.util.List;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope.ScopeRemoveActionEntity;
import br.com.oncast.ontrack.server.util.converter.annotations.ConversionAlias;
import br.com.oncast.ontrack.server.util.converter.annotations.ConvertTo;
import br.com.oncast.ontrack.shared.model.actions.ModelAction;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

@ConvertTo(ScopeRemoveActionEntity.class)
public class ScopeRemoveAction implements ScopeAction {

	@ConversionAlias("referenceId")
	private UUID referenceId;

	public ScopeRemoveAction(final UUID selectedScopeId) {
		this.referenceId = selectedScopeId;
	}

	// IMPORTANT A package-visible default constructor is necessary for serialization. Do not remove this.
	protected ScopeRemoveAction() {}

	@Override
	public ScopeRemoveRollbackAction execute(final ProjectContext context) throws UnableToCompleteActionException {
		final Scope selectedScope = context.findScope(referenceId);
		if (selectedScope.isRoot()) throw new UnableToCompleteActionException("Unable to remove root level.");

		final Scope parent = selectedScope.getParent();
		final UUID parentScopeId = parent.getId();
		final String description = selectedScope.getDescription();

		final List<ScopeRemoveRollbackAction> childActionRollbackList = executeChildActions(context, selectedScope);
		final List<ModelAction> subActionRollbackList = executeSubActions(context, selectedScope);

		final int index = parent.getChildIndex(selectedScope);
		parent.remove(selectedScope);

		return new ScopeRemoveRollbackAction(parentScopeId, referenceId, description, index, subActionRollbackList, childActionRollbackList);
	}

	private List<ScopeRemoveRollbackAction> executeChildActions(final ProjectContext context, final Scope selectedScope) throws UnableToCompleteActionException {
		final List<ScopeRemoveRollbackAction> childActionRollbackList = new ArrayList<ScopeRemoveRollbackAction>();
		for (final Scope child : new ArrayList<Scope>(selectedScope.getChildren()))
			childActionRollbackList.add(new ScopeRemoveAction(child.getId()).execute(context));

		return childActionRollbackList;
	}

	private List<ModelAction> executeSubActions(final ProjectContext context, final Scope selectedScope) throws UnableToCompleteActionException {
		final List<ModelAction> subActionList = new ArrayList<ModelAction>();
		subActionList.add(new ScopeProgressAction(referenceId, selectedScope.getProgress().getDescription()));
		subActionList.add(new ScopeBindReleaseAction(referenceId, null));
		subActionList.add(new ScopeDeclareEffortAction(referenceId, false, 0));

		final List<ModelAction> subActionRollbackList = new ArrayList<ModelAction>();
		for (final ModelAction subAction : subActionList) {
			subActionRollbackList.add(subAction.execute(context));
		}
		return subActionRollbackList;
	}

	@Override
	public UUID getReferenceId() {
		return referenceId;
	}

	@Override
	public boolean changesEffortInference() {
		return true;
	}
}