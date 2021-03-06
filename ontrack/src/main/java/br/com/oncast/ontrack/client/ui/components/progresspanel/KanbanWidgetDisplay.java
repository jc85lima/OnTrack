package br.com.oncast.ontrack.client.ui.components.progresspanel;

import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionService;
import br.com.oncast.ontrack.client.ui.components.scope.ScopeCardWidget;
import br.com.oncast.ontrack.shared.model.kanban.Kanban;
import br.com.oncast.ontrack.shared.model.scope.Scope;

public interface KanbanWidgetDisplay {

	public void setActionExecutionService(ActionExecutionService actionExecutionService);

	public void update(Kanban kanban);

	public ScopeCardWidget getWidgetFor(Scope scope);
}