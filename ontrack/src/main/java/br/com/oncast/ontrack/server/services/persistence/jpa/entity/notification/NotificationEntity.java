package br.com.oncast.ontrack.server.services.persistence.jpa.entity.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConversionAlias;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertUsing;
import br.com.oncast.ontrack.server.utils.typeConverter.custom.StringToUuidConverter;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.services.notification.Notification;

@Entity(name = "Notification")
@ConvertTo(Notification.class)
public class NotificationEntity {

	@Id
	@ConvertUsing(StringToUuidConverter.class)
	@ConversionAlias("id")
	private String id;

	@Column(name = "message", unique = false, nullable = false)
	@ConversionAlias("message")
	private String message;

	@Temporal(TemporalType.TIMESTAMP)
	@ConversionAlias("timestamp")
	private Date timestamp;

	@OneToMany(cascade = CascadeType.ALL)
	@ConversionAlias("recipients")
	private List<User> recipients = new ArrayList<User>();

	public NotificationEntity() {}

	public String getId() {
		return id;
	}

	public void setId(final String id) {
		this.id = id;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}

	public List<User> getRecipients() {
		return recipients;
	}

	public void setRecipients(final List<User> recipients) {
		this.recipients = recipients;
	}
}
