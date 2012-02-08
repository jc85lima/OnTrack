package br.com.oncast.ontrack.shared.model.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import br.com.oncast.ontrack.shared.model.kanban.Kanban;
import br.com.oncast.ontrack.shared.model.kanban.KanbanFactory;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.utils.mocks.models.ReleaseTestUtils;

public class KanbanColumnMoveActionTest {

	private final String kanbanColumnDescription = "target column";
	private Kanban kanban;
	private ProjectContext context;
	private UUID releaseId;

	@Before
	public void setup() throws Exception {
		final Release release = ReleaseTestUtils.createRelease();
		releaseId = release.getId();
		kanban = KanbanFactory.createFor(release);
		kanban.appendColumn("First Column");
		kanban.appendColumn(kanbanColumnDescription);

		context = mock(ProjectContext.class);
		Mockito.when(context.findRelease(release.getId())).thenReturn(release);
		Mockito.when(context.getKanban(release)).thenReturn(kanban);
	}

	@Test
	public void shouldMoveColumnToDesiredIndex() throws Exception {
		final int desiredIndex = 0;

		final ModelAction action = new KanbanColumnMoveAction(releaseId, kanbanColumnDescription, desiredIndex);
		action.execute(context);

		assertEquals(desiredIndex, kanban.indexOf(kanbanColumnDescription));
	}

	@Test
	public void shouldLockKanban() throws Exception {
		final ModelAction action = new KanbanColumnMoveAction(releaseId, kanbanColumnDescription, 0);
		action.execute(context);

		assertTrue(kanban.isLocked());
	}

	@Test
	public void undoShouldReturnKanbanToPreviousState() throws Exception {
		final ModelAction action = new KanbanColumnMoveAction(releaseId, kanbanColumnDescription, 0);
		final ModelAction undo = action.execute(context);
		undo.execute(context);
		assertEquals(1, kanban.indexOf(kanbanColumnDescription));
	}

	@Test
	public void shouldRedo() throws Exception {
		final ModelAction action = new KanbanColumnMoveAction(releaseId, kanbanColumnDescription, 0);
		final ModelAction redo = action.execute(context).execute(context);
		redo.execute(context);
		assertEquals(0, kanban.indexOf(kanbanColumnDescription));
	}

	@Test
	public void undoShouldKeepTheTheKanbanLocked() throws Exception {
		final ModelAction action = new KanbanColumnMoveAction(releaseId, kanbanColumnDescription, 0);
		final ModelAction undo = action.execute(context);
		undo.execute(context);
		assertTrue(kanban.isLocked());
	}
}