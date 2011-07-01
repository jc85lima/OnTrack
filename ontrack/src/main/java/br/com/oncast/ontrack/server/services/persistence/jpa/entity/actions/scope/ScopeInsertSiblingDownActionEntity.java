package br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope;

import javax.persistence.Entity;

import br.com.oncast.ontrack.server.services.persistence.jpa.beanConverter.annotations.MapTo;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.shared.model.scope.actions.ScopeInsertSiblingDownAction;

@Entity
@MapTo(ScopeInsertSiblingDownAction.class)
public class ScopeInsertSiblingDownActionEntity extends ModelActionEntity {

	private String referenceId;
	private String newScopeId;
	private String pattern;

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(final String referenceId) {
		this.referenceId = referenceId;
	}

	public String getNewScopeId() {
		return newScopeId;
	}

	public void setNewScopeId(final String newScopeId) {
		this.newScopeId = newScopeId;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(final String pattern) {
		this.pattern = pattern;
	}
}