package br.com.oncast.ontrack.server.utils.mmConverter;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.oncast.ontrack.server.model.scope.ScopeBuilder;
import br.com.oncast.ontrack.server.utils.mmConverter.abstractions.FreeMindMap;
import br.com.oncast.ontrack.server.utils.mmConverter.abstractions.Icon;
import br.com.oncast.ontrack.server.utils.mmConverter.abstractions.MindNode;
import br.com.oncast.ontrack.shared.model.scope.Scope;

// TODO Review by Lobo - This code has been created by Rodrigo Machado and Jaime and has not yet been reviewed.
public class FreeMindImporter {
	private static Pattern INTEGER_EXTRACTOR = Pattern.compile("\\d+");
	private static Pattern FLOAT_EXTRACTOR = Pattern.compile("(\\d+)(\\.\\d?)?");

	private final FreeMindMap mindMap;

	private FreeMindImporter(final FreeMindMap mindMap) {
		this.mindMap = mindMap;
	}

	public static FreeMindImporter importMapFrom(final File file) {
		return new FreeMindImporter(FreeMindMap.open(file));
	}

	public Scope getScope() {
		final ScopeBuilder builder = ScopeBuilder.scope();
		pullSync(builder, mindMap.root());

		return builder.getScope();
	}

	private static void pullSync(final ScopeBuilder scope, final MindNode node) {
		int declaredEffort = 0;
		float topDownEffort = 0;
		float bottomUpEffort = 0;
		boolean effortDeclared = false;
		boolean topDownEffortCalculated = false;
		boolean bottomUpEffortCalculated = false;

		scope.description(node.getText());
		for (final MindNode childNode : node.getChildren()) {
			if (childNode.hasIcon(Icon.PENCIL)) {
				effortDeclared = true;
				declaredEffort += extractDeclaredEffort(childNode);
				continue;
			}
			else if (childNode.hasIcon(Icon.DOWN)) {
				topDownEffortCalculated = true;
				topDownEffort += extractCalculatedEffort(childNode);
				continue;
			}
			else if (childNode.hasIcon(Icon.UP)) {
				bottomUpEffortCalculated = true;
				bottomUpEffort += extractCalculatedEffort(childNode);
				continue;
			}

			final ScopeBuilder childScope = ScopeBuilder.scope();
			pullSync(childScope, childNode);
			scope.add(childScope);
		}

		if (effortDeclared) scope.declaredEffort(declaredEffort);
		if (topDownEffortCalculated) scope.topDownEffort(topDownEffort);
		if (bottomUpEffortCalculated) scope.bottomUpEffort(bottomUpEffort);
	}

	private static int extractDeclaredEffort(final MindNode effortNode) {
		final Matcher matcher = INTEGER_EXTRACTOR.matcher(effortNode.getText());
		if (!matcher.find()) return 0;

		return Integer.valueOf(matcher.group());
	}

	private static float extractCalculatedEffort(final MindNode effortNode) {
		final Matcher matcher = FLOAT_EXTRACTOR.matcher(effortNode.getText());
		if (!matcher.find()) return 0.0f;

		return Float.valueOf(matcher.group());
	}

}