package br.com.oncast.ontrack.shared.model.actions;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.release.ScopeIncreasePriorityActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConversionAlias;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

@ConvertTo(ScopeIncreasePriorityActionEntity.class)
public class ScopeIncreasePriorityAction implements ReleaseAction {

	private static final long serialVersionUID = 1L;

	@ConversionAlias("releaseReferenceId")
	private UUID releaseReferenceId;

	@ConversionAlias("scopeReferenceId")
	private UUID scopeReferenceId;

	// IMPORTANT A package-visible default constructor is necessary for serialization. Do not remove this.
	protected ScopeIncreasePriorityAction() {}

	public ScopeIncreasePriorityAction(final UUID releaseReferenceId, final UUID scopeReferenceId) {
		this.releaseReferenceId = releaseReferenceId;
		this.scopeReferenceId = scopeReferenceId;
	}

	@Override
	public ModelAction execute(final ProjectContext context) throws UnableToCompleteActionException {
		final Release release = ReleaseActionHelper.findRelease(releaseReferenceId, context);
		final Scope scope = ScopeActionHelper.findScope(scopeReferenceId, context);

		final int index = release.getScopeIndex(scope);
		if (index < 0) throw new UnableToCompleteActionException("The scope priority cannot be updated because it is not part of the referenced release.");
		if (index == 0) throw new UnableToCompleteActionException(
				"The scope priority cannot be increased because it already is the most prioritary in this release.");
		release.removeScope(scope);
		release.addScope(scope, index - 1);

		return new ScopeDecreasePriorityAction(releaseReferenceId, scopeReferenceId);
	}

	@Override
	public UUID getReferenceId() {
		return releaseReferenceId;
	}
}
