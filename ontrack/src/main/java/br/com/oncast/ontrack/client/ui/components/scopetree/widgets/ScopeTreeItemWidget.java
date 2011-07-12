package br.com.oncast.ontrack.client.ui.components.scopetree.widgets;

import static br.com.oncast.ontrack.shared.util.keyboard.BrowserKeyCodes.KEY_ENTER;
import static br.com.oncast.ontrack.shared.util.keyboard.BrowserKeyCodes.KEY_ESCAPE;
import br.com.oncast.ontrack.client.ui.generalwidgets.Tag;
import br.com.oncast.ontrack.shared.model.effort.Effort;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.stringrepresentation.ScopeRepresentationBuilder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ScopeTreeItemWidget extends Composite {

	private static EditableLabelUiBinder uiBinder = GWT.create(EditableLabelUiBinder.class);

	interface EditableLabelUiBinder extends UiBinder<Widget, ScopeTreeItemWidget> {}

	interface Style extends CssResource {
		String effortLabelTranslucid();

		String effortDifferenceLabelProblem();
	}

	@UiField
	protected Style style;

	@UiField
	protected DeckPanel deckPanel;

	@UiField
	protected Label descriptionLabel;

	@UiField
	protected Label effortLabel;

	@UiField
	protected Label effortDifferenceLabel;

	@UiField
	protected Label declaredEffortLabel;

	@UiField
	protected Label bottomUpValueEffortLabel;

	@UiField
	protected Label topDownValueEffortLabel;

	@UiField
	protected Label inferedEffortLabel;

	@UiField
	protected TextBox editionBox;

	@UiField
	protected Tag releaseTag;

	@UiField
	protected FocusPanel focusPanel;

	private final ScopeTreeItemWidgetEditionHandler editionHandler;

	private Scope scope;

	public ScopeTreeItemWidget(final Scope scope, final ScopeTreeItemWidgetEditionHandler editionHandler) {

		initWidget(uiBinder.createAndBindUi(this));
		setScope(scope);

		this.editionHandler = editionHandler;

		focusPanel.addDoubleClickHandler(new DoubleClickHandler() {
			@Override
			public void onDoubleClick(final DoubleClickEvent event) {
				switchToEditionMode();
			}
		});

		editionBox.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(final BlurEvent event) {
				switchToVisualization(true);
			}
		});

		editionBox.addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(final KeyUpEvent event) {
				if (!isEditing()) return;
				event.preventDefault();
				event.stopPropagation();

				if (event.getNativeKeyCode() == KEY_ENTER) {
					switchToVisualization(true);
				}
				else if (event.getNativeKeyCode() == KEY_ESCAPE) {
					switchToVisualization(false);
				}
			}
		});

		releaseTag.setCloseButtonClickHandler(new ClickHandler() {

			@Override
			public void onClick(final ClickEvent event) {
				editionHandler.onEditionEnd(new ScopeRepresentationBuilder(scope).includeEverything().excludeReleaseReference().toString());
			}
		});

		deckPanel.showWidget(0);
	}

	public String getValue() {
		return new ScopeRepresentationBuilder(scope).includeEverything().toString();
	}

	public void setValue(final String value) {
		descriptionLabel.setText(value);
		editionBox.setText(value);
	}

	public void switchToEditionMode() {
		if (isEditing()) return;

		editionBox.setText(getValue());
		deckPanel.showWidget(1);
		new Timer() {

			@Override
			public void run() {
				editionBox.selectAll();
				editionBox.setFocus(true);
			}
		}.schedule(100);
	}

	public void switchToVisualization(final boolean shouldTryToUpdateChanges) {
		if (!isEditing()) return;
		deckPanel.showWidget(0);

		if (!shouldTryToUpdateChanges) {
			editionBox.setText(descriptionLabel.getText());
			editionHandler.onEditionCancel();
		}
		else {
			if (!getValue().equals(editionBox.getText()) || editionBox.getText().isEmpty()) editionHandler.onEditionEnd(editionBox.getText());
			else editionHandler.onEditionCancel();
		}
	}

	private boolean isEditing() {
		return deckPanel.getVisibleWidget() == 1;
	}

	public void setScope(final Scope scope) {
		this.scope = scope;
		descriptionLabel.setText(scope.getDescription());
		editionBox.setText(scope.getDescription());
		updateEffortDisplay();
		updateReleaseDisplay();
	}

	public Scope getScope() {
		return scope;
	}

	// TODO It may check if the values changed.
	// TODO Change the way we show the effort
	public void updateEffortDisplay() {
		final Effort effort = scope.getEffort();

		declaredEffortLabel.setVisible(effort.hasDeclared());
		declaredEffortLabel.setText((effort.getDeclared()) + "");
		bottomUpValueEffortLabel.setText(((int) effort.getBottomUpValue()) + "");
		topDownValueEffortLabel.setText(((int) effort.getTopDownValue()) + "");
		inferedEffortLabel.setText(((int) effort.getInfered()) + "");

		final boolean effortVisibility = effort.hasDeclared() || effort.getInfered() > 0;
		final int effortErrorDifference = effort.hasDeclared() ? (int) (effort.getInfered() - effort.getDeclared()) : 0;
		final int effortPositiveDifference = effort.getBottomUpValue() != 0 ? (int) (effort.getInfered() - effort.getBottomUpValue()) : 0;
		// final int effortPositiveDifference = effort.getProcessedValue() != 0 ? (int) (effort.getDeclared() - effort.getProcessedValue()) : 0;
		final boolean effortDifferenceVisibility = (effortErrorDifference > 0 || effortPositiveDifference > 0);
		// final boolean effortDifferenceVisibility = effort.hasDeclared() && (effortErrorDifference > 0 || effortPositiveDifference > 0); // FIXME

		effortLabel.setVisible(effortVisibility);
		if (effort.hasDeclared()) effortLabel.getElement().removeClassName(style.effortLabelTranslucid());
		else effortLabel.getElement().addClassName(style.effortLabelTranslucid());
		effortLabel.setText(effort.getInfered() + "");

		effortDifferenceLabel.setVisible(effortDifferenceVisibility);
		if (!effortDifferenceVisibility) return;

		if (effortErrorDifference > 0) {
			effortDifferenceLabel.getElement().addClassName(style.effortDifferenceLabelProblem());
			effortDifferenceLabel.setText(effortErrorDifference + "");
		}
		else {
			effortDifferenceLabel.getElement().removeClassName(style.effortDifferenceLabelProblem());
			effortDifferenceLabel.setText(effortPositiveDifference + "");
		}
	}

	private void updateReleaseDisplay() {
		final Release release = scope.getRelease();
		final boolean isReleasePresent = release != null;
		releaseTag.setVisible(isReleasePresent);
		releaseTag.setText(isReleasePresent ? release.getFullDescription() : "");
	}
}
