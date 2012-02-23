package br.com.oncast.ontrack.client.ui.components.releasepanel.widgets;

import br.com.oncast.ontrack.client.ui.components.releasepanel.widgets.dnd.ItemDroppedListener;
import br.com.oncast.ontrack.client.ui.components.releasepanel.widgets.dnd.ReleaseScopeItemDragHandler;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidgetContainerListener;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidgetFactory;
import br.com.oncast.ontrack.client.ui.generalwidgets.VerticalModelWidgetContainer;
import br.com.oncast.ontrack.client.ui.generalwidgets.dnd.DragAndDropManager;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.utils.deepEquality.IgnoredByDeepEquality;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

public class ReleasePanelWidget extends Composite {

	interface ReleasePanelWidgetUiBinder extends UiBinder<Widget, ReleasePanelWidget> {}

	@IgnoredByDeepEquality
	private static ReleasePanelWidgetUiBinder uiBinder = GWT.create(ReleasePanelWidgetUiBinder.class);

	@UiField
	@IgnoredByDeepEquality
	protected VerticalModelWidgetContainer<Release, ReleaseWidget> releaseContainer;

	private Release rootRelease;

	// IMPORTANT: This field cannot be 'final' because some tests need to set it to a new value through reflection. Do not remove the 'null' attribution.
	@IgnoredByDeepEquality
	private ModelWidgetFactory<Release, ReleaseWidget> releaseWidgetFactory = null;

	public ReleasePanelWidget(final ReleasePanelWidgetInteractionHandler releasePanelInteractionHandler) {
		final DragAndDropManager dragAndDropManager = new DragAndDropManager();
		releaseWidgetFactory = new ReleaseWidgetFactory(releasePanelInteractionHandler, new ScopeWidgetFactory(releasePanelInteractionHandler,
				dragAndDropManager), dragAndDropManager);

		initWidget(uiBinder.createAndBindUi(this));
		dragAndDropManager.configureBoundaryPanel(RootPanel.get());
		dragAndDropManager.setDragHandler(createScopeItemDragHandler(releasePanelInteractionHandler));
	}

	public void setRelease(final Release rootRelease) {
		this.rootRelease = rootRelease;
		releaseContainer.update(rootRelease.getChildren());
	}

	public void update() {
		releaseContainer.update(rootRelease.getChildren());
	}

	public ReleaseWidget setHierarchicalContainerState(final Release release, final boolean state) {
		if (rootRelease.equals(release.getParent())) {
			final ReleaseWidget widget = releaseContainer.getWidgetFor(release);
			if (widget == null) throw new RuntimeException("Release not found");
			widget.setContainerState(state);
			return widget;
		}
		final ReleaseWidget parentWidget = setHierarchicalContainerState(release.getParent(), state);

		final ReleaseWidget widget = parentWidget.getChildReleasesContainer().getWidgetFor(release);
		if (widget == null) throw new RuntimeException("Release not found");
		widget.setContainerState(state);
		return widget;
	}

	@UiFactory
	protected VerticalModelWidgetContainer<Release, ReleaseWidget> createReleaseContainer() {
		return new VerticalModelWidgetContainer<Release, ReleaseWidget>(releaseWidgetFactory, new ModelWidgetContainerListener() {
			@Override
			public void onUpdateComplete(final boolean hasChanged) {}
		});
	}

	private ReleaseScopeItemDragHandler createScopeItemDragHandler(final ReleasePanelWidgetInteractionHandler releasePanelInteractionHandler) {
		return new ReleaseScopeItemDragHandler(createItemDroppedListener(releasePanelInteractionHandler));
	}

	private ItemDroppedListener createItemDroppedListener(final ReleasePanelWidgetInteractionHandler releasePanelInteractionHandler) {
		return new ItemDroppedListener() {
			@Override
			public void onItemDropped(final Scope droppedScope, final Release targetRelease, final int newScopePosition) {
				releasePanelInteractionHandler.onScopeDragAndDropRequest(droppedScope, targetRelease, newScopePosition);
			}
		};
	}
}
