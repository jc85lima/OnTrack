package br.com.oncast.ontrack.server.services.exportImport.freemind;

import java.io.File;
import java.util.logging.Logger;

import br.com.oncast.ontrack.server.services.exportImport.freemind.abstractions.FreeMindMap;
import br.com.oncast.ontrack.server.services.exportImport.freemind.abstractions.Icon;
import br.com.oncast.ontrack.server.services.exportImport.freemind.abstractions.MindNode;
import br.com.oncast.ontrack.shared.model.project.Project;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;

public class FreeMindProjectLoader {
	private static final Logger LOGGER = Logger.getLogger(FreeMindProjectLoader.class.getName());

	private final FreeMindMap mm;
	private final Project project = new Project();
	private final ProjectContext context = new ProjectContext(project);
	private final Release rootRelease = new Release("project");

	private FreeMindProjectLoader(final FreeMindMap mm) {
		this.mm = mm;
	}

	public static Project loadMap(final File mmFile) {
		return new FreeMindProjectLoader(FreeMindMap.open(mmFile)).load();
	}

	private Project load() {
		final MindNode mmRoot = mm.root();
		final Scope root = new Scope(mmRoot.getText());
		project.setProjectScope(root);
		project.setProjectRelease(rootRelease);
		visitScope(root, mmRoot.getChildren().get(1));

		return project;
	}

	private void visitScope(final Scope scope, final MindNode node) {
		for (final MindNode child : node.getChildren()) {
			if (child.hasIcon(Icon.CALENDAR)) {
				final Release r = context.loadRelease(child.getText());
				scope.setRelease(r);
				if (r!=null) r.addScope(scope);
			}
			else if (child.hasIcon(Icon.LAUNCH) && !child.hasIcon(Icon.WIZARD)) {
				try {
					scope.getEffort().setDeclared(Integer.parseInt(child.getText()));
				}
				catch (final NumberFormatException e) {
					LOGGER.warning("Invalid effort declaration on scope '" + scope.getDescription() + "' (" + child.getText() + "). Effort declaration ignored.");
				}
			}
			else if (child.hasIcon(Icon.HOURGLASS) && !child.hasIcon(Icon.WIZARD)) {
				scope.getProgress().setDescription(child.getText());
			}
			else {
				final Scope s = new Scope(child.getText());
				visitScope(s,child);
				scope.add(s);
			}
		}
	}
}