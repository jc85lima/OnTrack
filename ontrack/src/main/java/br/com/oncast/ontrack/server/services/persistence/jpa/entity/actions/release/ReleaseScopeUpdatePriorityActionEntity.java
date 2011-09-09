package br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.release;

import javax.persistence.Entity;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConversionAlias;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertUsing;
import br.com.oncast.ontrack.server.utils.typeConverter.custom.StringToUuidConverter;
import br.com.oncast.ontrack.shared.model.actions.ReleaseScopeUpdatePriorityAction;

@Entity
@ConvertTo(ReleaseScopeUpdatePriorityAction.class)
public class ReleaseScopeUpdatePriorityActionEntity extends ModelActionEntity {

	@ConversionAlias("releaseReferenceId")
	@ConvertUsing(StringToUuidConverter.class)
	private String releaseReferenceId;

	@ConversionAlias("scopeReferenceId")
	@ConvertUsing(StringToUuidConverter.class)
	private String scopeReferenceId;

	@ConversionAlias("priority")
	private int priority;

	public String getReleaseReferenceId() {
		return releaseReferenceId;
	}

	public void setReleaseReferenceId(final String releaseReferenceId) {
		this.releaseReferenceId = releaseReferenceId;
	}

	public String getScopeReferenceId() {
		return scopeReferenceId;
	}

	public void setScopeReferenceId(final String scopeReferenceId) {
		this.scopeReferenceId = scopeReferenceId;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}
}