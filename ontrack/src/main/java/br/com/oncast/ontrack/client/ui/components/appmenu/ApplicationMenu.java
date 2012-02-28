package br.com.oncast.ontrack.client.ui.components.appmenu;

import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.client.services.authentication.UserLogoutCallback;
import br.com.oncast.ontrack.client.services.messages.ClientNotificationService;
import br.com.oncast.ontrack.client.ui.components.appmenu.widgets.ApplicationMenuItem;
import br.com.oncast.ontrack.client.ui.components.appmenu.widgets.BreadcrumbWidget;
import br.com.oncast.ontrack.client.ui.components.appmenu.widgets.InvitationWidget;
import br.com.oncast.ontrack.client.ui.components.appmenu.widgets.PasswordChangeWidget;
import br.com.oncast.ontrack.client.ui.components.appmenu.widgets.ProjectMenuWidget;
import br.com.oncast.ontrack.client.ui.generalwidgets.PopupConfig;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class ApplicationMenu extends Composite {

	private static final ClientServiceProvider SERVICE_PROVIDER = ClientServiceProvider.getInstance();

	private static ApplicationMenuWidgetUiBinder uiBinder = GWT.create(ApplicationMenuWidgetUiBinder.class);

	interface ApplicationMenuWidgetUiBinder extends UiBinder<Widget, ApplicationMenu> {}

	@UiField
	protected HTMLPanel applicationMenuPanel;

	@UiField
	protected ApplicationMenuItem projectMenuItem;

	@UiField
	protected ApplicationMenuItem memberMenuItem;

	@UiField
	protected ApplicationMenuItem userMenuItem;

	@UiField
	protected BreadcrumbWidget breadcrumb;

	public ApplicationMenu() {
		initWidget(uiBinder.createAndBindUi(this));

		createProjectMenu();

		createMemberMenu();

		createUserMenu();
	}

	public BreadcrumbWidget getBreadcrumb() {
		return breadcrumb;
	}

	public void setProjectName(final String name) {
		projectMenuItem.setText(name);
	}

	private void logUserOut() {
		SERVICE_PROVIDER.getAuthenticationService().logout(new UserLogoutCallback() {

			@Override
			public void onUserLogout() {}

			@Override
			public void onFailure(final Throwable caught) {
				// TODO Threat this error properly. Maybe even call the ErrorService.
				ClientNotificationService.showError("It was not possible to log the user out properly.");
			}
		});
	}

	private void createProjectMenu() {
		final PopupConfig config = PopupConfig.configPopup().popup(new ProjectMenuWidget()).alignBelow(applicationMenuPanel).alignRight(projectMenuItem);
		projectMenuItem.setPopupConfig(config);
	}

	private void createUserMenu() {
		final PopupConfig popup = PopupConfig.configPopup().popup(new PasswordChangeWidget()).alignRight(userMenuItem).alignBelow(applicationMenuPanel);
		userMenuItem.setPopupConfig(popup);
	}

	private void createMemberMenu() {
		final PopupConfig invitePopup = PopupConfig.configPopup().popup(new InvitationWidget()).alignRight(memberMenuItem).alignBelow(applicationMenuPanel);
		memberMenuItem.setPopupConfig(invitePopup);
	}
}