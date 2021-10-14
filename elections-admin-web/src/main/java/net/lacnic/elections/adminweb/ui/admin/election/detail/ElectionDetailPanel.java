package net.lacnic.elections.adminweb.ui.admin.election.detail;

import java.util.Arrays;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.UrlValidator;

import net.lacnic.elections.adminweb.wicket.util.Time24HoursValidator;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionCategory;


public class ElectionDetailPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");


	public ElectionDetailPanel(String id, Election election) {
		super(id);
		try {
			setOutputMarkupId(true);

			WebMarkupContainer electionDescriptions = new WebMarkupContainer("electionDescriptions");
			electionDescriptions.setOutputMarkupPlaceholderTag(true);
			electionDescriptions.setVisible(!election.isOnlySp());
			add(electionDescriptions);

			WebMarkupContainer electionTitles = new WebMarkupContainer("electionTitles");
			electionTitles.setOutputMarkupPlaceholderTag(true);
			electionTitles.setVisible(!election.isOnlySp());
			add(electionTitles);

			WebMarkupContainer electionUrls = new WebMarkupContainer("electionUrls");
			electionUrls.setOutputMarkupPlaceholderTag(true);
			electionUrls.setVisible(!election.isOnlySp());
			add(electionUrls);

			AjaxCheckBox onlySp = new AjaxCheckBox("onlySpCheckbox", new PropertyModel<>(election, "onlySp")) {
				private static final long serialVersionUID = 7324854310046050015L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					electionDescriptions.setVisible(!election.isOnlySp());
					electionTitles.setVisible(!election.isOnlySp());
					electionUrls.setVisible(!election.isOnlySp());
					target.add(electionDescriptions);
					target.add(electionTitles);
					target.add(electionUrls);
				}
			};
			add(onlySp);

			DropDownChoice<ElectionCategory> selectCategory = new DropDownChoice<>("selectCategory", new PropertyModel<>(election, "category"), Arrays.asList(ElectionCategory.values()));
			selectCategory.setRequired(true);
			add(selectCategory);

			TextField<String> titleSpanish = new TextField<>("titleSpanish", new PropertyModel<>(election, "titleSpanish"));
			titleSpanish.add(StringValidator.maximumLength(1000));
			titleSpanish.setRequired(true);
			add(titleSpanish);

			TextField<String> titleEnglish = new TextField<>("titleEnglish", new PropertyModel<>(election, "titleEnglish"));
			titleEnglish.add(StringValidator.maximumLength(1000));
			titleEnglish.setRequired(true);
			electionTitles.add(titleEnglish);

			TextField<String> titlePortuguese = new TextField<>("titlePortuguese", new PropertyModel<>(election, "titlePortuguese"));
			titlePortuguese.add(StringValidator.maximumLength(1000));
			titlePortuguese.setRequired(true);
			electionTitles.add(titlePortuguese);

			TextArea<String> descriptionSpanish = new TextArea<>("descriptionSpanish", new PropertyModel<>(election, "descriptionSpanish"));
			descriptionSpanish.add(StringValidator.maximumLength(2000));
			descriptionSpanish.setRequired(true);
			add(descriptionSpanish);

			TextArea<String> descriptionEnglish = new TextArea<>("descriptionEnglish", new PropertyModel<>(election, "descriptionEnglish"));
			descriptionEnglish.add(StringValidator.maximumLength(2000));
			descriptionEnglish.setRequired(true);
			electionDescriptions.add(descriptionEnglish);

			TextArea<String> descriptionPortuguese = new TextArea<>("descriptionPortuguese", new PropertyModel<>(election, "descriptionPortuguese"));
			descriptionPortuguese.add(StringValidator.maximumLength(2000));
			descriptionPortuguese.setRequired(true);
			electionDescriptions.add(descriptionPortuguese);

			TextField<String> linkSpanish = new TextField<>("linkSpanish", new PropertyModel<>(election, "linkSpanish"));
			linkSpanish.add(StringValidator.maximumLength(1000));
			linkSpanish.add(new UrlValidator());
			linkSpanish.setRequired(true);
			add(linkSpanish);

			TextField<String> linkEnglish = new TextField<>("linkEnglish", new PropertyModel<>(election, "linkEnglish"));
			linkEnglish.add(StringValidator.maximumLength(1000));
			linkEnglish.add(new UrlValidator());
			linkEnglish.setRequired(true);
			electionUrls.add(linkEnglish);

			TextField<String> linkPortuguese = new TextField<>("linkPortuguese", new PropertyModel<>(election, "linkPortuguese"));
			linkPortuguese.add(StringValidator.maximumLength(1000));
			linkPortuguese.add(new UrlValidator());
			linkPortuguese.setRequired(true);
			electionUrls.add(linkPortuguese);

			TextField<String> startDate = new TextField<>("startDate", new PropertyModel<>(election, "auxStartDate"));
			startDate.setRequired(true);
			add(startDate);

			TextField<String> endDate = new TextField<>("endDate", new PropertyModel<>(election, "auxEndDate"));
			endDate.setRequired(true);
			add(endDate);

			TextField<String> startTime = new TextField<>("startTime", new PropertyModel<>(election, "auxStartHour"));
			startTime.setRequired(true);
			startTime.add(new Time24HoursValidator());
			add(startTime);

			TextField<String> endTime = new TextField<>("endTime", new PropertyModel<>(election, "auxEndHour"));
			endTime.setRequired(true);
			endTime.add(new Time24HoursValidator());
			add(endTime);

			TextField<Integer> maxCandidates = new TextField<>("maxCandidates", new PropertyModel<>(election, "maxCandidates"));
			maxCandidates.setRequired(true);
			maxCandidates.add(RangeValidator.range(1, 100));
			add(maxCandidates);

			TextField<Integer> diffUTC = new TextField<>("diffUTC", new PropertyModel<>(election, "diffUTC"));
			diffUTC.setRequired(true);
			diffUTC.add(RangeValidator.range(0, 12));
			add(diffUTC);

			EmailTextField defaultSender = new EmailTextField("defaultSender", new PropertyModel<>(election, "defaultSender"));
			defaultSender.setRequired(true);
			add(defaultSender);

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}