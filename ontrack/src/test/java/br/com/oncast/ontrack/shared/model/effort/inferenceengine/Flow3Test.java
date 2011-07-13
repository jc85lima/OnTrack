package br.com.oncast.ontrack.shared.model.effort.inferenceengine;

import static br.com.oncast.ontrack.shared.model.effort.inferenceengine.Util.getModifiedScope;
import static br.com.oncast.ontrack.shared.model.effort.inferenceengine.Util.getOriginalScope;
import static br.com.oncast.ontrack.utils.assertions.Assert.assertDeepEquals;

import org.junit.Test;

import br.com.oncast.ontrack.shared.model.effort.EffortInferenceEngine;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.exceptions.UnableToCompleteActionException;

public class Flow3Test {

	private final String FILE_NAME = "Flow3";
	private final Scope original = getOriginalScope(FILE_NAME);

	@Test
	public void shouldApplyInferencesWhenEffortChanges() throws UnableToCompleteActionException {
		shouldApplyInferenceTopDownThroughChildren();
		shouldRedistributeInferencesWhenChildrenReceiveEffortDeclarations();
	}

	private void shouldApplyInferenceTopDownThroughChildren() {
		original.getChild(0).getEffort().setDeclared(12);
		EffortInferenceEngine.process(original);

		assertDeepEquals(original, getModifiedScope(FILE_NAME, 1));
	}

	private void shouldRedistributeInferencesWhenChildrenReceiveEffortDeclarations() {
		final Scope scope = original.getChild(0).getChild(0);
		scope.getChild(0).getEffort().setDeclared(8);
		EffortInferenceEngine.process(scope);
		scope.getChild(1).getEffort().setDeclared(8);
		EffortInferenceEngine.process(scope);

		assertDeepEquals(original, getModifiedScope(FILE_NAME, 2));
	}

	// private void shouldRedistributeInferenceBetweenSiblingsWhenOneIsAdded() {
	// final Scope newScope = new Scope("Cancelar pedido");
	// original.getChild(1).add(newScope);
	// EffortInferenceEngine.process(original);
	//
	// assertEquals(newScope.getEffort().getInfered(), 62.5, 0.1);
	// assertTrue(original.deepEquals(getModifiedScope(FILE_NAME, 2)));
	// }

	// private void shouldRedistributeInferenceBetweenSiblingsWhenParentEffortIsDeclared() {
	// final Scope scopeWithChangedEffort = original.getChild(1);
	// scopeWithChangedEffort.getEffort().setDeclared(350);
	// EffortInferenceEngine.process(scopeWithChangedEffort.getParent());
	//
	// for (final Scope child : scopeWithChangedEffort.getChildren()) {
	// assertEquals(child.getEffort().getInfered(), 87.5, 0.1);
	// }
	// assertTrue(original.deepEquals(getModifiedScope(FILE_NAME, 3)));
	// }
}