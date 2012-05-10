package br.com.oncast.ontrack.shared.model.action;

import org.junit.Before;
import org.junit.Test;

import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.kanban.KanbanColumnCreateActionEntity;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.actions.model.ModelActionEntity;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.progress.Progress;
import br.com.oncast.ontrack.shared.model.progress.Progress.ProgressState;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.utils.mocks.actions.ActionTestUtils;
import br.com.oncast.ontrack.utils.mocks.models.ProjectTestUtils;

public class KanbanColumnCreateActionTest extends ModelActionTest {

	private ProjectContext context;
	private Release release;
	private String newColumnDescription;

	@Before
	public void setUp() {
		context = new ProjectContext(ProjectTestUtils.createPopulatedProject());
		release = context.getProjectRelease().getChild(0);
		newColumnDescription = "Blabla";
	}

	@Test
	public void executionShouldCreateNewKanbanColumnNamedBlabla() throws UnableToCompleteActionException {
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 2, Progress.DEFAULT_NOT_STARTED_NAME, ProgressState.DONE.getDescription());
		new KanbanColumnCreateAction(release.getId(), newColumnDescription, true).execute(context);
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 3, Progress.DEFAULT_NOT_STARTED_NAME, newColumnDescription,
				ProgressState.DONE.getDescription());
	}

	@Test(expected = UnableToCompleteActionException.class)
	public void executionShouldFailWhenTryingToCreateNewKanbanColumnThatAlreadyExists() throws UnableToCompleteActionException {
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 2, Progress.DEFAULT_NOT_STARTED_NAME, ProgressState.DONE.getDescription());
		try {
			new KanbanColumnCreateAction(release.getId(), newColumnDescription, true).execute(context);
		}
		catch (final Exception e) {
			throw new RuntimeException();
		}
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 3, Progress.DEFAULT_NOT_STARTED_NAME, newColumnDescription,
				ProgressState.DONE.getDescription());
		new KanbanColumnCreateAction(release.getId(), newColumnDescription, true).execute(context);
	}

	@Test
	public void executionShouldCreateTwoNewKanbanColumns() throws UnableToCompleteActionException {
		final String newColumnDescription2 = "Blibli";

		ActionTestUtils.assertExpectedKanbanColumns(context, release, 2, Progress.DEFAULT_NOT_STARTED_NAME, ProgressState.DONE.getDescription());
		new KanbanColumnCreateAction(release.getId(), newColumnDescription, true).execute(context);
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 3, Progress.DEFAULT_NOT_STARTED_NAME, newColumnDescription,
				ProgressState.DONE.getDescription());
		new KanbanColumnCreateAction(release.getId(), newColumnDescription2, true).execute(context);
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 4, Progress.DEFAULT_NOT_STARTED_NAME, newColumnDescription, newColumnDescription2,
				ProgressState.DONE.getDescription());
	}

	@Test
	public void kanbanShouldHaveByDefaultAKanbanColumnForNotStartedAndForDone() throws UnableToCompleteActionException {
		ActionTestUtils.assertExpectedKanbanColumns(context, release, 2, Progress.DEFAULT_NOT_STARTED_NAME, ProgressState.DONE.getDescription());
	}

	@Test
	public void shouldBeAbleToInsertColumnInTheCorrectPosition() throws UnableToCompleteActionException {
		new KanbanColumnCreateAction(release.getId(), newColumnDescription, true).execute(context);
		ActionTestUtils.assertExpectedKanbanColumnPosition(context, release, newColumnDescription, 0);
	}

	@Test
	public void shouldBeAbleToInsertColumnsInTheCorrectPositions() throws UnableToCompleteActionException {
		final String columnDescription2 = newColumnDescription + "1";

		new KanbanColumnCreateAction(release.getId(), newColumnDescription, true).execute(context);
		new KanbanColumnCreateAction(release.getId(), columnDescription2, true).execute(context);

		ActionTestUtils.assertExpectedKanbanColumnPosition(context, release, newColumnDescription, 0);
		ActionTestUtils.assertExpectedKanbanColumnPosition(context, release, columnDescription2, 1);
	}

	@Test
	public void shouldBeAbleToInsertColumnsInTheDesiredPositions() throws UnableToCompleteActionException {
		final String columnDescription2 = newColumnDescription + "1";

		new KanbanColumnCreateAction(release.getId(), newColumnDescription, true).execute(context);
		new KanbanColumnCreateAction(release.getId(), columnDescription2, true, 0).execute(context);

		ActionTestUtils.assertExpectedKanbanColumnPosition(context, release, columnDescription2, 0);
		ActionTestUtils.assertExpectedKanbanColumnPosition(context, release, newColumnDescription, 1);
	}

	@Test
	public void shouldBeAbleToInsertColumnsInTheDesiredPositions1() throws UnableToCompleteActionException {
		final String columnDescription2 = newColumnDescription + "2";
		final String columnDescription3 = newColumnDescription + "3";

		new KanbanColumnCreateAction(release.getId(), newColumnDescription, true).execute(context);
		new KanbanColumnCreateAction(release.getId(), columnDescription2, true, 0).execute(context);
		new KanbanColumnCreateAction(release.getId(), columnDescription3, true, 1).execute(context);

		ActionTestUtils.assertExpectedKanbanColumnPosition(context, release, columnDescription2, 0);
		ActionTestUtils.assertExpectedKanbanColumnPosition(context, release, columnDescription3, 1);
		ActionTestUtils.assertExpectedKanbanColumnPosition(context, release, newColumnDescription, 2);
	}

	@Test
	public void shouldBeAbleToInsertColumnsInTheDesiredPositions2() throws UnableToCompleteActionException {
		final String columnDescription2 = newColumnDescription + "1";

		new KanbanColumnCreateAction(release.getId(), newColumnDescription, true, 0).execute(context);
		new KanbanColumnCreateAction(release.getId(), columnDescription2, true, 0).execute(context);

		ActionTestUtils.assertExpectedKanbanColumnPosition(context, release, columnDescription2, 0);
		ActionTestUtils.assertExpectedKanbanColumnPosition(context, release, newColumnDescription, 1);
	}

	@Test(expected = UnableToCompleteActionException.class)
	public void executionShouldFailWhenTryingToCreateNewKanbanColumnNamedDone() throws UnableToCompleteActionException {
		final String newColumnDescription = ProgressState.DONE.getDescription();
		new KanbanColumnCreateAction(release.getId(), newColumnDescription, true).execute(context);
	}

	@Test(expected = UnableToCompleteActionException.class)
	public void executionShouldFailWhenTryingToCreateNewKanbanColumnNamedNotStarted() throws UnableToCompleteActionException {
		final String newColumnDescription = Progress.DEFAULT_NOT_STARTED_NAME;
		new KanbanColumnCreateAction(release.getId(), newColumnDescription, true).execute(context);
	}

	@Test(expected = UnableToCompleteActionException.class)
	public void executionShouldFailWhenTryingToCreateNewKanbanColumnNamedBlank() throws UnableToCompleteActionException {
		final String newColumnDescription = "";
		new KanbanColumnCreateAction(release.getId(), newColumnDescription, true).execute(context);
	}

	@Override
	protected Class<? extends ModelActionEntity> getEntityType() {
		return KanbanColumnCreateActionEntity.class;
	}

	@Override
	protected Class<? extends ModelAction> getActionType() {
		return KanbanColumnCreateAction.class;
	}
}
