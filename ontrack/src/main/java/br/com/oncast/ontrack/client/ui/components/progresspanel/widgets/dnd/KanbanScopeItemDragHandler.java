package br.com.oncast.ontrack.client.ui.components.progresspanel.widgets.dnd;

import br.com.oncast.ontrack.client.ui.components.progresspanel.interaction.ProgressPanelWidgetInteractionHandler;
import br.com.oncast.ontrack.client.ui.components.progresspanel.widgets.KanbanScopeContainer;
import br.com.oncast.ontrack.client.ui.components.progresspanel.widgets.ScopeWidget;
import br.com.oncast.ontrack.shared.model.kanban.KanbanColumn;
import br.com.oncast.ontrack.shared.model.progress.Progress.ProgressState;
import br.com.oncast.ontrack.shared.model.scope.Scope;

import com.allen_sauer.gwt.dnd.client.DragEndEvent;
import com.allen_sauer.gwt.dnd.client.DragHandlerAdapter;
import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.ui.VerticalPanel;

public class KanbanScopeItemDragHandler extends DragHandlerAdapter {

	private final ProgressPanelWidgetInteractionHandler interactionHandler;

	public KanbanScopeItemDragHandler(final ProgressPanelWidgetInteractionHandler interactionHandler) {
		this.interactionHandler = interactionHandler;
	}

	@Override
	public void onDragEnd(final DragEndEvent event) {
		final DropController finalDropController = event.getContext().finalDropController;
		if (finalDropController == null) return;

		final VerticalPanel dropTarget = (VerticalPanel) finalDropController.getDropTarget();
		final ScopeWidget draggedScope = (ScopeWidget) event.getContext().draggable;
		final KanbanColumn kanbanColumn = ((KanbanScopeContainer) dropTarget.getParent().getParent()).getKanbanColumn();

		if (!isPriorityChange(draggedScope.getModelObject(), kanbanColumn.getDescription())) {
			interactionHandler.onDragAndDropProgressRequest(draggedScope.getModelObject(), kanbanColumn.getDescription());
		}
	}

	private boolean isPriorityChange(final Scope scope, final String title) {
		if (scope.getProgress().getState() == ProgressState.NOT_STARTED && ProgressState.getStateForDescription(title) == ProgressState.NOT_STARTED) return true;
		return scope.getProgress().getDescription().equals(title);
	}
}
