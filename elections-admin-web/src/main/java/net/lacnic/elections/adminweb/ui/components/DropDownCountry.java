package net.lacnic.elections.adminweb.ui.components;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

import net.lacnic.elections.utils.CountryUtils;

public class DropDownCountry extends DropDownChoice<String> {

	private static final long serialVersionUID = 4766509011662393037L;
	private static final String DEFAULT_COUNTRY_ID = "AA";
	private final CountryUtils countryUtils;

	public DropDownCountry(IModel<String> model) {
		this("country", model, true);
	}

	public DropDownCountry(String id, IModel<String> model, boolean includePlaceholder) {
		this(id, model, includePlaceholder, new CountryUtils());
	}

	private DropDownCountry(String id, IModel<String> model, boolean includePlaceholder, CountryUtils countryUtils) {
		super(id, model, getCountryIds(countryUtils, includePlaceholder), null);
		this.countryUtils = countryUtils;
		setChoiceRenderer(new CountryChoiceRenderer(countryUtils));
		setNullValid(true);
	}

	private static List<String> getCountryIds(CountryUtils countryUtils, boolean includePlaceholder) {
		return countryUtils.getIdsListLacnicFirst(includePlaceholder);
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
		List<? extends String> choices = getChoices();
		AppendingStringBuffer buffer = new AppendingStringBuffer((choices.size() * 64) + 32);
		String selectedValue = getValue();

		buffer.append(getDefaultChoice(selectedValue));

		List<CountryOption> lacnicOptions = new ArrayList<>();
		List<CountryOption> otherOptions = new ArrayList<>();

		for (int index = 0; index < choices.size(); index++) {
			String countryId = choices.get(index);
			if (countryId == null) {
				continue;
			}
			if (DEFAULT_COUNTRY_ID.equals(countryId)) {
				appendOptionHtml(buffer, countryId, index, selectedValue);
				continue;
			}
			if (countryUtils.isLacnicCoverageCountryCode(countryId)) {
				lacnicOptions.add(new CountryOption(countryId, index));
			} else {
				otherOptions.add(new CountryOption(countryId, index));
			}
		}

		appendGroup(buffer, getCountryGroupLabel("acceptNominationCountriesGroupLacnic", "LACNIC"), lacnicOptions, selectedValue);
		appendGroup(buffer, getCountryGroupLabel("acceptNominationCountriesGroupOtherCountries", "Other countries"), otherOptions, selectedValue);

		buffer.append('\n');
		replaceComponentTagBody(markupStream, openTag, buffer);
	}

	private String getCountryGroupLabel(String key, String defaultValue) {
		return getString(key, null, defaultValue);
	}

	private void appendGroup(AppendingStringBuffer buffer, String label, List<CountryOption> options, String selectedValue) {
		if (options.isEmpty()) {
			return;
		}
		buffer.append("\n<optgroup label=\"");
		buffer.append(Strings.escapeMarkup(label));
		buffer.append("\">");
		for (CountryOption option : options) {
			appendOptionHtml(buffer, option.countryId, option.index, selectedValue);
		}
		buffer.append("\n</optgroup>");
	}

	private static class CountryOption {
		private final String countryId;
		private final int index;

		private CountryOption(String countryId, int index) {
			this.countryId = countryId;
			this.index = index;
		}
	}

	private class CountryChoiceRenderer extends ChoiceRenderer<String> {

		private static final long serialVersionUID = 6511560739872455637L;
		private final CountryUtils countryUtils;

		private CountryChoiceRenderer(CountryUtils countryUtils) {
			this.countryUtils = countryUtils;
		}

		@Override
		public String getDisplayValue(String countryId) {
			return countryUtils.getDisplayLabel(countryId, DropDownCountry.this.getLocale(), true);
		}

		@Override
		public String getIdValue(String countryId, int index) {
			return countryId;
		}
	}
}
