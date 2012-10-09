package br.com.oncast.ontrack.server.business.notification;

import java.util.List;

import br.com.oncast.ontrack.server.services.persistence.PersistenceService;
import br.com.oncast.ontrack.server.services.persistence.exceptions.NoResultFoundException;
import br.com.oncast.ontrack.server.services.persistence.exceptions.PersistenceException;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToPostProcessActionException;
import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.ImpedimentCreateAction;
import br.com.oncast.ontrack.shared.model.action.ImpedimentSolveAction;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.services.notification.Notification;
import br.com.oncast.ontrack.shared.services.notification.Notification.NotificationType;
import br.com.oncast.ontrack.shared.services.notification.NotificationBuilder;

public class NotificationFactory {

	private enum ActionNotificationCreator {
		IMPEDIMENT_CREATION(ImpedimentCreateAction.class) {
			@Override
			protected NotificationBuilder createNotificationBuilder(final ModelAction action, final ProjectRepresentation projectRepresentation,
					final User author) {
				final NotificationBuilder notificationBuilder = new NotificationBuilder(NotificationType.IMPEDIMENT_CREATED, projectRepresentation, author);
				notificationBuilder.setReferenceId(action.getReferenceId());

				return notificationBuilder;
			}
		},
		IMPEDIMENT_COMPLETITION(ImpedimentSolveAction.class) {
			@Override
			protected NotificationBuilder createNotificationBuilder(final ModelAction action, final ProjectRepresentation projectRepresentation,
					final User author) {
				final NotificationBuilder notificationBuilder = new NotificationBuilder(NotificationType.IMPEDIMENT_SOLVED, projectRepresentation, author);
				notificationBuilder.setReferenceId(action.getReferenceId());

				return notificationBuilder;
			}
		};

		private final Class<? extends ModelAction> clazz;

		private ActionNotificationCreator(final Class<? extends ModelAction> clazz) {
			this.clazz = clazz;
		}

		protected static ActionNotificationCreator getCreatorFor(final Class<? extends ModelAction> clazz) {
			final ActionNotificationCreator[] values = ActionNotificationCreator.values();
			for (final ActionNotificationCreator actionNotificationCreator : values) {
				if (actionNotificationCreator.clazz.isAssignableFrom(clazz)) return actionNotificationCreator;
			}
			return null;
		}

		protected abstract NotificationBuilder createNotificationBuilder(ModelAction action, ProjectRepresentation projectRepresentation, User author);
	}

	private final PersistenceService persistenceService;

	public NotificationFactory(final PersistenceService persistenceService) {
		this.persistenceService = persistenceService;
	}

	public Notification createNofification(final ModelAction action, final ActionContext actionContext, final ProjectContext projectContext)
			throws PersistenceException {
		try {
			final ActionNotificationCreator creator = ActionNotificationCreator.getCreatorFor(action.getClass());
			if (creator == null) return null;

			final ProjectRepresentation projectRepresentation = projectContext.getProjectRepresentation();
			final List<User> projectUsers = persistenceService.retrieveProjectUsers(projectRepresentation);
			final User author = persistenceService.retrieveUserByEmail(actionContext.getUserEmail());

			final NotificationBuilder notificationBuilder = creator.createNotificationBuilder(action, projectRepresentation, author);

			for (final User user : projectUsers) {
				notificationBuilder.addReceipient(user);
			}

			return notificationBuilder.getNotification();
		}
		catch (final NoResultFoundException e) {
			throw new UnableToPostProcessActionException("The author user '" + actionContext.getUserEmail() + "' could not be retrieved from the persistence.",
					e);
		}
	}
}
