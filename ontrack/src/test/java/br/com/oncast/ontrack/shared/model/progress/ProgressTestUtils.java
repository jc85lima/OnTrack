package br.com.oncast.ontrack.shared.model.progress;

import java.util.Date;

import br.com.oncast.ontrack.shared.model.progress.Progress.ProgressState;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.utils.mocks.models.UserRepresentationTestUtils;

public class ProgressTestUtils {

	public static void setProgressState(final Scope scope, final ProgressState state) {
		scope.getProgress().setState(state, UserRepresentationTestUtils.getAdmin(), new Date());
	}

	public static Progress create() {
		return new Progress(UserRepresentationTestUtils.getAdmin(), new Date());
	}

}
