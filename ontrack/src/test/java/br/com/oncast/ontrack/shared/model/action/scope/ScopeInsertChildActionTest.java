package br.com.oncast.ontrack.shared.model.action.scope;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.scope.ScopeInsertChildActionEntity;
import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ModelActionTest;
import br.com.oncast.ontrack.shared.model.action.ScopeInsertChildAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.progress.Progress.ProgressState;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.release.ReleaseFactoryTestUtil;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.services.actionExecution.ActionExecuterTestUtils;
import br.com.oncast.ontrack.utils.mocks.models.ProjectTestUtils;

public class ScopeInsertChildActionTest extends ModelActionTest {

	private Scope selectedScope;
	private ProjectContext context;
	private String newScopeDescription;
	private String newReleaseDescription;

	@Before
	public void setUp() {
		selectedScope = new Scope("root");
		selectedScope.add(new Scope("first child"));

		newScopeDescription = "description for new scope";
		newReleaseDescription = "Release1";

		context = ProjectTestUtils.createProjectContext(selectedScope, ReleaseFactoryTestUtil.create(""));
	}

	@Test
	public void mustInsertNewChild() throws UnableToCompleteActionException {
		assertEquals(selectedScope.getChildren().size(), 1);

		new ScopeInsertChildAction(selectedScope.getId(), newScopeDescription).execute(context, Mockito.mock(ActionContext.class));

		assertEquals(selectedScope.getChildren().size(), 2);
		assertEquals(selectedScope.getChildren().get(1).getDescription(), newScopeDescription);
	}

	@Test
	public void theInsertedChildMustBeTheLastChild() throws UnableToCompleteActionException {
		new ScopeInsertChildAction(selectedScope.getId(), newScopeDescription).execute(context, Mockito.mock(ActionContext.class));

		assertFalse(selectedScope.getChildren().get(0).getDescription().equals(newScopeDescription));
		assertEquals(newScopeDescription, selectedScope.getChildren().get(1).getDescription());
	}

	@Test
	public void rollbackMustRevertExecuteChanges() throws UnableToCompleteActionException {
		final ScopeInsertChildAction insertChildScopeAction = new ScopeInsertChildAction(selectedScope.getId(), newScopeDescription);
		final ModelAction rollbackAction = insertChildScopeAction.execute(context, Mockito.mock(ActionContext.class));

		assertEquals(2, selectedScope.getChildren().size());

		rollbackAction.execute(context, Mockito.mock(ActionContext.class));

		assertEquals(1, selectedScope.getChildren().size());
	}

	@Test
	public void mustAssociateScopeWithARelease() throws UnableToCompleteActionException {
		new ScopeInsertChildAction(selectedScope.getId(), newScopeDescription + " @" + newReleaseDescription).execute(context, Mockito.mock(ActionContext.class));

		assertEquals(selectedScope.getChildren().get(1).getDescription(), newScopeDescription);
		assertEquals(selectedScope.getChildren().get(1).getRelease().getDescription(), newReleaseDescription);
	}

	@Test
	public void mustDisassociateScopeFromReleaseAfterUndo() throws UnableToCompleteActionException {
		final ScopeInsertChildAction insertChildAction = new ScopeInsertChildAction(selectedScope.getId(), newScopeDescription + " @"
				+ newReleaseDescription);
		final ModelAction rollbackAction = insertChildAction.execute(context, Mockito.mock(ActionContext.class));

		final Scope insertedScope = selectedScope.getChildren().get(1);
		final Release release = insertedScope.getRelease();

		assertEquals(release.getDescription(), newReleaseDescription);
		assertTrue(release.getScopeList().contains(insertedScope));
		assertEquals(insertedScope.getDescription(), newScopeDescription);

		rollbackAction.execute(context, Mockito.mock(ActionContext.class));

		assertEquals(selectedScope.getChildren().size(), 1);
		assertFalse(release.getScopeList().contains(insertedScope));
	}

	@Test
	public void rollbackMustGiveBackTheProgressStateToParentIfItWasLeaf() throws UnableToCompleteActionException {
		final Scope parent = selectedScope.getChild(0);
		parent.getProgress().setDescription("Done");
		ActionExecuterTestUtils.executeInferenceEnginesForTestingPurposes(selectedScope);

		assertTrue(parent.getProgress().isDone());
		assertTrue(parent.getProgress().hasDeclared());
		assertEquals(ProgressState.DONE, parent.getProgress().getState());

		final ScopeInsertChildAction insertChildAction = new ScopeInsertChildAction(parent.getId(), newScopeDescription);
		final ModelAction rollbackAction = insertChildAction.execute(context, Mockito.mock(ActionContext.class));
		ActionExecuterTestUtils.executeInferenceEnginesForTestingPurposes(parent);

		assertTrue(parent.getProgress().hasDeclared());
		assertTrue(parent.getProgress().isDone());

		rollbackAction.execute(context, Mockito.mock(ActionContext.class));
		ActionExecuterTestUtils.executeInferenceEnginesForTestingPurposes(parent);

		assertTrue(parent.getProgress().isDone());
		assertTrue(parent.getProgress().hasDeclared());
		assertEquals(ProgressState.DONE, parent.getProgress().getState());
	}

	@Override
	protected Class<? extends ModelActionEntity> getEntityType() {
		return ScopeInsertChildActionEntity.class;
	}

	@Override
	protected Class<? extends ModelAction> getActionType() {
		return ScopeInsertChildAction.class;
	}

	@Override
	protected ModelAction getInstance() {
		return new ScopeInsertChildAction(new UUID(), "");
	}

}