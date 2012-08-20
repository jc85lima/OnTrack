package br.com.oncast.ontrack.server.business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.oncast.ontrack.server.model.project.ProjectSnapshot;
import br.com.oncast.ontrack.server.model.project.UserAction;
import br.com.oncast.ontrack.server.services.actionPostProcessing.monitoring.DontPostProcessActions;
import br.com.oncast.ontrack.server.services.actionPostProcessing.monitoring.PostProcessActions;
import br.com.oncast.ontrack.server.services.authentication.AuthenticationManager;
import br.com.oncast.ontrack.server.services.authentication.DefaultAuthenticationCredentials;
import br.com.oncast.ontrack.server.services.authorization.AuthorizationManager;
import br.com.oncast.ontrack.server.services.email.FeedbackMailFactory;
import br.com.oncast.ontrack.server.services.notification.ClientManager;
import br.com.oncast.ontrack.server.services.notification.NotificationService;
import br.com.oncast.ontrack.server.services.persistence.PersistenceService;
import br.com.oncast.ontrack.server.services.persistence.exceptions.NoResultFoundException;
import br.com.oncast.ontrack.server.services.persistence.exceptions.PersistenceException;
import br.com.oncast.ontrack.server.services.session.SessionManager;
import br.com.oncast.ontrack.shared.exceptions.authorization.AuthorizationException;
import br.com.oncast.ontrack.shared.exceptions.authorization.UnableToAuthorizeUserException;
import br.com.oncast.ontrack.shared.exceptions.business.InvalidIncomingAction;
import br.com.oncast.ontrack.shared.exceptions.business.ProjectNotFoundException;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToCreateProjectRepresentation;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToHandleActionException;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToLoadProjectException;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToRetrieveProjectListException;
import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.FileUploadAction;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.TeamInviteAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.file.FileRepresentation;
import br.com.oncast.ontrack.shared.model.project.Project;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.services.actionExecution.ActionExecuter;
import br.com.oncast.ontrack.shared.services.actionSync.ModelActionSyncEvent;
import br.com.oncast.ontrack.shared.services.requestDispatch.ModelActionSyncRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectContextRequest;

class BusinessLogicImpl implements BusinessLogic {

	private static final Logger LOGGER = Logger.getLogger(BusinessLogicImpl.class);

	private final PersistenceService persistenceService;
	private final NotificationService notificationService;
	private final ClientManager clientManager;
	private final AuthenticationManager authenticationManager;
	private final SessionManager sessionManager;
	private final AuthorizationManager authorizationManager;
	private final FeedbackMailFactory feedbackMailFactory;

	protected BusinessLogicImpl(final PersistenceService persistenceService,
			final NotificationService notificationService, final ClientManager clientManager,
			final AuthenticationManager authenticationManager, final AuthorizationManager authorizationManager, final SessionManager sessionManager,
			final FeedbackMailFactory userQuotaRequestMailFactory) {
		this.persistenceService = persistenceService;
		this.notificationService = notificationService;
		this.clientManager = clientManager;
		this.authenticationManager = authenticationManager;
		this.authorizationManager = authorizationManager;
		this.sessionManager = sessionManager;
		this.feedbackMailFactory = userQuotaRequestMailFactory;
	}

	@Override
	@PostProcessActions
	public void handleIncomingActionSyncRequest(final ModelActionSyncRequest actionSyncRequest) throws UnableToHandleActionException,
			AuthorizationException {
		LOGGER.debug("Processing incoming action batch.");
		try {
			final UUID projectId = actionSyncRequest.getProjectId();
			authorizationManager.assureProjectAccessAuthorization(projectId);

			ModelActionSyncEvent modelActionSyncEvent = null;
			synchronized (this) {
				final User authenticatedUser = authenticationManager.getAuthenticatedUser();
				final Date timestamp = new Date();

				final ActionContext actionContext = new ActionContext(authenticatedUser, timestamp);
				final List<ModelAction> actionList = actionSyncRequest.getActionList();

				validateIncomingActions(projectId, actionList, actionContext);
				persistenceService.persistActions(projectId, actionSyncRequest.getActionList(), authenticatedUser.getId(), timestamp);
				modelActionSyncEvent = new ModelActionSyncEvent(projectId, actionList, actionContext);
			}
			notificationService.notifyActionsToOtherProjectUsers(modelActionSyncEvent);
		}
		catch (final PersistenceException e) {
			final String errorMessage = "The server could not handle the incoming action correctly. The action could not be persisted.";
			LOGGER.error(errorMessage, e);
			throw new UnableToHandleActionException(errorMessage);
		}
	}

	// TODO Report errors as feedback for development.
	// TODO Re-think validation strategy as loading the project every time may be a performance bottleneck.
	@PostProcessActions
	private ProjectContext validateIncomingActions(final UUID projectId, final List<ModelAction> actionList, final ActionContext actionContext)
			throws UnableToHandleActionException {
		LOGGER.debug("Validating action upon the project current state.");
		try {
			final Project project = loadProject(projectId);
			final ProjectContext context = new ProjectContext(project);
			for (final ModelAction action : actionList)
				ActionExecuter.executeAction(context, actionContext, action);
			return context;
		}
		catch (final UnableToCompleteActionException e) {
			final String errorMessage = "Unable to process action. The incoming action is invalid.";
			LOGGER.error(errorMessage, e);
			throw new InvalidIncomingAction(errorMessage);
		}
		catch (final UnableToLoadProjectException e) {
			final String errorMessage = "The server could not handle the incoming action. The action could not be validated because the project could not be loaded.";
			LOGGER.error(errorMessage, e);
			throw new UnableToHandleActionException(errorMessage);
		}
		catch (final Exception e) {
			final String errorMessage = "Unable to process action. An unknown problem occured.";
			LOGGER.error(errorMessage, e);
			throw new InvalidIncomingAction(errorMessage);
		}
	}

	@DontPostProcessActions
	private Project applyActionsToProject(final Project project, final List<UserAction> actionList) throws UnableToCompleteActionException {
		final ProjectContext projectContext = new ProjectContext(project);

		for (final UserAction action : actionList) {
			User user;
			try {
				user = persistenceService.retrieveUserById(action.getUserId());
				final ActionContext actionContext = new ActionContext(user, action.getTimestamp());
				ActionExecuter.executeAction(projectContext, actionContext, action.getModelAction());
			}
			catch (final Exception e) {
				LOGGER.error("Unable to apply action to project", e);
				throw new UnableToCompleteActionException(e);
			}
		}

		return project;
	}

	@Override
	public void authorize(final String userEmail, final UUID projectId, final boolean wasRequestedByTheUser) throws UnableToAuthorizeUserException,
			UnableToHandleActionException,
			AuthorizationException {
		authorizationManager.authorize(projectId, userEmail, wasRequestedByTheUser);
		LOGGER.debug("Authorized user '" + userEmail + "' to project '" + projectId.toStringRepresentation() + "'");
		handleIncomingActionSyncRequest(new ModelActionSyncRequest(projectId, Arrays.asList(new ModelAction[] { new TeamInviteAction(userEmail) })));
	}

	@Override
	public List<ProjectRepresentation> retrieveCurrentUserProjectList() throws UnableToRetrieveProjectListException {
		final User user = authenticationManager.getAuthenticatedUser();
		LOGGER.debug("Retrieving authorized project list for user '" + user + "'.");
		try {
			return authorizationManager.listAuthorizedProjects(user);
		}
		catch (final Exception e) {
			final String errorMessage = "Unable to retrieve the current user project list.";
			LOGGER.error(errorMessage, e);
			throw new UnableToRetrieveProjectListException(errorMessage);
		}
	}

	@Override
	// TODO make this method transactional.
	public ProjectRepresentation createProject(final String projectName) throws UnableToCreateProjectRepresentation, PersistenceException,
			AuthorizationException {

		LOGGER.debug("Creating new project '" + projectName + "'.");
		final User authenticatedUser = authenticationManager.getAuthenticatedUser();
		authorizationManager.validateAndUpdateUserProjectCreationQuota(authenticatedUser);

		try {
			final ProjectRepresentation persistedProjectRepresentation = persistenceService.persistOrUpdateProjectRepresentation(new ProjectRepresentation(
					projectName));

			authorize(authenticatedUser.getEmail(), persistedProjectRepresentation.getId(), false);
			if (!authenticatedUser.getEmail().equals(DefaultAuthenticationCredentials.USER_EMAIL)) authorizationManager
					.authorizeAdmin(persistedProjectRepresentation);

			notificationService.notifyProjectCreation(authenticatedUser.getId(), persistedProjectRepresentation);

			return persistedProjectRepresentation;
		}
		catch (final Exception e) {
			final String errorMessage = "Unable to create project '" + projectName + "'.";
			LOGGER.error(errorMessage, e);
			throw new UnableToCreateProjectRepresentation(errorMessage);
		}
	}

	@Override
	public synchronized Project loadProjectForClient(final ProjectContextRequest projectContextRequest) throws UnableToLoadProjectException,
			ProjectNotFoundException {
		final Project loadedProject = loadProject(projectContextRequest.getRequestedProjectId());
		clientManager.bindClientToProject(sessionManager.getCurrentSession().getThreadLocalClientId(), projectContextRequest.getRequestedProjectId());
		return loadedProject;
	}

	@Override
	@DontPostProcessActions
	public Project loadProject(final UUID projectId) throws ProjectNotFoundException, UnableToLoadProjectException {
		LOGGER.debug("Loading current state for project id '" + projectId + "'.");
		try {
			authorizationManager.assureProjectAccessAuthorization(projectId);

			return doLoadProject(projectId);
		}
		catch (final PersistenceException e) {
			final String errorMessage = "The server could not load the project: A persistence exception occured.";
			LOGGER.error(errorMessage, e);
			throw new UnableToLoadProjectException(errorMessage);
		}
		catch (final AuthorizationException e) {
			final String errorMessage = "Access denied to project '" + projectId + "'";
			LOGGER.error(errorMessage, e);
			throw new UnableToLoadProjectException(errorMessage);
		}
	}

	private Project doLoadProject(final UUID projectId) throws ProjectNotFoundException, UnableToLoadProjectException {
		try {
			final ProjectSnapshot snapshot = loadProjectSnapshot(projectId);
			final List<UserAction> actionList = persistenceService.retrieveActionsSince(projectId, snapshot.getLastAppliedActionId());

			Project project = snapshot.getProject();
			if (actionList.isEmpty()) return project;

			project = applyActionsToProject(project, actionList);
			updateProjectSnapshot(snapshot, project, actionList.get(actionList.size() - 1).getId());

			return project;
		}
		catch (final NoResultFoundException e) {
			final String errorMessage = "The project '" + projectId + "' is inexistent.";
			LOGGER.error(errorMessage, e);
			throw new ProjectNotFoundException(errorMessage);
		}
		catch (final PersistenceException e) {
			final String errorMessage = "The server could not load the project: A persistence exception occured.";
			LOGGER.error(errorMessage, e);
			throw new UnableToLoadProjectException(errorMessage);
		}
		catch (final UnableToCompleteActionException e) {
			final String errorMessage = "The project state could not be correctly restored.";
			LOGGER.error(errorMessage, e);
			throw new UnableToLoadProjectException(errorMessage);
		}
		catch (final Exception e) {
			final String errorMessage = "The server could not load the project: Unknown error.";
			LOGGER.error(errorMessage, e);
			throw new UnableToLoadProjectException(errorMessage);
		}
	}

	private ProjectSnapshot loadProjectSnapshot(final UUID projectId) throws PersistenceException, UnableToLoadProjectException, NoResultFoundException {
		ProjectSnapshot snapshot;
		try {
			snapshot = persistenceService.retrieveProjectSnapshot(projectId);
		}
		catch (final NoResultFoundException e) {
			snapshot = createBlankProjectSnapshot(projectId);
		}
		return snapshot;
	}

	private ProjectSnapshot createBlankProjectSnapshot(final UUID projectId) throws UnableToLoadProjectException, NoResultFoundException, PersistenceException {
		try {
			final ProjectRepresentation projectRepresentation = persistenceService.retrieveProjectRepresentation(projectId);

			final Scope projectScope = new Scope(projectRepresentation.getName(), new UUID("0"));
			final Release projectRelease = new Release(projectRepresentation.getName(), new UUID("release0"));

			final ProjectSnapshot projectSnapshot = new ProjectSnapshot(new Project(projectRepresentation, projectScope,
					projectRelease), new Date());
			return projectSnapshot;
		}
		catch (final IOException e) {
			final String errorMessage = "It was not possible to create a blank project snapshot.";
			LOGGER.error(errorMessage, e);
			throw new UnableToLoadProjectException(errorMessage);
		}
	}

	private void updateProjectSnapshot(final ProjectSnapshot snapshot, final Project project, final long lastAppliedActionId) throws IOException,
			PersistenceException {

		snapshot.setProject(project);
		snapshot.setTimestamp(new Date());
		snapshot.setLastAppliedActionId(lastAppliedActionId);

		persistenceService.persistProjectSnapshot(snapshot);
	}

	@Override
	public void sendProjectCreationQuotaRequestEmail() {
		feedbackMailFactory.createUserQuotaRequestMail()
				.currentUser(authenticationManager.getAuthenticatedUser().getEmail())
				.send();
	}

	@Override
	public void sendFeedbackEmail(final String feedbackMessage) {
		feedbackMailFactory.createSendFeedbackMail()
				.currentUser(authenticationManager.getAuthenticatedUser().getEmail())
				.feedbackMessage(feedbackMessage)
				.send();

	}

	@Override
	public void onFileUploadCompleted(final FileRepresentation fileRepresentation) throws UnableToHandleActionException, AuthorizationException {
		final List<ModelAction> actionList = new ArrayList<ModelAction>();
		actionList.add(new FileUploadAction(fileRepresentation));
		handleIncomingActionSyncRequest(new ModelActionSyncRequest(fileRepresentation.getProjectId(), actionList));
	}

	@Override
	@PostProcessActions
	public void loadProjectForMigration(final UUID projectId) throws ProjectNotFoundException, UnableToLoadProjectException {
		doLoadProject(projectId);
	}
}
