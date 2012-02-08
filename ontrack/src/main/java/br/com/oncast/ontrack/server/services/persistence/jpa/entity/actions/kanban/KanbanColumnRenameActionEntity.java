package br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.kanban;

import javax.persistence.Column;
import javax.persistence.Entity;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConversionAlias;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertUsing;
import br.com.oncast.ontrack.server.utils.typeConverter.custom.StringToUuidConverter;
import br.com.oncast.ontrack.shared.model.action.KanbanColumnRenameAction;

@Entity(name = "KanbanColumnRename")
@ConvertTo(KanbanColumnRenameAction.class)
public class KanbanColumnRenameActionEntity extends ModelActionEntity {

	@ConversionAlias("releaseId")
	@ConvertUsing(StringToUuidConverter.class)
	@Column(name = "referenceId")
	private String releaseId;

	@ConversionAlias("columnDescription")
	@Column(name = "secondaryReferenceId")
	private String columnDescription;

	@ConversionAlias("newDescription")
	@Column(name = "description")
	private String newDescription;

	public String getReleaseId() {
		return releaseId;
	}

	public void setReleaseId(final String releaseId) {
		this.releaseId = releaseId;
	}

	public String getColumnDescription() {
		return columnDescription;
	}

	public void setColumnDescription(final String columnDescription) {
		this.columnDescription = columnDescription;
	}

	public String getNewDescription() {
		return newDescription;
	}

	public void setNewDescription(final String newDescription) {
		this.newDescription = newDescription;
	}
}
