package br.com.oncast.ontrack.client.ui.components.releasepanel;

import java.util.Set;

import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionListener;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionRequestHandler;
import br.com.oncast.ontrack.client.ui.components.Component;
import br.com.oncast.ontrack.client.ui.components.ComponentInteractionHandler;
import br.com.oncast.ontrack.client.ui.components.releasepanel.interaction.ReleasePanelInteractionHandler;
import br.com.oncast.ontrack.client.ui.components.releasepanel.widgets.ReleasePanelWidget;
import br.com.oncast.ontrack.shared.model.actions.ModelAction;
import br.com.oncast.ontrack.shared.model.actions.ReleaseRemoveAction;
import br.com.oncast.ontrack.shared.model.actions.ReleaseRemoveRollbackAction;
import br.com.oncast.ontrack.shared.model.actions.ReleaseScopeUpdatePriorityAction;
import br.com.oncast.ontrack.shared.model.actions.ReleaseUpdatePriorityAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeBindReleaseAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeDeclareEffortAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeDeclareProgressAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeInsertAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeInsertChildRollbackAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeInsertParentRollbackAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeInsertSiblingDownRollbackAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeInsertSiblingUpRollbackAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeRemoveAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeRemoveRollbackAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeUpdateAction;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.utils.deepEquality.IgnoredByDeepEquality;

import com.google.gwt.user.client.ui.Widget;

public class ReleasePanel implements Component {

	private final ReleasePanelWidget releasePanelWidget;

	@IgnoredByDeepEquality
	private final ActionExecutionListener actionExecutionListener;

	// TODO Review if this should be tested by deepEquality
	@IgnoredByDeepEquality
	private Release rootRelease;

	@IgnoredByDeepEquality
	private ReleasePanelInteractionHandler releasePanelInteractionHandler;

	public ReleasePanel() {
		releasePanelWidget = new ReleasePanelWidget(releasePanelInteractionHandler = new ReleasePanelInteractionHandler());

		actionExecutionListener = new ActionExecutionListener() {
			@Override
			public void onActionExecution(final ModelAction action, final ProjectContext context, final Set<UUID> inferenceInfluencedScopeSet,
					final boolean isUserAction) {

				if (action instanceof ScopeUpdateAction ||
						action instanceof ScopeRemoveAction ||
						action instanceof ScopeInsertAction ||
						action instanceof ScopeInsertParentRollbackAction ||
						action instanceof ScopeInsertChildRollbackAction ||
						action instanceof ScopeInsertSiblingUpRollbackAction ||
						action instanceof ScopeInsertSiblingDownRollbackAction ||
						action instanceof ScopeRemoveRollbackAction ||
						action instanceof ScopeDeclareProgressAction ||
						action instanceof ScopeDeclareEffortAction ||
						action instanceof ScopeBindReleaseAction ||
						action instanceof ReleaseRemoveAction ||
						action instanceof ReleaseRemoveRollbackAction ||
						action instanceof ReleaseUpdatePriorityAction ||
						action instanceof ReleaseScopeUpdatePriorityAction) update();
			}
		};
	}

	protected void update() {
		releasePanelWidget.update();
	}

	public void setRelease(final Release release) {
		this.rootRelease = release;

		releasePanelInteractionHandler.setRootRelease(rootRelease);
		releasePanelWidget.setRelease(rootRelease);
	}

	@Override
	public ActionExecutionListener getActionExecutionListener() {
		return actionExecutionListener;
	}

	public void setComponentInteractionHandler(final ComponentInteractionHandler componentInteractionHandler) {
		releasePanelInteractionHandler.configureComponentInteractionHandler(componentInteractionHandler);
	}

	@Override
	public void setActionExecutionRequestHandler(final ActionExecutionRequestHandler actionHandler) {
		releasePanelInteractionHandler.configureActionExecutionRequestHandler(actionHandler);
	}

	@Override
	public Widget asWidget() {
		return releasePanelWidget;
	}

	@Override
	public int hashCode() {
		return rootRelease.hashCode();
	}
}
