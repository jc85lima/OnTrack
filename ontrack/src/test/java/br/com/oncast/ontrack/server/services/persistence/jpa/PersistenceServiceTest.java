package br.com.oncast.ontrack.server.services.persistence.jpa;

import br.com.oncast.ontrack.server.model.project.ProjectSnapshot;
import br.com.oncast.ontrack.server.services.authentication.Password;
import br.com.oncast.ontrack.server.services.exportImport.xml.UserActionTestUtils;
import br.com.oncast.ontrack.server.services.persistence.PersistenceService;
import br.com.oncast.ontrack.server.services.persistence.exceptions.NoResultFoundException;
import br.com.oncast.ontrack.server.services.persistence.exceptions.PersistenceException;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.ProjectAuthorization;
import br.com.oncast.ontrack.shared.exceptions.business.ProjectNotFoundException;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToLoadProjectException;
import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ScopeInsertChildAction;
import br.com.oncast.ontrack.shared.model.action.UserAction;
import br.com.oncast.ontrack.shared.model.file.FileRepresentation;
import br.com.oncast.ontrack.shared.model.progress.Progress;
import br.com.oncast.ontrack.shared.model.project.Project;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.services.notification.Notification;
import br.com.oncast.ontrack.shared.services.notification.NotificationBuilder;
import br.com.oncast.ontrack.shared.services.notification.NotificationType;
import br.com.oncast.ontrack.shared.utils.WorkingDay;
import br.com.oncast.ontrack.shared.utils.WorkingDayFactory;
import br.com.oncast.ontrack.utils.assertions.AssertTestUtils;
import br.com.oncast.ontrack.utils.deepEquality.DeepEqualityTestUtils;
import br.com.oncast.ontrack.utils.mocks.actions.ActionTestUtils;
import br.com.oncast.ontrack.utils.mocks.models.UserRepresentationTestUtils;
import br.com.oncast.ontrack.utils.model.ProjectTestUtils;
import br.com.oncast.ontrack.utils.model.ScopeTestUtils;
import br.com.oncast.ontrack.utils.model.UserTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.when;

public class PersistenceServiceTest {

	private static final UUID PROJECT_ID = new UUID();

	private PersistenceService persistenceService;

	private EntityManager entityManager;

	private UUID author;

	@Before
	public void before() throws Exception {
		entityManager = Persistence.createEntityManagerFactory("ontrackPU").createEntityManager();
		persistenceService = new PersistenceServiceJpaImpl();
		assureProjectRepresentationExistance();
	}

	@After
	public void tearDown() {
		entityManager.close();
	}

	@Test
	public void shouldBeAbleToPersistUserActions() throws Exception {
		final List<UserAction> userActions = toUserActionsList(ActionTestUtils.createSomeActions());
		persistenceService.persistActions(userActions);

		final List<UserAction> persistedUserActions = persistenceService.retrieveActionsSince(PROJECT_ID, 0);
		AssertTestUtils.assertCollectionEquality(userActions, persistedUserActions);
	}

	@Test
	public void shouldReturnTheLastPersistedActionIdWhenActionsArePersisted() throws Exception {
		final List<UserAction> userActions = toUserActionsList(ActionTestUtils.createSomeActions());
		final long lastPersistedActionId = persistenceService.persistActions(userActions);
		assertEquals(userActions.size(), lastPersistedActionId);
	}

	@Test
	public void shouldUpdateTheUserActionsSequencialId() throws Exception {
		final List<UserAction> userActions = toUserActionsList(ActionTestUtils.createSomeActions());
		for (final UserAction action : userActions) {
			assertEquals(0, action.getSequencialId());
		}
		persistenceService.persistActions(userActions);
		for (int i = 0; i < userActions.size(); i++) {
			assertEquals(i + 1, userActions.get(i).getSequencialId());
		}
	}

	@Test
	public void shouldOnlyReturnActionsAfterAGivenId() throws Exception {
		final long actionId = persistenceService.persistActions(toUserActionsList(ActionTestUtils.createSomeActions()));

		final List<UserAction> secondWaveOfActions = toUserActionsList(ActionTestUtils.getActions2());
		persistenceService.persistActions(secondWaveOfActions);

		final List<UserAction> retrievedActions = persistenceService.retrieveActionsSince(PROJECT_ID, actionId);
		AssertTestUtils.assertCollectionEquality(secondWaveOfActions, retrievedActions);
	}

	@Test
	public void shouldRetrieveSnapshotAfterExecutingActionAndPersistingIt() throws Exception {
		final ProjectSnapshot snapshot1 = loadProjectSnapshot();
		final Project project1 = snapshot1.getProject();

		final ActionContext actionContext = Mockito.mock(ActionContext.class);
		when(actionContext.getUserId()).thenReturn(UserTestUtils.getAdmin().getId());
		when(actionContext.getTimestamp()).thenReturn(new Date(0));
		final ProjectContext context = new ProjectContext(project1);
		context.addUser(UserRepresentationTestUtils.getAdmin());
		new ScopeInsertChildAction(project1.getProjectScope().getId(), "big son").execute(context, actionContext);

		snapshot1.setProject(project1);
		snapshot1.setTimestamp(new Date());
		persistenceService.persistProjectSnapshot(snapshot1);

		final ProjectSnapshot snapshot2 = loadProjectSnapshot();
		final Project project2 = snapshot2.getProject();

		DeepEqualityTestUtils.assertObjectEquality(project1, project2);
	}

	@Test
	public void shouldRetrieveSnapshotWithCorrectProgressStartDateAndEndDate() throws Exception {
		final ProjectSnapshot snapshot1 = loadProjectSnapshot();
		final Project project1 = snapshot1.getProject();

		final WorkingDay startDate = WorkingDayFactory.create(2011, Calendar.OCTOBER, 3);
		final WorkingDay endDate = WorkingDayFactory.create(2011, Calendar.OCTOBER, 5);

		ScopeTestUtils.setStartDate(project1.getProjectScope(), startDate);
		ScopeTestUtils.setEndDate(project1.getProjectScope(), endDate);

		snapshot1.setProject(project1);
		snapshot1.setTimestamp(new Date());
		persistenceService.persistProjectSnapshot(snapshot1);

		final ProjectSnapshot snapshot2 = loadProjectSnapshot();
		final Project project2 = snapshot2.getProject();
		final Progress progress = project2.getProjectScope().getProgress();

		assertEquals(startDate, progress.getStartDay());
		assertEquals(endDate, progress.getEndDay());
	}

	@Test
	public void shouldPersistUser() throws PersistenceException, NoResultFoundException {
		final String email = "user1@email.com";
		final User user = UserTestUtils.createUser(email);
		persistenceService.persistOrUpdateUser(user);

		final User persistedUser = persistenceService.retrieveUserByEmail(email);
		assertEquals(user.getEmail(), persistedUser.getEmail());
	}

	@Test
	public void shouldUpdateUser() throws PersistenceException, NoResultFoundException {
		final String email = "user1@email.com";
		final User user = UserTestUtils.createUser(email);

		persistenceService.persistOrUpdateUser(user);

		final User newUser = persistenceService.retrieveUserByEmail(email);
		assertEquals(user.getEmail(), newUser.getEmail());

		final String newEmail = "newEmail@email.com";
		newUser.setEmail(newEmail);
		persistenceService.persistOrUpdateUser(newUser);
		final User updatedUser = persistenceService.retrieveUserByEmail(newEmail);
		assertEquals(newEmail, updatedUser.getEmail());
		assertEquals(newUser.getId(), updatedUser.getId());
	}

	@Test
	public void shouldPersistPasswordForAnExistentUser() throws PersistenceException, NoResultFoundException {
		final String email = "user1@email.com";
		User user = UserTestUtils.createUser(email);
		persistenceService.persistOrUpdateUser(user);

		user = persistenceService.retrieveUserByEmail(email);
		final Password password = new Password();
		password.setUserId(user.getId());
		final String passwordText = "password";
		password.setPassword(passwordText);
		persistenceService.persistOrUpdatePassword(password);
		final List<Password> newPassword = persistenceService.retrievePasswordsForUser(user.getId());
		assertTrue(newPassword.get(0).authenticate(passwordText));
	}

	@Test
	public void shouldUpdateAndExistentPassword() throws PersistenceException, NoResultFoundException {
		final String email = "user1@email.com";
		User user = UserTestUtils.createUser(email);

		persistenceService.persistOrUpdateUser(user);

		user = persistenceService.retrieveUserByEmail(email);
		final Password password = new Password();
		password.setUserId(user.getId());
		final String passwordText = "password";
		password.setPassword(passwordText);
		persistenceService.persistOrUpdatePassword(password);
		final Password firstPassword = persistenceService.retrievePasswordsForUser(user.getId()).get(0);
		assertTrue(firstPassword.authenticate(passwordText));

		final String newPassword = "newPassword";
		firstPassword.setPassword(newPassword);
		persistenceService.persistOrUpdatePassword(firstPassword);

		final Password secondPassword = persistenceService.retrievePasswordsForUser(user.getId()).get(0);
		assertFalse(secondPassword.authenticate(passwordText));
		assertTrue(secondPassword.authenticate(newPassword));
	}

	@Test
	public void shouldReturnEmptyListWhenUserDontHaveAPassword() throws PersistenceException, NoResultFoundException {
		final String email = "user1@email.com";
		User user = UserTestUtils.createUser(email);

		persistenceService.persistOrUpdateUser(user);

		user = persistenceService.retrieveUserByEmail(email);
		assertEquals(0, persistenceService.retrievePasswordsForUser(user.getId()).size());
	}

	@Test
	public void shouldReturnEmptyListWhenUserNotExist() throws PersistenceException, NoResultFoundException {
		persistenceService.retrievePasswordsForUser(new UUID());
	}

	@Test(expected = NoResultFoundException.class)
	public void shouldThrowNotResultFoundExceptionWhenUserNotFound() throws NoResultFoundException, PersistenceException {
		persistenceService.retrieveUserByEmail("inexistant@email.com");
	}

	@Test
	public void shouldRetrieveAllUsers() throws Exception {
		final User user1 = UserTestUtils.createUser("user@user1");
		persistenceService.persistOrUpdateUser(user1);
		final User user2 = UserTestUtils.createUser("user@user2");
		persistenceService.persistOrUpdateUser(user2);
		final User user3 = UserTestUtils.createUser("user@user3");
		persistenceService.persistOrUpdateUser(user3);
		final User user4 = UserTestUtils.createUser("user@user4");
		persistenceService.persistOrUpdateUser(user4);

		final List<User> userList = persistenceService.retrieveAllUsers();
		assertEquals(4, userList.size());

	}

	@Test
	public void shouldPersistProjectRepresentation() throws Exception {
		final ProjectRepresentation projectRepresentation = new ProjectRepresentation("Name");
		persistenceService.persistOrUpdateProjectRepresentation(projectRepresentation);
		final ProjectRepresentation foundProjectRepresentation = persistenceService.retrieveProjectRepresentation(projectRepresentation.getId());
		assertEquals("Name", foundProjectRepresentation.getName());
	}

	public void thePersistedProjectRepresentationShouldKeepTheGivenId() throws Exception {
		final ProjectRepresentation projectRepresentation = new ProjectRepresentation("Name");

		persistenceService.persistOrUpdateProjectRepresentation(projectRepresentation);

		final ProjectRepresentation foundProjectRepresentation = persistenceService.retrieveProjectRepresentation(projectRepresentation.getId());
		assertEquals("Name", foundProjectRepresentation.getName());
		assertEquals(projectRepresentation.getId(), foundProjectRepresentation.getId());
	}

	@Test
	public void shouldBeAbleToFindAllProjectRepresentations() throws Exception {
		assertEquals(1, persistenceService.retrieveAllProjectRepresentations().size());

		final ArrayList<ProjectRepresentation> projectRepresentations = new ArrayList<ProjectRepresentation>();
		for (int i = 2; i <= 11; i++) {
			final ProjectRepresentation projectRepresentation = new ProjectRepresentation(new UUID(), "Name" + i);
			persistenceService.persistOrUpdateProjectRepresentation(projectRepresentation);
			projectRepresentations.add(projectRepresentation);
			assertEquals(i, persistenceService.retrieveAllProjectRepresentations().size());
		}
		assertTrue(persistenceService.retrieveAllProjectRepresentations().containsAll(projectRepresentations));
	}

	@Test
	public void shouldBeAbleToFindAllProjectAuthorizationsThatAGivenUserIsRelatedTo() throws Exception {
		final UUID user = createAndPersistUser();
		final ProjectRepresentation project1 = createProjectRepresentation("project1");
		createProjectRepresentation("project2");
		final ProjectRepresentation project3 = createProjectRepresentation("project3");

		authorize(user, project1, project3);

		final List<ProjectAuthorization> authorizations = persistenceService.retrieveProjectAuthorizations(user);

		final List<ProjectRepresentation> obtainedProjectAuthorizations = extractProjectsFromAuthorization(authorizations);
		assertTrue(obtainedProjectAuthorizations.contains(project1));
		assertTrue(obtainedProjectAuthorizations.contains(project3));
	}

	@Test
	public void shouldBeAbleToRetrieveProjectAuthorizationForSpecificUserAndProject() throws Exception {
		final UUID user1 = createAndPersistUser();
		final UUID user2 = createAndPersistUser();
		final ProjectRepresentation project1 = createProjectRepresentation("project1");
		final ProjectRepresentation project2 = createProjectRepresentation("project2");

		authorize(user1, project1);
		authorize(user2, project2);

		assertNotNull(persistenceService.retrieveProjectAuthorization(user1, project1.getId()));
		assertNotNull(persistenceService.retrieveProjectAuthorization(user2, project2.getId()));

		assertNull(persistenceService.retrieveProjectAuthorization(user1, project2.getId()));
		assertNull(persistenceService.retrieveProjectAuthorization(user2, project1.getId()));
	}

	private List<ProjectRepresentation> extractProjectsFromAuthorization(final List<ProjectAuthorization> authorizations) throws PersistenceException, NoResultFoundException {
		final List<ProjectRepresentation> projects = new ArrayList<ProjectRepresentation>();
		for (final ProjectAuthorization authorization : authorizations) {
			projects.add(persistenceService.retrieveProjectRepresentation(authorization.getProjectId()));
		}
		return projects;
	}

	@Test(expected = PersistenceException.class)
	public void inexistentUserCannotBeAuthorized() throws Exception {
		persistenceService.authorize("inexistent@email.com", PROJECT_ID);
	}

	@Test(expected = PersistenceException.class)
	public void inexistentProjectCannotBeAuthorized() throws Exception {
		final UUID user = createAndPersistUser();
		persistenceService.authorize(user, new UUID());
	}

	@Test
	public void cannotAuthorizeAUserToProjectTwice() throws Exception {
		final UUID user = createAndPersistUser();
		final ProjectRepresentation project = persistenceService.retrieveProjectRepresentation(PROJECT_ID);

		boolean reached = false;
		try {
			persistenceService.authorize(user, project.getId());
			reached = true;
			persistenceService.authorize(user, project.getId());
			fail();
		} catch (final PersistenceException e) {
			assertTrue(reached);
		}

	}

	@Test
	public void shouldBeAbleToPersistFileRepresentations() throws Exception {
		final FileRepresentation fileRepresentation = new FileRepresentation(new UUID(), "fileName", "fileHash", new UUID());
		persistenceService.persistOrUpdateFileRepresentation(fileRepresentation);
	}

	@Test
	public void shouldBeAbleToRetrieveFileRepresentations() throws Exception {
		final FileRepresentation fileRepresentation = new FileRepresentation(new UUID(), "fileName", "fileHash", new UUID());
		persistenceService.persistOrUpdateFileRepresentation(fileRepresentation);

		final FileRepresentation retrievedFileRepresentation = persistenceService.retrieveFileRepresentationById(fileRepresentation.getId());
		DeepEqualityTestUtils.assertObjectEquality(fileRepresentation, retrievedFileRepresentation);
	}

	@Test
	public void shouldPersistAndRetrieveSingleUserNotification() throws Exception {
		final UUID user = createAndPersistUser();
		final Notification notification = getBuilder("msg").addReceipient(user).getNotification();

		persistenceService.persistOrUpdateNotification(notification);

		final List<Notification> latestNotificationsForUser = persistenceService.retrieveLatestNotificationsForUser(user, 50);
		assertEquals(1, latestNotificationsForUser.size());
		DeepEqualityTestUtils.assertObjectEquality(notification, latestNotificationsForUser.get(0));
	}

	private NotificationBuilder getBuilder(final String desc) throws Exception {
		final ProjectRepresentation project = ProjectTestUtils.createRepresentation(new UUID(""));

		persistenceService.persistOrUpdateProjectRepresentation(project);

		if (author == null) author = createAndPersistUser();
		return new NotificationBuilder(NotificationType.IMPEDIMENT_CREATED, project, author).setDescription(desc);
	}

	@Test
	public void shouldPersistAndRetrieveSingleUserNotificationWhenThereIsMoreNotifications() throws Exception {
		final UUID user1 = createAndPersistUser();
		final Notification notification1 = getBuilder("msg1").addReceipient(user1).getNotification();
		persistenceService.persistOrUpdateNotification(notification1);

		final UUID user2 = createAndPersistUser();
		final Notification notification2 = getBuilder("msg2").addReceipient(user2).getNotification();
		persistenceService.persistOrUpdateNotification(notification2);

		final List<Notification> latestNotificationsForUser = persistenceService.retrieveLatestNotificationsForUser(user1, 50);
		assertEquals(1, latestNotificationsForUser.size());
		DeepEqualityTestUtils.assertObjectEquality(notification1, latestNotificationsForUser.get(0));
	}

	@Test
	public void shouldPersistAndRetrieveMultipleUserNotifications() throws Exception {
		final UUID user1 = createAndPersistUser();
		final Notification notification1 = getBuilder("msg1").addReceipient(user1).getNotification();
		persistenceService.persistOrUpdateNotification(notification1);

		final UUID user2 = createAndPersistUser();
		final Notification notification2 = getBuilder("msg2").addReceipient(user2).getNotification();
		persistenceService.persistOrUpdateNotification(notification2);

		final UUID user3 = createAndPersistUser();
		final Notification notification3 = getBuilder("msg3").addReceipient(user3).addReceipient(user1).getNotification();
		persistenceService.persistOrUpdateNotification(notification3);

		final List<Notification> latestNotificationsForUser = persistenceService.retrieveLatestNotificationsForUser(user1, 50);
		assertEquals(2, latestNotificationsForUser.size());
		DeepEqualityTestUtils.assertObjectEquality(notification3, latestNotificationsForUser.get(0));
		DeepEqualityTestUtils.assertObjectEquality(notification1, latestNotificationsForUser.get(1));
	}

	@Test
	public void shouldPersistAndRetrieveMultipleUserNotificationsInTheCorrectOrder() throws Exception {
		final UUID user1 = createAndPersistUser();
		final Notification notification1 = getBuilder("msg1").addReceipient(user1).setTimestamp(new Date(1)).getNotification();
		persistenceService.persistOrUpdateNotification(notification1);

		final UUID user2 = createAndPersistUser();
		final Notification notification2 = getBuilder("msg2").addReceipient(user1).addReceipient(user2).setTimestamp(new Date(1000)).getNotification();
		persistenceService.persistOrUpdateNotification(notification2);

		final UUID user3 = createAndPersistUser();
		final Notification notification3 = getBuilder("msg3").addReceipient(user3).addReceipient(user1).setTimestamp(new Date(100)).getNotification();
		persistenceService.persistOrUpdateNotification(notification3);

		final List<Notification> latestNotificationsForUser = persistenceService.retrieveLatestNotificationsForUser(user1, 50);

		assertEquals(3, latestNotificationsForUser.size());
		DeepEqualityTestUtils.assertObjectEquality(notification2, latestNotificationsForUser.get(0));
		DeepEqualityTestUtils.assertObjectEquality(notification3, latestNotificationsForUser.get(1));
		DeepEqualityTestUtils.assertObjectEquality(notification1, latestNotificationsForUser.get(2));
	}

	@Test
	public void shouldPersistAndRetrieveMultipleUserNotificationsLimitedByMaxRequested() throws Exception {
		final UUID user1 = createAndPersistUser();
		final Notification notification1 = getBuilder("msg1").addReceipient(user1).setTimestamp(new Date(1)).getNotification();
		persistenceService.persistOrUpdateNotification(notification1);

		final UUID user2 = createAndPersistUser();
		final Notification notification2 = getBuilder("msg2").addReceipient(user1).addReceipient(user2).setTimestamp(new Date(1000)).getNotification();
		persistenceService.persistOrUpdateNotification(notification2);

		final UUID user3 = createAndPersistUser();
		final Notification notification3 = getBuilder("msg3").addReceipient(user3).addReceipient(user1).setTimestamp(new Date(100)).getNotification();
		persistenceService.persistOrUpdateNotification(notification3);

		final List<Notification> latestNotificationsForUser = persistenceService.retrieveLatestNotificationsForUser(user1, 2);

		assertEquals(2, latestNotificationsForUser.size());
		DeepEqualityTestUtils.assertObjectEquality(notification2, latestNotificationsForUser.get(0));
		DeepEqualityTestUtils.assertObjectEquality(notification3, latestNotificationsForUser.get(1));
	}

	@Test
	public void shouldPersistAndRetrieveLatestNotificationsLimitedByDate() throws Exception {
		final UUID user1 = createAndPersistUser();
		final Notification notification1 = getBuilder("msg1").addReceipient(user1).setTimestamp(new Date()).getNotification();
		persistenceService.persistOrUpdateNotification(notification1);

		final UUID user2 = createAndPersistUser();
		final Notification notification2 = getBuilder("msg2").addReceipient(user1).addReceipient(user2).setTimestamp(getInitialFetchDate(1)).getNotification();
		persistenceService.persistOrUpdateNotification(notification2);

		final UUID user3 = createAndPersistUser();
		final Notification notification3 = getBuilder("msg3").addReceipient(user3).addReceipient(user1).setTimestamp(getInitialFetchDate(3)).getNotification();
		persistenceService.persistOrUpdateNotification(notification3);

		final List<Notification> latestNotificationsForUser = persistenceService.retrieveLatestNotifications(getInitialFetchDate(2));

		assertEquals(2, latestNotificationsForUser.size());
		DeepEqualityTestUtils.assertObjectEquality(notification1, latestNotificationsForUser.get(0));
		DeepEqualityTestUtils.assertObjectEquality(notification2, latestNotificationsForUser.get(1));
	}

	@Test
	public void shouldPersistAndRetrieveLatestNotificationsLimitedByDateAndProject() throws Exception {
		final ProjectRepresentation projectRepresentation1 = ProjectTestUtils.createRepresentation(new UUID("1"));
		final ProjectRepresentation projectRepresentation2 = ProjectTestUtils.createRepresentation(new UUID("2"));
		final ProjectRepresentation projectRepresentation3 = ProjectTestUtils.createRepresentation(new UUID("3"));

		final UUID user1 = createAndPersistUser();
		final Notification notification1 = getBuilder("msg1").addReceipient(user1).setTimestamp(new Date()).setProjectRepresentation(projectRepresentation3).getNotification();
		persistenceService.persistOrUpdateNotification(notification1);

		final UUID user2 = createAndPersistUser();
		final Notification notification2 = getBuilder("msg2").addReceipient(user1).addReceipient(user2).setTimestamp(getInitialFetchDate(1)).setProjectRepresentation(projectRepresentation2)
				.getNotification();
		persistenceService.persistOrUpdateNotification(notification2);

		final UUID user3 = createAndPersistUser();
		final Notification notification3 = getBuilder("msg3").addReceipient(user3).addReceipient(user1).setTimestamp(getInitialFetchDate(3)).setProjectRepresentation(projectRepresentation1)
				.getNotification();
		persistenceService.persistOrUpdateNotification(notification3);

		final UUID user4 = createAndPersistUser();
		final Notification notification4 = getBuilder("msg4").addReceipient(user4).addReceipient(user1).setTimestamp(getInitialFetchDate(0)).setProjectRepresentation(projectRepresentation1)
				.getNotification();
		persistenceService.persistOrUpdateNotification(notification4);

		final List<UUID> list = Arrays.asList(new UUID[] { projectRepresentation2.getId(), projectRepresentation1.getId() });
		final List<Notification> latestNotificationsForUser = persistenceService.retrieveLatestProjectNotifications(list, getInitialFetchDate(2));

		assertEquals(2, latestNotificationsForUser.size());
		DeepEqualityTestUtils.assertObjectEquality(notification4, latestNotificationsForUser.get(0));
		DeepEqualityTestUtils.assertObjectEquality(notification2, latestNotificationsForUser.get(1));
	}

	private Date getInitialFetchDate(final int numberOfMonthsAgo) {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -numberOfMonthsAgo);
		return cal.getTime();
	}

	@Test
	public void shouldRetrieveMultipleUsers() throws Exception {
		final UUID user1 = createAndPersistUser();
		createAndPersistUser();
		final UUID user2 = createAndPersistUser();
		createAndPersistUser();

		final List<UUID> userIds = new ArrayList<UUID>();
		userIds.add(user1);
		userIds.add(user2);

		final List<User> retrievedUsers = persistenceService.retrieveUsersByIds(userIds);

		assertEquals(2, retrievedUsers.size());

		for (final User user : retrievedUsers) {
			assertTrue(userIds.contains(user.getId()));
		}

	}

	private List<UserAction> toUserActionsList(final List<ModelAction> modelActions) {
		return UserActionTestUtils.create(modelActions, UserTestUtils.getAdmin().getId(), PROJECT_ID);
	}

	private UUID createAndPersistUser() throws Exception {
		final User user = persistenceService.persistOrUpdateUser(UserTestUtils.createUser());
		return user.getId();
	}

	private void authorize(final UUID user, final ProjectRepresentation... projects) throws PersistenceException {
		for (final ProjectRepresentation project : projects) {
			persistenceService.authorize(user, project.getId());
		}
	}

	private ProjectRepresentation createProjectRepresentation(final String projectName) throws PersistenceException {
		return persistenceService.persistOrUpdateProjectRepresentation(ProjectTestUtils.createRepresentation(projectName));
	}

	private ProjectSnapshot loadProjectSnapshot() throws PersistenceException, UnableToLoadProjectException, ProjectNotFoundException {
		ProjectSnapshot snapshot;
		try {
			snapshot = persistenceService.retrieveProjectSnapshot(PROJECT_ID);
		} catch (final NoResultFoundException e) {
			snapshot = createBlankProject();
		}
		return snapshot;
	}

	private ProjectSnapshot createBlankProject() throws UnableToLoadProjectException, ProjectNotFoundException {
		final Scope projectScope = ScopeTestUtils.createScope("Project", UUID.INVALID_UUID);
		final Release projectRelease = new Release("proj", new UUID("release0"));

		try {
			final ProjectSnapshot projectSnapshot = new ProjectSnapshot(ProjectTestUtils.createProject(persistenceService.retrieveProjectRepresentation(PROJECT_ID), projectScope, projectRelease),
					new Date(0));
			return projectSnapshot;
		} catch (final IOException e) {
			throw new UnableToLoadProjectException("It was not possible to create a blank project.");
		} catch (final PersistenceException e) {
			throw new UnableToLoadProjectException("It was not possible to create a blank project, because the project with id '" + PROJECT_ID + "' was not found.");
		} catch (final NoResultFoundException e) {
			throw new ProjectNotFoundException("It was not possible to create a blank project, because the project representation with id '" + PROJECT_ID + "' was not found.");
		}
	}

	private void assureProjectRepresentationExistance() throws Exception {
		persistenceService.persistOrUpdateProjectRepresentation(ProjectTestUtils.createRepresentation(PROJECT_ID));
	}
}
