package br.com.oncast.ontrack.server.services.exportImport.xml;

import static br.com.oncast.ontrack.utils.assertions.AssertTestUtils.assertCollectionEquality;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import br.com.oncast.ontrack.server.model.project.UserAction;
import br.com.oncast.ontrack.server.services.authentication.Password;
import br.com.oncast.ontrack.server.services.exportImport.xml.abstractions.OntrackMigrationManager;
import br.com.oncast.ontrack.server.services.exportImport.xml.abstractions.OntrackXML;
import br.com.oncast.ontrack.server.services.exportImport.xml.abstractions.ProjectAuthorizationXMLNode;
import br.com.oncast.ontrack.server.services.exportImport.xml.abstractions.ProjectXMLNode;
import br.com.oncast.ontrack.server.services.exportImport.xml.abstractions.UserXMLNode;
import br.com.oncast.ontrack.server.services.persistence.exceptions.PersistenceException;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.ProjectAuthorization;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.utils.deepEquality.DeepEqualityTestUtils;
import br.com.oncast.ontrack.utils.mocks.xml.XMLNodeTestUtils;

public class XMLWriterTest {

	private static final String ONTRACK_XML = "ontrack.xml";
	private XMLWriter xmlExporter;
	private List<User> userList;
	private List<Password> passwordList;
	private String version;

	@Before
	public void setUp() throws Exception {
		deleteGeneratedXMLFile();

		xmlExporter = new XMLWriter();
		version = OntrackMigrationManager.getCurrentVersion();
		userList = UserActionTestUtils.createUserList();
		passwordList = UserActionTestUtils.createPasswordList();
	}

	@After
	public void tearDown() {
		// deleteGeneratedXMLFile();
	}

	public void deleteGeneratedXMLFile() {
		final File generatedXML = new File(ONTRACK_XML);
		if (generatedXML.exists()) generatedXML.delete();
	}

	@Test
	public void shouldWriteUsersAndPasswordToXML() throws Exception {
		testWithActionList(new ArrayList<UserAction>());
	}

	@Test
	public void shouldWriteUsersPasswordAndReleaseRemoveActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createReleaseRemoveAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndReleaseScopeUpdatePriorityActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createReleaseScopeUpdatePriorityAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndReleaseUpdatePriorityActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createReleaseUpdatePriorityAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeDeclareEffortActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeDeclareEffortAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeDeclareProgressActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeDeclareProgressAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeMoveUpActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeMoveUpAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeMoveDownActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeMoveDownAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeUpdateActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeUpdateAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndReleaseCreateActionDefaultToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createReleaseCreateActionDefault()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeBindReleaseActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeBindReleaseAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeInsertChildRollbackActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeInsertChildRollbackAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndReleaseRemoveRollbackActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createReleaseRemoveRollbackAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeInsertParentRollbackActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeInsertParentRollbackAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeInsertSiblingDownRollbackActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeInsertSiblingDownRollbackAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeInsertSiblingUpRollbackActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeInsertSiblingUpRollbackAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeRemoveActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeRemoveAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeInsertChildActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeInsertChildAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeInsertParentActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeInsertParentAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeRemoveRollbackActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeRemoveRollbackAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeInsertSiblingDownActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeInsertSiblingDownAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeInsertSiblingUpActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeInsertSiblingUpAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeMoveLeftActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeMoveLeftAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndScopeMoveRightActionToXML() throws Exception {
		testWithActionList(asList(UserActionTestUtils.createScopeMoveRightAction()));
	}

	@Test
	public void shouldWriteUsersPasswordAndActionsToXML() throws Exception {
		testWithActionList(UserActionTestUtils.createCompleteUserActionList());
	}

	@Test
	public void shouldWriteActionsSeparatedByProjectsToXML() throws Exception {
		final ArrayList<UserAction> actionList = new ArrayList<UserAction>();

		actionList.addAll(UserActionTestUtils.createRandomUserActionList(1, "Project 1"));
		actionList.addAll(UserActionTestUtils.createRandomUserActionList(2, "Project 2"));
		actionList.addAll(UserActionTestUtils.createRandomUserActionList(3, "Project 3"));

		assertEquality(actionList, generateXMLAndReadWithCustomActions(actionList));
	}

	@Test
	public void shouldWriteActionsToXMLInTheSameOrderTheyArePassed() throws Exception {
		final ArrayList<UserAction> actionList = new ArrayList<UserAction>();
		actionList.addAll(UserActionTestUtils.createCompleteUserActionListOrderedById());

		final OntrackXML xml = generateXMLAndReadWithCustomActions(actionList);

		final ProjectXMLNode project = xml.getProjects().get(0);
		for (int i = 0; i < project.getActions().size(); i++) {
			assertEquals(actionList.get(i).getTimestamp(), project.getActions().get(i).getTimestamp());
		}
	}

	@Test
	public void shouldWriteProjectAuthorizationsToXML() throws Exception {
		final List<ProjectXMLNode> projectNodes = XMLNodeTestUtils.createProjectNodes(2);
		final List<UserXMLNode> userNodes = XMLNodeTestUtils.createUserNodes(3);
		final List<ProjectAuthorizationXMLNode> authNodes = XMLNodeTestUtils.createAuthorizationNodes(projectNodes, userNodes);

		final OntrackXML ontrackXML = generateXMLAndRead(projectNodes, userNodes, authNodes);

		assertCollectionEquality(authNodes, ontrackXML.getProjectAuthorizations());
	}

	@Test
	@Ignore("Run this everytime you write a migration and want to generate a xml file with data simulating an export. Remember to comment the deletion of generated file inside @After method.")
	public void generateCompleteXML() throws Exception {
		final ArrayList<UserAction> actionList = new ArrayList<UserAction>();
		actionList.addAll(UserActionTestUtils.createCompleteUserActionListOrderedById());

		generateXMLAndReadWithCustomActions(actionList);
	}

	private List<ProjectXMLNode> separateByProject(final List<UserAction> userActions) {
		final Map<ProjectRepresentation, List<UserAction>> map = new HashMap<ProjectRepresentation, List<UserAction>>();
		for (final UserAction userAction : userActions) {
			final ProjectRepresentation projectRepresentation = userAction.getProjectRepresentation();
			if (!map.containsKey(projectRepresentation)) map.put(projectRepresentation, new ArrayList<UserAction>());
			map.get(projectRepresentation).add(userAction);
		}
		final List<ProjectXMLNode> list = new ArrayList<ProjectXMLNode>();
		for (final ProjectRepresentation projectRepresentation : map.keySet()) {
			list.add(new ProjectXMLNode(projectRepresentation, map.get(projectRepresentation)));
		}
		return list;
	}

	private void testWithActionList(final List<UserAction> actionList) throws Exception {
		final OntrackXML ontrackXML = generateXMLAndReadWithCustomActions(actionList);
		assertEquality(actionList, ontrackXML);
	}

	private void assertEquality(final List<UserAction> actionList, final OntrackXML ontrackXML) {
		assertEquals(version, ontrackXML.getVersion());
		final List<UserXMLNode> userXMLNodes = ontrackXML.getUsers();
		assertEquals(userList.size(), userXMLNodes.size());
		for (final UserXMLNode userXMLNode : userXMLNodes) {
			assertContainsUser(userXMLNode.getUser());
			assertContainsPassword(userXMLNode.getPassword());
		}

		final List<ProjectXMLNode> projects = separateByProject(actionList);
		DeepEqualityTestUtils.assertObjectEquality(projects, ontrackXML.getProjects());
	}

	private void assertContainsUser(final User user) {
		boolean contains = false;
		for (final User userFromList : userList) {
			if (userFromList.getEmail().equals(user.getEmail())) {
				contains = true;
				break;
			}
		}
		assertTrue(contains);
	}

	private void assertContainsPassword(final Password password) {
		boolean contains = false;
		for (final Password passwordFromList : passwordList) {
			if (passwordFromList.getPasswordHash().equals(password.getPasswordHash()) &&
					passwordFromList.getPasswordSalt().equals(password.getPasswordSalt())) {
				contains = true;
				break;
			}
		}
		assertTrue(contains);
	}

	private OntrackXML generateXMLAndReadWithCustomActions(final List<UserAction> actionList) throws Exception {
		final List<ProjectXMLNode> projects = separateByProject(actionList);
		final List<UserXMLNode> users = getAllUsersWithPassword();
		final List<ProjectAuthorizationXMLNode> authorizations = extractAuthorizations(users, projects);

		return generateXMLAndRead(projects, users, authorizations);
	}

	private OntrackXML generateXMLAndRead(final List<ProjectXMLNode> projectNodes, final List<UserXMLNode> userNodes,
			final List<ProjectAuthorizationXMLNode> authorizationNodes) throws FileNotFoundException, Exception {

		final File ontrackFile = new File(ONTRACK_XML);

		xmlExporter
				.setVersion(version)
				.setUserList(userNodes)
				.setProjectList(projectNodes)
				.setProjectAuthorizationList(authorizationNodes)
				.export(new FileOutputStream(ontrackFile));

		final Serializer serializer = new Persister();
		final File source = new File(ONTRACK_XML);

		final OntrackXML ontrackXML = serializer.read(OntrackXML.class, source);
		return ontrackXML;
	}

	private List<ProjectAuthorizationXMLNode> extractAuthorizations(final List<UserXMLNode> users, final List<ProjectXMLNode> projects) {
		final List<ProjectAuthorizationXMLNode> auth = new ArrayList<ProjectAuthorizationXMLNode>();
		for (final ProjectXMLNode project : projects) {
			for (final UserXMLNode user : users) {
				final User entity = new User(user.getUser().getEmail());
				entity.setId(user.getId());
				auth.add(new ProjectAuthorizationXMLNode(new ProjectAuthorization(entity, project.getProjectRepresentation())));
			}
		}
		return auth;
	}

	private List<UserXMLNode> getAllUsersWithPassword() {
		final List<UserXMLNode> userXMLNodeList = new ArrayList<UserXMLNode>();

		try {
			final List<User> users = userList;
			for (final User user : users) {
				userXMLNodeList.add(associatePasswordTo(user));
			}
		}
		catch (final PersistenceException e) {
			e.printStackTrace();
		}

		return userXMLNodeList;
	}

	private UserXMLNode associatePasswordTo(final User user) throws PersistenceException {
		final UserXMLNode userXMLNode = XMLNodeTestUtils.createUserNode(user);
		userXMLNode.setPassword(getPasswordFor(user));
		return userXMLNode;
	}

	private Password getPasswordFor(final User user) {
		for (final Password password : passwordList) {
			if (password.getUserId() == user.getId()) return password;
		}
		return null;
	}

	private List<UserAction> asList(final UserAction userAction) throws Exception {
		final List<UserAction> userActions = new ArrayList<UserAction>();
		userActions.add(userAction);
		return userActions;
	}
}
