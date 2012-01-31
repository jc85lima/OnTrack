package br.com.oncast.ontrack.shared.model.action;

import java.util.List;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope.ScopeInsertChildRollbackActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConversionAlias;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.stringrepresentation.ScopeRepresentationBuilder;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

@ConvertTo(ScopeInsertChildRollbackActionEntity.class)
public class ScopeInsertChildRollbackAction implements ScopeAction {

	private static final long serialVersionUID = 1L;

	@ConversionAlias("referenceId")
	@Element
	private UUID referenceId;

	@ConversionAlias("subActionList")
	@ElementList
	private List<ModelAction> subActionList;

	// IMPORTANT A package-visible default constructor is necessary for serialization. Do not remove this.
	protected ScopeInsertChildRollbackAction() {}

	public ScopeInsertChildRollbackAction(final UUID newScopeId, final List<ModelAction> subActionList) {
		this.referenceId = newScopeId;
		this.subActionList = subActionList;
	}

	@Override
	public ModelAction execute(final ProjectContext context) throws UnableToCompleteActionException {
		final Scope selectedScope = ScopeActionHelper.findScope(referenceId, context);
		if (selectedScope.isRoot()) throw new UnableToCompleteActionException("Unable to remove root level.");

		final Scope parent = selectedScope.getParent();
		final UUID parentScopeId = parent.getId();
		final String pattern = new ScopeRepresentationBuilder(selectedScope).includeEverything().toString();

		executeSubActions(context);
		parent.remove(selectedScope);

		return new ScopeInsertChildAction(parentScopeId, referenceId, pattern);
	}

	private void executeSubActions(final ProjectContext context) throws UnableToCompleteActionException {
		for (final ModelAction subAction : subActionList)
			subAction.execute(context);
	}

	@Override
	public UUID getReferenceId() {
		return referenceId;
	}

	@Override
	public boolean changesEffortInference() {
		return true;
	}

	@Override
	public boolean changesValueInference() {
		return true;
	}

	@Override
	public boolean changesProgressInference() {
		return true;
	}

}