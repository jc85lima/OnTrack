package br.com.oncast.ontrack.client.ui.generalwidgets;

import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.HasKeyDownHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class PaddedTextBox extends Composite implements HasKeyDownHandlers, HasKeyUpHandlers, HasWidgets {

	private static PaddedTextBoxUiBinder uiBinder = GWT.create(PaddedTextBoxUiBinder.class);

	interface PaddedTextBoxUiBinder extends UiBinder<Widget, PaddedTextBox> {}

	public PaddedTextBox() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	HTMLPanel container;

	@UiField
	TextBox textBox;

	@UiField
	FocusPanel focusPanel;

	@UiHandler("focusPanel")
	public void onFocus(final FocusEvent e) {
		textBox.setFocus(true);
	}

	public void setText(final String text) {
		textBox.setText(text);
	}

	public String getText() {
		return textBox.getText();
	}

	@Override
	public HandlerRegistration addKeyDownHandler(final KeyDownHandler handler) {
		return textBox.addKeyDownHandler(handler);
	}

	@Override
	public HandlerRegistration addKeyUpHandler(final KeyUpHandler handler) {
		return textBox.addKeyUpHandler(handler);
	}

	public void setCursorPos(final int pos) {
		textBox.setCursorPos(pos);
	}

	public void setFocus(final boolean focused) {
		textBox.setFocus(focused);
	}

	public void selectAll() {
		textBox.selectAll();
	}

	public void setEnabled(final boolean enabled) {
		textBox.setEnabled(enabled);
	}

	@Override
	public void add(final Widget w) {
		container.add(w);
	}

	@Override
	public void clear() {
		container.clear();
	}

	@Override
	public Iterator<Widget> iterator() {
		return container.iterator();
	}

	@Override
	public boolean remove(final Widget w) {
		return container.remove(w);
	}

}