package br.com.oncast.ontrack.shared.model.action;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope.ScopeRemoveRollbackActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConversionAlias;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

@ConvertTo(ScopeRemoveRollbackActionEntity.class)
public class ScopeRemoveRollbackAction implements ScopeInsertAction {

	private static final long serialVersionUID = 1L;

	@ConversionAlias("referenceId")
	@Element
	private UUID referenceId;

	@ConversionAlias("parentScopeId")
	@Element
	private UUID parentScopeId;

	@ConversionAlias("index")
	@Attribute
	private int index;

	@ConversionAlias("description")
	@Attribute
	private String description;

	@ConversionAlias("childActionList")
	@ElementList
	private List<ScopeRemoveRollbackAction> childActionList;

	@ConversionAlias("subActionList")
	@ElementList
	private List<ModelAction> subActionList;

	public ScopeRemoveRollbackAction(final UUID parentScopeId, final UUID selectedScopeId, final String description, final int index,
			final List<ModelAction> subActionList, final List<ScopeRemoveRollbackAction> childActionList) {
		this.parentScopeId = parentScopeId;
		this.referenceId = selectedScopeId;
		this.index = index;
		this.description = description;
		this.subActionList = subActionList;
		this.childActionList = childActionList;
	}

	// IMPORTANT A package-visible default constructor is necessary for serialization. Do not remove this.
	protected ScopeRemoveRollbackAction() {}

	@Override
	public ModelAction execute(final ProjectContext context) throws UnableToCompleteActionException {
		final Scope parent = ScopeActionHelper.findScope(parentScopeId, context);
		final Scope newScope = new Scope(description, referenceId);

		parent.add(index, newScope);

		executSubActions(context);
		executeChildActions(context);

		return new ScopeRemoveAction(referenceId);
	}

	private void executeChildActions(final ProjectContext context) throws UnableToCompleteActionException {
		for (int i = childActionList.size() - 1; i >= 0; i--) {
			childActionList.get(i).execute(context);
		}
	}

	private void executSubActions(final ProjectContext context) throws UnableToCompleteActionException {
		for (final ModelAction subAction : subActionList)
			subAction.execute(context);
	}

	@Override
	public UUID getReferenceId() {
		return parentScopeId;
	}

	@Override
	public UUID getNewScopeId() {
		return referenceId;
	}

	// TODO Result should depend on its subActions.
	@Override
	public boolean changesEffortInference() {
		return true;
	}

	// TODO Result should depend on its subActions.
	@Override
	public boolean changesValueInference() {
		return true;
	}

	// TODO Result should depend on its subActions.
	@Override
	public boolean changesProgressInference() {
		return true;
	}
}
