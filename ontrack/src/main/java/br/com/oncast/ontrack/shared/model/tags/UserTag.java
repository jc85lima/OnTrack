package br.com.oncast.ontrack.shared.model.tags;

import java.io.Serializable;

import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

public class UserTag implements Tag, Serializable {

	private static final long serialVersionUID = UserTag.class.hashCode();

	private Scope scope;

	private User user;

	private UUID id;

	public UserTag() {}

	public UserTag(final UUID tagId, final Scope scope, final User user) {
		this.id = tagId;
		this.scope = scope;
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public HasTags getSubject() {
		return scope;
	}

	@Override
	public TagType getTagType() {
		return getType();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final UserTag other = (UserTag) obj;
		if (id == null) {
			if (other.id != null) return false;
		}
		else if (!id.equals(other.id)) return false;
		return true;
	}

	public static TagType getType() {
		return TagType.USER;
	}

}
