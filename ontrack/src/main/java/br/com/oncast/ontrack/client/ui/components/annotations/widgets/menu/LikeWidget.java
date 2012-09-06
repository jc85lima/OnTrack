package br.com.oncast.ontrack.client.ui.components.annotations.widgets.menu;

import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.shared.model.annotation.Annotation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class LikeWidget extends Composite implements AnnotationMenuItem {

	private static LikeWidgetUiBinder uiBinder = GWT.create(LikeWidgetUiBinder.class);

	interface LikeWidgetUiBinder extends UiBinder<Widget, LikeWidget> {}

	interface LikeWidgetStyle extends CssResource {
		String iconActive();
	}

	@UiField
	FocusPanel icon;

	@UiField
	Label label;

	@UiField
	LikeWidgetStyle style;

	private final UUID subjectId;

	private final Annotation annotation;

	public LikeWidget(final UUID subjectId, final Annotation annotation) {
		this.subjectId = subjectId;
		this.annotation = annotation;

		initWidget(uiBinder.createAndBindUi(this));

		update();
	}

	@Override
	public void update() {
		label.setText("" + annotation.getVoteCount());
		icon.setStyleName(style.iconActive(), hasVoted());
	}

	private boolean hasVoted() {
		return annotation.hasVoted(ClientServiceProvider.getInstance().getAuthenticationService().getCurrentUser());
	}

	@UiHandler("icon")
	void onClick(final ClickEvent e) {
		if (hasVoted()) ClientServiceProvider.getInstance().getAnnotationService().removeVote(subjectId, annotation.getId());
		else ClientServiceProvider.getInstance().getAnnotationService().addVote(subjectId, annotation.getId());
	}

}
