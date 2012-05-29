package br.com.oncast.ontrack.shared.model.action;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.annotation.AnnotationCreateActionEntity;
import br.com.oncast.ontrack.server.utils.typeConverter.annotations.ConvertTo;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.action.helper.ActionHelper;
import br.com.oncast.ontrack.shared.model.annotation.Annotation;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

@ConvertTo(AnnotationCreateActionEntity.class)
public class AnnotationCreateAction implements AnnotationAction {

	private static final long serialVersionUID = 1L;

	private UUID annotationId;

	private UUID annotatedObjectId;

	private Long authorId;

	private String message;

	protected AnnotationCreateAction() {}

	public AnnotationCreateAction(final UUID annotatedObjectId, final User author, final String message) {
		this.message = message;
		this.annotationId = new UUID();
		this.annotatedObjectId = annotatedObjectId;
		setAuthor(author);
	}

	public AnnotationCreateAction(final Annotation annotation, final UUID annotatedObjectId) {
		this(annotatedObjectId, annotation.getAuthor(), annotation.getMessage());
		annotationId = annotation.getId();
	}

	@Override
	public ModelAction execute(final ProjectContext context) throws UnableToCompleteActionException {
		final User author = ActionHelper.findUser(authorId, context);
		final Annotation annotation = new Annotation(annotationId, author, message);

		context.addAnnotation(annotation, annotatedObjectId);
		return new AnnotationRemoveAction(annotation.getId(), annotatedObjectId);
	}

	@Override
	public UUID getReferenceId() {
		return annotationId;
	}

	public void setAuthor(final User author) {
		this.authorId = author.getId();
	}

}
