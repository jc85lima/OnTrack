package br.com.oncast.ontrack.shared.model.effort;

import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ScopeDeclareEffortAction;
import br.com.oncast.ontrack.shared.model.action.ScopeInsertChildAction;
import br.com.oncast.ontrack.shared.model.action.ScopeInsertSiblingDownAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.exceptions.ScopeNotFoundException;
import br.com.oncast.ontrack.shared.services.actionExecution.ActionExecuterTestUtils;
import br.com.oncast.ontrack.utils.deepEquality.DeepEqualityTestUtils;
import br.com.oncast.ontrack.utils.model.ProjectTestUtils;
import br.com.oncast.ontrack.utils.model.ReleaseTestUtils;
import br.com.oncast.ontrack.utils.model.UserTestUtils;

import java.util.Date;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

import static br.com.oncast.ontrack.shared.model.effort.EffortInferenceTestUtils.getModifiedScope;
import static br.com.oncast.ontrack.shared.model.effort.EffortInferenceTestUtils.getOriginalScope;

public class EffortInferenceEngineFlow6Test {

	private final String FILE_NAME_PREFIX = "Flow6";
	private Scope original = null;
	private ProjectContext projectContext;
	private final Stack<ModelAction> rollbackActions = new Stack<ModelAction>();
	private final Stack<ModelAction> redoActions = new Stack<ModelAction>();

	@Mock
	private ActionContext actionContext;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(actionContext.getUserId()).thenReturn(UserTestUtils.getAdmin().getId());
		when(actionContext.getTimestamp()).thenReturn(new Date(0));

		original = getOriginalScope(FILE_NAME_PREFIX);
		projectContext = ProjectTestUtils.createProjectContext(original, ReleaseTestUtils.createRelease("proj"));
		DeepEqualityTestUtils.setRequiredFloatingPointPrecision(0.1);

		executeAction(new ScopeDeclareEffortAction(original.getChild(0).getId(), true, 30));
		DeepEqualityTestUtils.assertObjectEquality(getModifiedScope(FILE_NAME_PREFIX, 1), original);
	}

	@Test
	public void testCaseStep01() throws UnableToCompleteActionException, ScopeNotFoundException {
		shouldDistributeTopDownEffortWhenAddingAChildToAnAlreadyDeclaredParent();
	}

	@Test
	public void testCaseStep02() throws UnableToCompleteActionException, ScopeNotFoundException {
		shouldDistributeTopDownEffortWhenAddingAChildToAnAlreadyDeclaredParent();
		shouldRemoveChildAfterUndo();
	}

	@Test
	public void testCaseStep03() throws UnableToCompleteActionException, ScopeNotFoundException {
		shouldDistributeTopDownEffortWhenAddingAChildToAnAlreadyDeclaredParent();
		shouldRemoveChildAfterUndo();
		shouldRedistributeTopDownEffortAfterRedo();
	}

	private void shouldDistributeTopDownEffortWhenAddingAChildToAnAlreadyDeclaredParent() throws UnableToCompleteActionException, ScopeNotFoundException {
		rollbackActions.push(executeAction(new ScopeInsertChildAction(original.getChild(0).getId(), "a1")));
		DeepEqualityTestUtils.assertObjectEquality(getModifiedScope(FILE_NAME_PREFIX, 2), original);

		rollbackActions.push(executeAction(new ScopeInsertSiblingDownAction(original.getChild(0).getChild(0).getId(), "a2")));
		DeepEqualityTestUtils.assertObjectEquality(getModifiedScope(FILE_NAME_PREFIX, 3), original);
	}

	private void shouldRemoveChildAfterUndo() throws UnableToCompleteActionException, ScopeNotFoundException {
		redoActions.push(executeAction(rollbackActions.pop()));
		DeepEqualityTestUtils.assertObjectEquality(getModifiedScope(FILE_NAME_PREFIX, 2), original);

		redoActions.push(executeAction(rollbackActions.pop()));
		DeepEqualityTestUtils.assertObjectEquality(getModifiedScope(FILE_NAME_PREFIX, 1), original);
	}

	private void shouldRedistributeTopDownEffortAfterRedo() throws UnableToCompleteActionException, ScopeNotFoundException {
		executeAction(redoActions.pop());
		DeepEqualityTestUtils.assertObjectEquality(getModifiedScope(FILE_NAME_PREFIX, 2), original);

		executeAction(redoActions.pop());
		DeepEqualityTestUtils.assertObjectEquality(getModifiedScope(FILE_NAME_PREFIX, 3), original);
	}

	private ModelAction executeAction(final ModelAction action) throws UnableToCompleteActionException, ScopeNotFoundException {
		final Scope effortInferenceBaseScopeForTestingPurposes = ActionExecuterTestUtils.getInferenceBaseScopeForTestingPurposes(projectContext, action);
		final ModelAction rollbackAction = action.execute(projectContext, actionContext);
		ActionExecuterTestUtils.executeInferenceEnginesForTestingPurposes(effortInferenceBaseScopeForTestingPurposes);
		return rollbackAction;
	}
}
