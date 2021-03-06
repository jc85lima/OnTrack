package br.com.oncast.ontrack.server.services.exportImport.freemind;

import br.com.oncast.ontrack.server.services.exportImport.freemind.abstractions.FreeMindMap;
import br.com.oncast.ontrack.server.services.exportImport.freemind.abstractions.Icon;
import br.com.oncast.ontrack.server.services.exportImport.freemind.abstractions.MindNode;
import br.com.oncast.ontrack.shared.model.prioritizationCriteria.Effort;
import br.com.oncast.ontrack.shared.model.prioritizationCriteria.Value;
import br.com.oncast.ontrack.shared.model.project.Project;
import br.com.oncast.ontrack.shared.model.scope.Scope;

import java.io.OutputStream;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

// TODO Review by Lobo - This code has been created by Rodrigo Machado and Jaime and has not yet been reviewed.
public class FreeMindExporter {

	private final static DecimalFormat EFFORT_AND_VALUE_DECIMAL_FORMAT;

	static {
		EFFORT_AND_VALUE_DECIMAL_FORMAT = new DecimalFormat("#.###");
		EFFORT_AND_VALUE_DECIMAL_FORMAT.setRoundingMode(RoundingMode.DOWN);
		final DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
		decimalFormatSymbols.setDecimalSeparator('.');
		EFFORT_AND_VALUE_DECIMAL_FORMAT.setDecimalFormatSymbols(decimalFormatSymbols);
	}

	/**
	 * Creates a FreeMind (*.mm) compatible mind map, using a {@link Project} to build the map.
	 * @param project source of data which the map will be created.
	 * @param outputStream the stream in which the data will be written.
	 * @return the mind map exported.
	 */
	public static FreeMindMap export(final Project project, final OutputStream outputStream) {
		final FreeMindMap mindMap = FreeMindMap.createNewMap();

		populateMap(project, mindMap);
		save(mindMap, outputStream);

		return mindMap;
	}

	private static void populateMap(final Project project, final FreeMindMap mindMap) {
		final MindNode root = mindMap.root();

		final Scope scope = project.getProjectScope();
		root.setText(scope.getDescription());

		addLegendTo(root);
		addScopeHierarchyTo(root, scope);
	}

	// TODO i18n - Translate these hardcoded descriptions.
	private static void addLegendTo(final MindNode node) {
		final MindNode legend = appendNodeTo(node, "Legenda", Icon.INFO);
		appendNodeTo(legend, "Associação com entrega", Icon.CALENDAR);
		appendNodeTo(legend, "Declaração de esforço", Icon.LAUNCH);
		appendNodeTo(legend, "Inferência de esforço", Icon.LAUNCH, Icon.WIZARD);
		appendNodeTo(legend, "Declaração de valor", Icon.LAUNCH, Icon.STAR);
		appendNodeTo(legend, "Inferência de valor", Icon.LAUNCH, Icon.WIZARD, Icon.STAR);
		appendNodeTo(legend, "Declaração de progresso", Icon.HOURGLASS);
	}

	private static void addScopeHierarchyTo(final MindNode project, final Scope scope) {
		final MindNode rootNodeOfScopeHierarchy = appendNodeTo(project, "Árvore de escopo", Icon.LIST);
		populateScopeHierarchy(rootNodeOfScopeHierarchy, scope);
	}

	private static void populateScopeHierarchy(final MindNode node, final Scope scope) {
		addReleaseNodeTo(node, scope);
		addEffortNodeTo(node, scope);
		addValueNodeTo(node, scope);
		addProgressNodeTo(node, scope);

		for (final Scope childScope : scope.getChildren()) {
			final MindNode newNode = appendNodeTo(node, childScope.getDescription());
			populateScopeHierarchy(newNode, childScope);
		}
	}

	private static void addReleaseNodeTo(final MindNode node, final Scope scope) {
		if (scope.getRelease() != null) appendNodeTo(node, scope.getRelease().getFullDescription(), Icon.CALENDAR);
	}

	private static void addEffortNodeTo(final MindNode node, final Scope scope) {
		final Effort effort = scope.getEffort();

		if (effort.hasDeclared()) appendNodeTo(node, Float.toString(effort.getDeclared()), Icon.LAUNCH);
		if (effort.getInfered() > effort.getDeclared()) appendNodeTo(node, roundValue(effort.getInfered()), Icon.LAUNCH,
				Icon.WIZARD);
	}

	private static void addValueNodeTo(final MindNode node, final Scope scope) {
		final Value value = scope.getValue();

		if (value.hasDeclared()) appendNodeTo(node, Float.toString(value.getDeclared()), Icon.LAUNCH, Icon.STAR);
		if (value.getInfered() > value.getDeclared()) appendNodeTo(node, roundValue(value.getInfered()), Icon.LAUNCH,
				Icon.WIZARD, Icon.STAR);
	}

	private static String roundValue(final float infered) {
		return EFFORT_AND_VALUE_DECIMAL_FORMAT.format(infered);
	}

	private static void addProgressNodeTo(final MindNode node, final Scope scope) {
		if (scope.getProgress().hasDeclared()) appendNodeTo(node, scope.getProgress().getDescription(), Icon.HOURGLASS);
	}

	private static MindNode appendNodeTo(final MindNode node, final String text, final Icon... icons) {
		final MindNode newNode = node.appendChild();
		newNode.setText(text);
		for (final Icon icon : icons)
			newNode.addIcon(icon);

		return newNode;
	}

	private static void save(final FreeMindMap map, final OutputStream outputStream) {
		try {
			map.write(outputStream);
			outputStream.close();
		}
		catch (final Exception e) {
			throw new RuntimeException("Unable to re-write FreeMind MindMap.", e);
		}
	}

}
