package br.com.oncast.ontrack.client.ui.components.releasepanel.widgets;

import br.com.oncast.ontrack.shared.release.Release;

public interface ReleaseWidgetFactory {
	ReleaseWidget createReleaseWidget(Release release);
}
