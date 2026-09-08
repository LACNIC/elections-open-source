package net.lacnic.elections.adminweb.ui.admin.election.charts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.UserVoter;

class CountryParticipationStatsTest {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	void shouldBuildCountryParticipationReportWithWeightedVotes() throws Exception {
		CountryParticipationStats stats = CountryParticipationStats.build(List.of(
				voter("AR", 2, true, "2026-01-01"),
				voter("AR", 1, false, null),
				voter("UY", 3, false, null)), new Locale("es"));

		assertTrue(stats.hasCountries());
		assertEquals(List.of("Argentina (AR)", "Uruguay (UY)"), stats.getCountries().stream().map(CountryParticipationStats.CountryColumn::getCountryLabel).toList());

		CountryParticipationStats.CountryReportRow argentina = stats.getCountryReportRows().get(0);
		assertEquals("Argentina (AR)", argentina.getCountryLabel());
		assertEquals("2", argentina.getTotalVoters());
		assertEquals("3", argentina.getTotalVotes());
		assertEquals("1", argentina.getVotedVoters());
		assertEquals("2", argentina.getVotedVotes());
		assertEquals("1", argentina.getNotVotedVoters());
		assertEquals("1", argentina.getNotVotedVotes());
		assertEquals("50%", argentina.getVoterPercentage());
		assertEquals("66,67%", argentina.getVotePercentage());
		assertEquals("100%", argentina.getGlobalVotePercentage());

		CountryParticipationStats.CountryReportRow uruguay = stats.getCountryReportRows().get(1);
		assertEquals("Uruguay (UY)", uruguay.getCountryLabel());
		assertEquals("1", uruguay.getTotalVoters());
		assertEquals("3", uruguay.getTotalVotes());
		assertEquals("0", uruguay.getVotedVoters());
		assertEquals("0", uruguay.getVotedVotes());
		assertEquals("1", uruguay.getNotVotedVoters());
		assertEquals("3", uruguay.getNotVotedVotes());
		assertEquals("0%", uruguay.getVoterPercentage());
		assertEquals("0%", uruguay.getVotePercentage());
		assertEquals("0%", uruguay.getGlobalVotePercentage());

		CountryParticipationStats.CountryReportRow total = stats.getCountryReportRows().get(2);
		assertEquals("Total", total.getCountryLabel());
		assertEquals("3", total.getTotalVoters());
		assertEquals("6", total.getTotalVotes());
		assertEquals("1", total.getVotedVoters());
		assertEquals("2", total.getVotedVotes());
		assertEquals("2", total.getNotVotedVoters());
		assertEquals("4", total.getNotVotedVotes());
		assertEquals("33,33%", total.getVoterPercentage());
		assertEquals("33,33%", total.getVotePercentage());
		assertEquals("100%", total.getGlobalVotePercentage());
		assertTrue(total.isTotalRow());
	}

	@Test
	void shouldBuildAccumulatedChartSeriesByCountry() throws Exception {
		CountryParticipationStats stats = CountryParticipationStats.build(List.of(
				voter("AR", 2, true, "2026-01-01"),
				voter("UY", 1, true, "2026-01-03")), new Locale("es"));

		assertEquals(List.of("01/01/2026", "02/01/2026", "03/01/2026"), stats.getChartLabels());
		assertEquals(List.of(1, 1, 1), stats.getChartSeries().get(0).getValues());
		assertEquals(List.of(0, 0, 1), stats.getChartSeries().get(1).getValues());
		assertEquals("['01/01/2026',1,0],['02/01/2026',1,0],['03/01/2026',1,1]",
				CountryParticipationStats.buildChartRows(stats.getChartLabels(), stats.getChartSeries()));
	}

	@Test
	void shouldShowCountriesWithoutVotesInReportAndHideChartWhenNoVoteDatesExist() {
		CountryParticipationStats stats = CountryParticipationStats.build(List.of(
				voter("AR", 2, false, null)), Locale.ENGLISH);

		assertTrue(stats.hasCountries());
		assertTrue(!stats.hasChartData());
		assertEquals("1", stats.getCountryReportRows().get(0).getTotalVoters());
		assertEquals("2", stats.getCountryReportRows().get(0).getTotalVotes());
		assertEquals("Total", stats.getCountryReportRows().get(1).getCountryLabel());
		assertEquals("0%", stats.getCountryReportRows().get(1).getGlobalVotePercentage());
	}

	private static UserVoter voter(String country, Integer voteAmount, boolean voted, String voteDate) {
		UserVoter userVoter = new UserVoter();
		userVoter.setCountry(country);
		userVoter.setVoteAmount(voteAmount);
		userVoter.setVoted(voted);
		if (voteDate != null) {
			try {
				userVoter.setVoteDate(DATE_FORMAT.parse(voteDate));
			} catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return userVoter;
	}
}
