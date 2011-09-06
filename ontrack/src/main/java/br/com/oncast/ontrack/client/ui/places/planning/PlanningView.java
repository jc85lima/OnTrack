package br.com.oncast.ontrack.client.ui.places.planning;

import br.com.oncast.ontrack.client.ui.components.releasepanel.ReleasePanel;
import br.com.oncast.ontrack.client.ui.components.scopetree.ScopeTree;

import com.google.gwt.user.client.ui.IsWidget;

public interface PlanningView extends IsWidget {

	void setExporterPath(String href);

	ScopeTree getScopeTree();

	ReleasePanel getReleasePanel();
}
