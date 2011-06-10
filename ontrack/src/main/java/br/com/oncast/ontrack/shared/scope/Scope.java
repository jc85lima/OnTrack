package br.com.oncast.ontrack.shared.scope;

import java.util.List;

import br.com.oncast.ontrack.shared.release.Release;
import br.com.oncast.ontrack.shared.util.deeplyComparable.DeeplyComparable;
import br.com.oncast.ontrack.shared.util.deeplyComparable.DeeplyComparableList;
import br.com.oncast.ontrack.shared.util.uuid.UUID;

// TODO Test this class
public class Scope implements DeeplyComparable {

	private String description;
	private Scope parent;
	private final DeeplyComparableList<Scope> childrenList;
	private Release release;
	private final UUID uuid;

	public Scope(final String description) {
		this(description, null, null);
	}

	public Scope(final String description, final String uuid) {
		this(description, null, uuid);
	}

	public Scope(final String description, final Scope parent) {
		this(description, parent, null);
	}

	public Scope(final String description, final Scope parent, final String uuid) {
		this.description = description;
		this.parent = parent;
		childrenList = new DeeplyComparableList<Scope>();
		if (uuid != null) this.uuid = new UUID(uuid);
		else this.uuid = new UUID();
	}

	public String getDescription() {
		return description;
	}

	public List<Scope> getChildren() {
		return childrenList;
	}

	public Scope add(final Scope scope) {
		childrenList.add(scope);
		scope.parent = this;

		return this;
	}

	public void add(final int beforeIndex, final Scope scope) {
		childrenList.add(beforeIndex, scope);
		scope.parent = this;
	}

	public void remove(final Scope scope) {
		childrenList.remove(scope);
		scope.parent = null;
	}

	public Scope getParent() {
		return parent;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}

	public int getChildIndex(final Scope scope) {
		return childrenList.indexOf(scope);
	}

	public void clearChildren() {
		this.childrenList.clear();
	}

	public void setRelease(final Release release) {
		this.release = release;
	}

	public Release getRelease() {
		return release;
	}

	public UUID getUuid() {
		return uuid;
	}

	@Override
	public int hashCode() {
		return uuid.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof Scope)) return false;

		return this.uuid.equals(((Scope) obj).getUuid());
	}

	@Override
	public boolean deepEquals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof Scope)) return false;
		final Scope other = (Scope) obj;
		if (childrenList == null) {
			if (other.childrenList != null) return false;
		}
		else if (!childrenList.deepEquals(other.getChildren())) return false;
		if (description == null) {
			if (other.description != null) return false;
		}
		else if (!description.equals(other.description)) return false;
		return true;
	}
}