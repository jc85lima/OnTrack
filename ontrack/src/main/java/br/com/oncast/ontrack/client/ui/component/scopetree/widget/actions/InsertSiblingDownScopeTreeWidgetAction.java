package br.com.oncast.ontrack.client.ui.component.scopetree.widget.actions;

import br.com.oncast.ontrack.client.ui.component.scopetree.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.client.ui.component.scopetree.widget.ScopeTreeItem;
import br.com.oncast.ontrack.shared.beans.Scope;

public class InsertSiblingDownScopeTreeWidgetAction implements ScopeTreeWidgetAction {

	private final ScopeTreeItem treeItem;

	public InsertSiblingDownScopeTreeWidgetAction(final ScopeTreeItem treeItem) {
		this.treeItem = treeItem;
	}

	@Override
	public void execute() throws UnableToCompleteActionException {
		if (treeItem.isRoot()) throw new UnableToCompleteActionException("It is not possible to create a sibling for a root node.");
		else treeItem.getParentItem().insertItem(treeItem.getParentItem().getChildIndex(treeItem) + 1, new ScopeTreeItem(new Scope("Novo scope Down")));
	}

}