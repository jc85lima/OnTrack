package br.com.oncast.ontrack.client.ui.generalwidgets.scope;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionListener;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidget;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.TagUpdateAction;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.tag.Tag;
import br.com.oncast.ontrack.shared.services.actionExecution.ActionExecutionContext;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;

public class ScopeTagWidget extends Composite implements ModelWidget<Tag>, ActionExecutionListener {

	private static ScopeTagWidgetUiBinder uiBinder = GWT.create(ScopeTagWidgetUiBinder.class);

	interface ScopeTagWidgetUiBinder extends UiBinder<Widget, ScopeTagWidget> {}

	@UiField
	FocusPanel root;

	@UiField
	DivElement upperBg;

	private final Tag tag;

	public ScopeTagWidget(final Tag tag) {
		this.tag = tag;
		initWidget(uiBinder.createAndBindUi(this));
		update();
	}

	@Override
	protected void onLoad() {
		ClientServices.get().actionExecution().addActionExecutionListener(this);
	}

	@Override
	protected void onUnload() {
		ClientServices.get().actionExecution().removeActionExecutionListener(this);
	}

	@Override
	public boolean update() {
		root.setTitle(tag.getDescription());
		updateColor();
		return false;
	}

	private void updateColor() {
		final String bgColor = tag.getColorPack().getBackground().toCssRepresentation();
		upperBg.getStyle().setBackgroundColor(bgColor);
	}

	@Override
	public Tag getModelObject() {
		return tag;
	}

	@Override
	public void onActionExecution(final ActionExecutionContext execution, final ProjectContext context, final boolean isUserAction) {
		final ModelAction action = execution.getModelAction();
		if (action instanceof TagUpdateAction && action.getReferenceId().equals(tag.getId())) update();
	};

}
