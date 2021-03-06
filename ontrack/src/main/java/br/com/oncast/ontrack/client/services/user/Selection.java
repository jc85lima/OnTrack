package br.com.oncast.ontrack.client.services.user;

import br.com.oncast.ontrack.shared.model.color.Color;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;

public class Selection {

	private final UserRepresentation user;
	private Color color;

	public Selection(final UserRepresentation user, final Color selectionColor) {
		this.user = user;
		this.color = selectionColor;
	}

	public Selection(final UserRepresentation user) {
		this.user = user;
	}

	public UserRepresentation getUser() {
		return user;
	}

	public Color getColor() {
		return color;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final Selection other = (Selection) obj;
		if (user == null) {
			if (other.user != null) return false;
		}
		return user.equals(other.user);
	}

}