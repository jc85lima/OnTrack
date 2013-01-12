package br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal;

import java.util.ArrayList;
import java.util.List;

import br.com.oncast.ontrack.client.ui.components.scopetree.ScopeTreeItem;
import br.com.oncast.ontrack.client.ui.components.scopetree.widgets.ScopeTreeWidget;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.color.Color;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.tag.Tag;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

public class AddTagInternalAction implements OneStepInternalAction {

	private final ProjectContext context;
	private final Scope scope;
	private ScopeTreeItem selectedTreeItem;

	public AddTagInternalAction(final Scope scope, final ProjectContext context) {
		this.context = context;
		this.scope = scope;
	}

	@Override
	public void execute(final ScopeTreeWidget tree) throws UnableToCompleteActionException {
		selectedTreeItem = InternalActionHelper.findScopeTreeItem(tree, scope);
		// FIXME LOBO
		final List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag(new UUID(), "tag aaaa", Color.RED, Color.GRAY));

		tags.add(new Tag(new UUID(), "tag bbb", Color.GREEN, Color.YELLOW));

		tags.add(new Tag(new UUID(), "tag ccc", Color.BLUE, Color.RED));

		tags.add(new Tag(new UUID(), "tag ddd", Color.TRANSPARENT, Color.RED));

		tags.add(new Tag(new UUID(), "tag eee", Color.YELLOW, Color.RED));
		selectedTreeItem.getScopeTreeItemWidget().showTagMenu(tags);
	}
}
