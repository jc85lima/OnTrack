package br.com.oncast.ontrack.shared.model.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.simpleframework.xml.Element;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.release.ReleaseRemoveActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConversionAlias;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.action.helper.ActionHelper;
import br.com.oncast.ontrack.shared.model.annotation.Annotation;
import br.com.oncast.ontrack.shared.model.checklist.Checklist;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

@ConvertTo(ReleaseRemoveActionEntity.class)
public class ReleaseRemoveAction implements ReleaseAction {

	private static final long serialVersionUID = 1L;

	@ConversionAlias("referenceId")
	@Element
	private UUID referenceId;

	// IMPORTANT A package-visible default constructor is necessary for serialization. Do not remove this.
	protected ReleaseRemoveAction() {}

	public ReleaseRemoveAction(final UUID selectedReleaseId) {
		this.referenceId = selectedReleaseId;
	}

	@Override
	public ReleaseRemoveRollbackAction execute(final ProjectContext context, final ActionContext actionContext) throws UnableToCompleteActionException {
		final Release selectedRelease = ActionHelper.findRelease(referenceId, context);
		if (selectedRelease.isRoot()) throw new UnableToCompleteActionException("Unable to remove root level.");

		final List<ModelAction> subActionRollbackList = new ArrayList<ModelAction>();
		final List<ReleaseRemoveRollbackAction> childActionRollbackList = removeDescendantsReleases(context, actionContext, selectedRelease);

		subActionRollbackList.addAll(dissociateScopesFromThisRelease(context, actionContext, selectedRelease));
		subActionRollbackList.addAll(removeAnnotations(context, actionContext));
		subActionRollbackList.addAll(removeChecklists(context, actionContext));

		final Release parentRelease = selectedRelease.getParent();
		final int index = parentRelease.getChildIndex(selectedRelease);
		parentRelease.removeChild(selectedRelease);

		return new ReleaseRemoveRollbackAction(parentRelease.getId(), referenceId, selectedRelease.getDescription(), index, childActionRollbackList,
				subActionRollbackList);
	}

	private List<ModelAction> removeAnnotations(final ProjectContext context, final ActionContext actionContext) throws UnableToCompleteActionException {
		final List<ModelAction> subActionRollbackList = new ArrayList<ModelAction>();
		for (final Annotation annotation : context.findAnnotationsFor(referenceId)) {
			subActionRollbackList.add(new AnnotationRemoveAction(referenceId, annotation.getId()).execute(context, actionContext));
		}
		return subActionRollbackList;
	}

	private List<ModelAction> removeChecklists(final ProjectContext context, final ActionContext actionContext) throws UnableToCompleteActionException {
		final List<ModelAction> subActionRollbackList = new ArrayList<ModelAction>();
		for (final Checklist checklist : context.findChecklistsFor(referenceId)) {
			subActionRollbackList.add(new ChecklistRemoveAction(referenceId, checklist.getId()).execute(context, actionContext));
		}
		return subActionRollbackList;
	}

	private List<ScopeBindReleaseAction> dissociateScopesFromThisRelease(final ProjectContext context, final ActionContext actionContext, final Release release)
			throws UnableToCompleteActionException {

		final List<ScopeBindReleaseAction> subActionRollbackList = new ArrayList<ScopeBindReleaseAction>();
		for (final Scope scope : release.getScopeList())
			subActionRollbackList.add(new ScopeBindReleaseAction(scope.getId(), null).execute(context, actionContext));

		Collections.reverse(subActionRollbackList);
		return subActionRollbackList;
	}

	private List<ReleaseRemoveRollbackAction> removeDescendantsReleases(final ProjectContext context, final ActionContext actionContext, final Release release)
			throws UnableToCompleteActionException {

		final List<ReleaseRemoveRollbackAction> childActionRollbackList = new ArrayList<ReleaseRemoveRollbackAction>();
		for (final Release child : release.getChildren())
			childActionRollbackList.add(new ReleaseRemoveAction(child.getId()).execute(context, actionContext));

		return childActionRollbackList;
	}

	@Override
	public UUID getReferenceId() {
		return referenceId;
	}
}
