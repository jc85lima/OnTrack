package br.com.oncast.ontrack.client.ui.generalwidgets.dnd;

import com.allen_sauer.gwt.dnd.client.drop.DropController;
import com.google.gwt.user.client.ui.CellPanel;

public interface DropControllerFactory {

	public DropController create(final CellPanel panel);
}
