package net.lacnic.elections.adminweb.ui.admin.election.charts;

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.utils.CountryUtils;

public class CountryParticipationStats implements Serializable {

	private static final long serialVersionUID = 8568928778443610032L;
	private static final String NO_COUNTRY_KEY = "__NO_COUNTRY__";
	private static final String NO_COUNTRY_LABEL_ES = "Sin pais";
	private static final String NO_COUNTRY_LABEL_EN = "No country";
	private static final String NO_COUNTRY_LABEL_PT = "Sem pais";
	private static final String TOTAL_LABEL = "Total";

	private final List<CountryColumn> countries;
	private final List<CountryReportRow> countryReportRows;
	private final List<String> chartLabels;
	private final List<ChartCountrySeries> chartSeries;

	private CountryParticipationStats(List<CountryColumn> countries, List<CountryReportRow> countryReportRows, List<String> chartLabels, List<ChartCountrySeries> chartSeries) {
		this.countries = countries;
		this.countryReportRows = countryReportRows;
		this.chartLabels = chartLabels;
		this.chartSeries = chartSeries;
	}

	public static CountryParticipationStats build(List<UserVoter> voters, Locale locale) {
		Locale safeLocale = locale != null ? locale : Locale.getDefault();
		Map<String, MutableCountryStats> statsByCountry = buildCountryStats(voters, safeLocale);
		List<MutableCountryStats> orderedStats = sortCountries(statsByCountry);
		List<CountryColumn> countries = buildCountries(orderedStats);
		List<CountryReportRow> countryReportRows = buildCountryReportRows(orderedStats, safeLocale);
		List<String> chartLabels = buildChartLabels(orderedStats);
		List<ChartCountrySeries> chartSeries = buildChartSeries(orderedStats, chartLabels);

		return new CountryParticipationStats(countries, countryReportRows, chartLabels, chartSeries);
	}

	public boolean hasCountries() {
		return !countries.isEmpty();
	}

	public boolean hasChartData() {
		return !chartLabels.isEmpty() && !chartSeries.isEmpty();
	}

	public List<CountryColumn> getCountries() {
		return countries;
	}

	public List<CountryReportRow> getCountryReportRows() {
		return countryReportRows;
	}

	public List<String> getChartLabels() {
		return chartLabels;
	}

	public List<ChartCountrySeries> getChartSeries() {
		return chartSeries;
	}

	private static Map<String, MutableCountryStats> buildCountryStats(List<UserVoter> voters, Locale locale) {
		Map<String, MutableCountryStats> statsByCountry = new LinkedHashMap<>();
		CountryUtils countryUtils = new CountryUtils();

		if (voters == null) {
			return statsByCountry;
		}

		for (UserVoter voter : voters) {
			String key = normalizeCountryKey(voter != null ? voter.getCountry() : null);
			MutableCountryStats stats = statsByCountry.computeIfAbsent(key, countryKey -> new MutableCountryStats(countryKey, resolveCountryLabel(countryKey, locale, countryUtils)));
			stats.addVoter(voter);
		}

		return statsByCountry;
	}

	private static List<MutableCountryStats> sortCountries(Map<String, MutableCountryStats> statsByCountry) {
		List<MutableCountryStats> orderedStats = new ArrayList<>(statsByCountry.values());
		orderedStats.sort(Comparator.comparing(MutableCountryStats::getLabel, String.CASE_INSENSITIVE_ORDER));
		return orderedStats;
	}

	private static List<CountryColumn> buildCountries(List<MutableCountryStats> orderedStats) {
		List<CountryColumn> countries = new ArrayList<>();
		for (MutableCountryStats stats : orderedStats) {
			countries.add(new CountryColumn(stats.getKey(), stats.getLabel()));
		}
		return Collections.unmodifiableList(countries);
	}

	private static List<CountryReportRow> buildCountryReportRows(List<MutableCountryStats> orderedStats, Locale locale) {
		List<CountryReportRow> rows = new ArrayList<>();
		if (orderedStats.isEmpty()) {
			return Collections.emptyList();
		}
		long globalTotalVoters = 0L;
		long globalTotalVotes = 0L;
		long globalVotedVoters = 0L;
		long globalVotedVotes = 0L;
		long globalNotVotedVoters = 0L;
		long globalNotVotedVotes = 0L;
		for (MutableCountryStats stats : orderedStats) {
			globalTotalVoters += stats.getTotalVoters();
			globalTotalVotes += stats.getTotalVotes();
			globalVotedVoters += stats.getVotedVoters();
			globalVotedVotes += stats.getVotedVotes();
			globalNotVotedVoters += stats.getNotVotedVoters();
			globalNotVotedVotes += stats.getNotVotedVotes();
		}
		for (MutableCountryStats stats : orderedStats) {
			rows.add(new CountryReportRow(
					stats.getLabel(),
					Long.toString(stats.getTotalVoters()),
					Long.toString(stats.getTotalVotes()),
					Long.toString(stats.getVotedVoters()),
					Long.toString(stats.getVotedVotes()),
					Long.toString(stats.getNotVotedVoters()),
					Long.toString(stats.getNotVotedVotes()),
					formatPercentage(stats.getVotedVoters(), stats.getTotalVoters(), locale),
					formatPercentage(stats.getVotedVotes(), stats.getTotalVotes(), locale),
					formatPercentage(stats.getVotedVotes(), globalVotedVotes, locale),
					false));
		}
		rows.add(new CountryReportRow(
				TOTAL_LABEL,
				Long.toString(globalTotalVoters),
				Long.toString(globalTotalVotes),
				Long.toString(globalVotedVoters),
				Long.toString(globalVotedVotes),
				Long.toString(globalNotVotedVoters),
				Long.toString(globalNotVotedVotes),
				formatPercentage(globalVotedVoters, globalTotalVoters, locale),
				formatPercentage(globalVotedVotes, globalTotalVotes, locale),
				formatPercentage(globalVotedVotes, globalVotedVotes, locale),
				true));
		return Collections.unmodifiableList(rows);
	}

	private static List<String> buildChartLabels(List<MutableCountryStats> orderedStats) {
		Date firstVoteDate = null;
		Date lastVoteDate = null;

		for (MutableCountryStats stats : orderedStats) {
			Date countryFirstVoteDate = stats.getFirstVoteDate();
			Date countryLastVoteDate = stats.getLastVoteDate();
			if (countryFirstVoteDate != null && (firstVoteDate == null || countryFirstVoteDate.before(firstVoteDate))) {
				firstVoteDate = countryFirstVoteDate;
			}
			if (countryLastVoteDate != null && (lastVoteDate == null || countryLastVoteDate.after(lastVoteDate))) {
				lastVoteDate = countryLastVoteDate;
			}
		}

		if (firstVoteDate == null || lastVoteDate == null) {
			return Collections.emptyList();
		}

		return Collections.unmodifiableList(getDatesBetween(firstVoteDate, lastVoteDate));
	}

	private static List<ChartCountrySeries> buildChartSeries(List<MutableCountryStats> orderedStats, List<String> chartLabels) {
		if (chartLabels.isEmpty()) {
			return Collections.emptyList();
		}

		List<ChartCountrySeries> series = new ArrayList<>();
		for (MutableCountryStats stats : orderedStats) {
			List<Integer> values = new ArrayList<>();
			int accumulated = 0;
			for (String day : chartLabels) {
				accumulated += stats.getVotedByDate().getOrDefault(day, 0);
				values.add(accumulated);
			}
			series.add(new ChartCountrySeries(stats.getLabel(), Collections.unmodifiableList(values)));
		}
		return Collections.unmodifiableList(series);
	}

	static String buildChartRows(List<String> labels, List<ChartCountrySeries> series) {
		if (labels == null || labels.isEmpty() || series == null || series.isEmpty()) {
			return "['']";
		}

		StringBuilder rows = new StringBuilder();
		for (int i = 0; i < labels.size(); i++) {
			if (rows.length() > 0) {
				rows.append(',');
			}
			rows.append("['").append(VotersGraphPanel.escapeJs(labels.get(i))).append("'");
			for (ChartCountrySeries countrySeries : series) {
				rows.append(',').append(countrySeries.valueAt(i));
			}
			rows.append(']');
		}
		return rows.toString();
	}

	static String buildChartColumns(List<ChartCountrySeries> series) {
		if (series == null || series.isEmpty()) {
			return "";
		}

		StringBuilder columns = new StringBuilder();
		for (ChartCountrySeries countrySeries : series) {
			columns.append("data.addColumn('number', '")
					.append(VotersGraphPanel.escapeJs(countrySeries.getCountryLabel()))
					.append("');\n");
		}
		return columns.toString();
	}

	private static String normalizeCountryKey(String country) {
		if (country == null || country.trim().isEmpty()) {
			return NO_COUNTRY_KEY;
		}
		return country.trim().toUpperCase(Locale.ROOT);
	}

	private static String resolveCountryLabel(String countryKey, Locale locale, CountryUtils countryUtils) {
		if (NO_COUNTRY_KEY.equals(countryKey)) {
			return resolveNoCountryLabel(locale);
		}
		String label = countryUtils.getDisplayLabel(countryKey, locale, true);
		return label == null || label.trim().isEmpty() ? countryKey : label;
	}

	private static String resolveNoCountryLabel(Locale locale) {
		String language = locale != null ? locale.getLanguage() : "";
		if ("en".equals(language)) {
			return NO_COUNTRY_LABEL_EN;
		}
		if ("pt".equals(language)) {
			return NO_COUNTRY_LABEL_PT;
		}
		return NO_COUNTRY_LABEL_ES;
	}

	private static String formatPercentage(long numerator, long denominator, Locale locale) {
		if (denominator <= 0) {
			return "0%";
		}
		NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
		numberFormat.setMinimumFractionDigits(0);
		numberFormat.setMaximumFractionDigits(2);
		return numberFormat.format((numerator * 100.0) / denominator) + "%";
	}

	private static List<String> getDatesBetween(Date startDate, Date endDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		List<String> datesInRange = new ArrayList<>();

		Calendar startCalendar = new GregorianCalendar();
		startCalendar.setTime(startDate);

		Calendar endCalendar = new GregorianCalendar();
		endCalendar.setTime(endDate);

		while (startCalendar.before(endCalendar)) {
			String day = sdf.format(startCalendar.getTime());
			if (!datesInRange.contains(day)) {
				datesInRange.add(day);
			}
			startCalendar.add(Calendar.DATE, 1);
		}

		String day = sdf.format(endDate);
		if (!datesInRange.contains(day)) {
			datesInRange.add(day);
		}

		return datesInRange;
	}

	public static class CountryColumn implements Serializable {

		private static final long serialVersionUID = 6793828834878394945L;
		private final String countryCode;
		private final String countryLabel;

		CountryColumn(String countryCode, String countryLabel) {
			this.countryCode = countryCode;
			this.countryLabel = countryLabel;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public String getCountryLabel() {
			return countryLabel;
		}
	}

	public static class CountryReportRow implements Serializable {

		private static final long serialVersionUID = 5382615787301463206L;
		private final String countryLabel;
		private final String totalVoters;
		private final String totalVotes;
		private final String votedVoters;
		private final String votedVotes;
		private final String notVotedVoters;
		private final String notVotedVotes;
		private final String voterPercentage;
		private final String votePercentage;
		private final String globalVotePercentage;
		private final boolean totalRow;

		CountryReportRow(String countryLabel, String totalVoters, String totalVotes, String votedVoters, String votedVotes, String notVotedVoters, String notVotedVotes, String voterPercentage, String votePercentage, String globalVotePercentage, boolean totalRow) {
			this.countryLabel = countryLabel;
			this.totalVoters = totalVoters;
			this.totalVotes = totalVotes;
			this.votedVoters = votedVoters;
			this.votedVotes = votedVotes;
			this.notVotedVoters = notVotedVoters;
			this.notVotedVotes = notVotedVotes;
			this.voterPercentage = voterPercentage;
			this.votePercentage = votePercentage;
			this.globalVotePercentage = globalVotePercentage;
			this.totalRow = totalRow;
		}

		public String getCountryLabel() {
			return countryLabel;
		}

		public String getTotalVoters() {
			return totalVoters;
		}

		public String getTotalVotes() {
			return totalVotes;
		}

		public String getVotedVoters() {
			return votedVoters;
		}

		public String getVotedVotes() {
			return votedVotes;
		}

		public String getNotVotedVoters() {
			return notVotedVoters;
		}

		public String getNotVotedVotes() {
			return notVotedVotes;
		}

		public String getVoterPercentage() {
			return voterPercentage;
		}

		public String getVotePercentage() {
			return votePercentage;
		}

		public String getGlobalVotePercentage() {
			return globalVotePercentage;
		}

		public boolean isTotalRow() {
			return totalRow;
		}
	}

	public static class ChartCountrySeries implements Serializable {

		private static final long serialVersionUID = 1102480404269773841L;
		private final String countryLabel;
		private final List<Integer> values;

		ChartCountrySeries(String countryLabel, List<Integer> values) {
			this.countryLabel = countryLabel;
			this.values = values;
		}

		public String getCountryLabel() {
			return countryLabel;
		}

		public List<Integer> getValues() {
			return values;
		}

		private int valueAt(int index) {
			return index < values.size() ? values.get(index) : 0;
		}
	}

	private static class MutableCountryStats {

		private final String key;
		private final String label;
		private final Map<String, Integer> votedByDate = new TreeMap<>();
		private long totalVoters;
		private long totalVotes;
		private long votedVoters;
		private long votedVotes;
		private Date firstVoteDate;
		private Date lastVoteDate;

		MutableCountryStats(String key, String label) {
			this.key = key;
			this.label = label;
		}

		void addVoter(UserVoter voter) {
			totalVoters++;
			long voteAmount = safeVoteAmount(voter);
			totalVotes += voteAmount;

			if (voter != null && voter.isVoted()) {
				votedVoters++;
				votedVotes += voteAmount;
				Date voteDate = voter.getVoteDate();
				if (voteDate != null) {
					votedByDate.merge(formatChartDate(voteDate), 1, Integer::sum);
					if (firstVoteDate == null || voteDate.before(firstVoteDate)) {
						firstVoteDate = voteDate;
					}
					if (lastVoteDate == null || voteDate.after(lastVoteDate)) {
						lastVoteDate = voteDate;
					}
				}
			}
		}

		private static int safeVoteAmount(UserVoter voter) {
			return voter != null && voter.getVoteAmount() != null ? voter.getVoteAmount() : 0;
		}

		private static String formatChartDate(Date voteDate) {
			return new SimpleDateFormat("dd/MM/yyyy").format(voteDate);
		}

		String getKey() {
			return key;
		}

		String getLabel() {
			return label;
		}

		Map<String, Integer> getVotedByDate() {
			return votedByDate;
		}

		long getTotalVoters() {
			return totalVoters;
		}

		long getTotalVotes() {
			return totalVotes;
		}

		long getVotedVoters() {
			return votedVoters;
		}

		long getVotedVotes() {
			return votedVotes;
		}

		long getNotVotedVoters() {
			return totalVoters - votedVoters;
		}

		long getNotVotedVotes() {
			return totalVotes - votedVotes;
		}

		Date getFirstVoteDate() {
			return firstVoteDate;
		}

		Date getLastVoteDate() {
			return lastVoteDate;
		}
	}
}
