package br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal;

import br.com.oncast.ontrack.client.ui.components.scopetree.ScopeTreeItem;
import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeWidget;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;

public class AddAnnotationInternalAction implements OneStepInternalAction {

	private final ProjectContext context;
	private final Scope scope;
	private ScopeTreeItem selectedTreeItem;

	public AddAnnotationInternalAction(final Scope scope, final ProjectContext context) {
		this.context = context;
		this.scope = scope;
	}

	@Override
	public void execute(final ScopeTreeWidget tree) throws UnableToCompleteActionException {
		selectedTreeItem = InternalActionHelper.findScopeTreeItem(tree, scope);
		selectedTreeItem.getScopeTreeItemWidget().showQuickAddAnnotationMenu(context.findAnnotationsFor(scope.getId()));
	}
}
