package br.com.oncast.ontrack.client.ui.component.editableLabel.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Allows in-place editing of Labels. Activated via click.
 * 
 * The reference value is always the one in the label and change events are only fired after editing has finished.
 * 
 * TODO make it optional how to activate the editing mode
 * 
 * @author Jonas Huckestein
 * 
 */
public class EditableLabel extends Composite implements HasValue<String> {

	private static EditableLabelUiBinder uiBinder = GWT.create(EditableLabelUiBinder.class);

	interface EditableLabelUiBinder extends UiBinder<Widget, EditableLabel> {}

	@UiField
	protected Label editLabel;

	@UiField
	protected DeckPanel deckPanel;

	@UiField
	protected TextBox editBox;

	@UiField
	protected FocusPanel focusPanel;

	public EditableLabel(final String text) {

		initWidget(uiBinder.createAndBindUi(this));

		editLabel.setText(text);

		deckPanel.showWidget(0);

		editLabel.addDoubleClickHandler(new DoubleClickHandler() {

			@Override
			public void onDoubleClick(final DoubleClickEvent event) {
				switchToEdit();
			}
		});

		editBox.addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(final BlurEvent event) {
				switchToLabel();
			}
		});

		editBox.addKeyPressHandler(new KeyPressHandler() {

			@Override
			public void onKeyPress(final KeyPressEvent event) {

				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
					switchToLabel();
				} else if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
					editBox.setText(editLabel.getText()); // reset to the original value
					switchToLabel();
				}
			}
		});
	}

	public void switchToEdit() {
		if (deckPanel.getVisibleWidget() == 1) return;
		editBox.setText(getValue());
		deckPanel.showWidget(1);
		editBox.setFocus(true);
	}

	public void switchToLabel() {
		if (deckPanel.getVisibleWidget() == 0) return;
		setValue(editBox.getText(), true); // fires events, too
		deckPanel.showWidget(0);
	}

	@Override
	public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public String getValue() {
		return editLabel.getText();
	}

	@Override
	public void setValue(final String value) {
		editLabel.setText(value);
		editBox.setText(value);
	}

	@Override
	public void setValue(final String value, final boolean fireEvents) {
		if (fireEvents) ValueChangeEvent.fireIfNotEqual(this, getValue(), value);
		setValue(value);
	}
}
