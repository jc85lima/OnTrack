package br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.server.util.converter.annotations.ConvertTo;
import br.com.oncast.ontrack.server.util.converter.annotations.ConvertUsing;
import br.com.oncast.ontrack.server.util.converter.custom.StringToUuidConverter;
import br.com.oncast.ontrack.shared.model.scope.actions.ScopeBindReleaseAction;

@ConvertTo(ScopeBindReleaseAction.class)
public class ScopeBindReleaseActionEntity extends ModelActionEntity {

	@ConvertUsing(StringToUuidConverter.class)
	private String referenceId;

	private String newReleaseDescription;

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(final String referenceId) {
		this.referenceId = referenceId;
	}

	public String getNewReleaseDescription() {
		return newReleaseDescription;
	}

	public void setNewReleaseDescription(final String newReleaseDescription) {
		this.newReleaseDescription = newReleaseDescription;
	}
}
