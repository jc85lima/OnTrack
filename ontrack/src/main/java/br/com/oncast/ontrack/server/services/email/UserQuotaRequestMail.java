package br.com.oncast.ontrack.server.services.email;

public class UserQuotaRequestMail implements OnTrackMail {

	private final String currentUser;

	private UserQuotaRequestMail(final String currentUser) {
		this.currentUser = currentUser;
	}

	public static UserQuotaRequestMail getMail(final String currentUser) {
		return new UserQuotaRequestMail(currentUser);
	}

	@Override
	public String getSubject() {
		return "[OnTrack] Project Creation Quota Request";
	}

	@Override
	public String getTemplatePath() {
		return "/br/com/oncast/ontrack/server/services/email/projectCreationQuotaRequest.html";
	}

	@Override
	public MailVariableValuesMap getParameters() {
		final MailVariableValuesMap context = new MailVariableValuesMap();
		context.put("currentUser", currentUser);
		return context;
	}

	@Override
	public String getSendTo() {
		return MailConfigurationProvider.getMailUsername();
	}
}
