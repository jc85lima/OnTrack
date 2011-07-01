package br.com.oncast.ontrack.server.services.persistence.jpa.mapping;

import java.lang.reflect.Field;

import br.com.oncast.ontrack.server.services.persistence.jpa.mapping.review.TypeMapper;

class SimpleMapper extends TypeMapper {

	public SimpleMapper(final TypeMapper myTrailer) {
		super(myTrailer);
	}

	@Override
	protected Object mapField(final Object from, final Object to, final Field field) throws IllegalArgumentException, IllegalAccessException,
			SecurityException, NoSuchFieldException {
		return field.get(from);
	}

	@Override
	protected boolean isMyAction(final Field field) {
		return true;
	}

}
