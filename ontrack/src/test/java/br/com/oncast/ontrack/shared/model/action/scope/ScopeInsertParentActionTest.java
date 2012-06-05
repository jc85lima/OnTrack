package br.com.oncast.ontrack.shared.model.action.scope;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope.ScopeInsertParentActionEntity;
import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ModelActionTest;
import br.com.oncast.ontrack.shared.model.action.ScopeAction;
import br.com.oncast.ontrack.shared.model.action.ScopeInsertParentAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.release.ReleaseFactoryTestUtil;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.utils.mocks.models.ProjectTestUtils;

public class ScopeInsertParentActionTest extends ModelActionTest {

	private Scope rootScope;
	private Scope childScope;
	private ProjectContext context;
	private String newScopeDescription;
	private String newReleaseDescription;

	@Before
	public void setUp() {
		rootScope = new Scope("root");
		childScope = new Scope("child");
		rootScope.add(childScope);

		newScopeDescription = "description for new scope";
		newReleaseDescription = "Release1";

		context = ProjectTestUtils.createProjectContext(rootScope, ReleaseFactoryTestUtil.create(""));
	}

	@Test
	public void mustInsertAScopeAsFather() throws UnableToCompleteActionException {
		assertEquals(childScope.getParent(), rootScope);
		assertEquals(rootScope.getChildren().get(0), childScope);

		new ScopeInsertParentAction(childScope.getId(), newScopeDescription).execute(context, Mockito.mock(ActionContext.class));

		assertEquals(childScope.getParent().getDescription(), newScopeDescription);
		assertEquals(rootScope.getChildren().get(0).getDescription(), newScopeDescription);
	}

	@Test(expected = UnableToCompleteActionException.class)
	public void insertingFatherAtRootNodeMustThrowException() throws UnableToCompleteActionException {
		new ScopeInsertParentAction(rootScope.getId(), newScopeDescription).execute(context, Mockito.mock(ActionContext.class));
	}

	@Test
	public void rollbackMustRevertExecuteChanges() throws UnableToCompleteActionException {
		final ScopeInsertParentAction insertFatherScopeAction = new ScopeInsertParentAction(childScope.getId(), newScopeDescription);
		final ScopeAction rollbackAction = insertFatherScopeAction.execute(context, Mockito.mock(ActionContext.class));

		assertEquals(childScope.getParent().getDescription(), newScopeDescription);
		assertEquals(rootScope.getChildren().get(0).getDescription(), newScopeDescription);

		rollbackAction.execute(context, Mockito.mock(ActionContext.class));

		assertEquals(childScope.getParent(), rootScope);
		assertEquals(rootScope.getChildren().get(0), childScope);
	}

	@Test
	public void mustAssociateScopeWithARelease() throws UnableToCompleteActionException {
		new ScopeInsertParentAction(childScope.getId(), newScopeDescription + " @" + newReleaseDescription).execute(context, Mockito.mock(ActionContext.class));

		assertEquals(childScope.getParent().getDescription(), newScopeDescription);
		assertEquals(childScope.getParent().getRelease().getDescription(), newReleaseDescription);
		assertEquals(rootScope.getChildren().get(0).getDescription(), newScopeDescription);
	}

	@Test
	public void mustDisassociateScopeFromReleaseAfterUndo() throws UnableToCompleteActionException {
		final ScopeInsertParentAction insertFatherScopeAction = new ScopeInsertParentAction(childScope.getId(), newScopeDescription + " @"
				+ newReleaseDescription);
		final ScopeAction rollbackAction = insertFatherScopeAction.execute(context, Mockito.mock(ActionContext.class));

		final Scope insertedParent = childScope.getParent();
		final Release release = insertedParent.getRelease();

		assertEquals(release.getDescription(), newReleaseDescription);
		assertTrue(release.getScopeList().contains(insertedParent));
		assertEquals(insertedParent.getDescription(), newScopeDescription);
		assertEquals(rootScope.getChildren().get(0).getDescription(), newScopeDescription);

		rollbackAction.execute(context, Mockito.mock(ActionContext.class));

		assertEquals(childScope.getParent(), rootScope);
		assertEquals(rootScope.getChildren().get(0), childScope);
		assertFalse(release.getScopeList().contains(insertedParent));
	}

	@Override
	protected Class<? extends ModelActionEntity> getEntityType() {
		return ScopeInsertParentActionEntity.class;
	}

	@Override
	protected Class<? extends ModelAction> getActionType() {
		return ScopeInsertParentAction.class;
	}

	@Override
	protected ModelAction getInstance() {
		return new ScopeInsertParentAction(new UUID(), "");
	}
}