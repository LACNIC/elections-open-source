package net.lacnic.elections.adminweb.ui.admin.election.charts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class VotersGraphPanelTest {

	@Test
	void shouldBuildRowsUsingShortestSeriesAndEscapingLabels() {
		String rows = VotersGraphPanel.buildRows(
				List.of("23/10/2025", "O'Brien\\test", "ignored"),
				Arrays.asList(199, null),
				List.of(199, 306));

		assertEquals("['23/10/2025',199,199],['O\\'Brien\\\\test',0,306]", rows);
	}

	@Test
	void shouldBuildTicksWithEscapedLabels() {
		String ticks = VotersGraphPanel.buildTicks(List.of("23/10/2025", "O'Brien\\test"));

		assertEquals("'23/10/2025','O\\'Brien\\\\test'", ticks);
	}

	@Test
	void shouldReturnFallbackValuesWhenSeriesAreEmpty() {
		assertEquals("['',0,0]", VotersGraphPanel.buildRows(List.of(), List.of(), List.of()));
		assertEquals("''", VotersGraphPanel.buildTicks(List.of()));
	}
}
