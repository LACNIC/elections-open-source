package net.lacnic.elections.adminweb.ui.admin.election.census;

import java.io.File;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.validators.CensusExcelFileValidator;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.data.AsyncProcessingType;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.exception.CensusValidationException;

public class UploadCensusFilePanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String KEY_CENSUS_MANAGEMENT_ERR_BIF = "censusManagementErrBif";

	public UploadCensusFilePanel(String id, Election election) {
		super(id);
		try {
			setOutputMarkupId(true);

			Model<Boolean> regenerateLinksModel = Model.of(Boolean.FALSE);
			final CheckBox overwriteCensus = new CheckBox("overwriteCensus", regenerateLinksModel);
			add(overwriteCensus);

			final FileUploadField censusFileInput = new FileUploadField("censusFileInput");
			censusFileInput.add(new CensusExcelFileValidator(election.getElectionId(), overwriteCensus));
			add(censusFileInput);

			WebMarkupContainer censusFieldsHelp = new WebMarkupContainer("censusFieldsHelp");
			censusFieldsHelp.add(AttributeModifier.replace("title", getString("censusManagementFileUploadTitle")));
			censusFieldsHelp.add(AttributeModifier.replace("data-bs-content", getString("censusManagementUploadHelpContent")));
			add(censusFieldsHelp);

			Button uploadCensusFileButton = new Button("uploadCensusFileButton") {
				private static final long serialVersionUID = 1204295872124958662L;

				@Override
				public void onSubmit() {
					long electionId = election.getElectionId();
					appLogger.info("Census upload submit received in admin UI. electionId={}", electionId);

					boolean regenerateLinks = Boolean.TRUE.equals(regenerateLinksModel.getObject());
					FileUpload fileUpload = censusFileInput.getFileUpload();
					if (fileUpload == null) {
						appLogger.warn("Census upload submit reached without file after successful validation. electionId={}", electionId);
						return;
					}

					try {
						AppContext.getInstance().getManagerBeanRemote().validateElectionCensusUpsertCanBeApplied(fileUpload.getContentType(), electionId, fileUpload.getBytes(), regenerateLinks);
						boolean queued = AppContext.getInstance().getManagerBeanRemote().queueElectionCensusUpdate(fileUpload.getContentType(), electionId, fileUpload.getBytes(), regenerateLinks, SecurityUtils.getUserAdminId(),
								SecurityUtils.getClientIp());
						if (queued) {
							SecurityUtils.markAsyncCompletionFeedbackPending(electionId, AsyncProcessingType.CENSUS);
							appLogger.info("Census upload queued in admin UI. electionId={}, regenerateVoteLinks={}", electionId, regenerateLinks);
						} else {
							SecurityUtils.error(getString("censusManagementProcessingInProgress"));
							appLogger.info("Census upload queue request skipped because another process is already running. electionId={}", electionId);
						}
						redirectToCensusDashboard(electionId);
					} catch (CensusValidationException cve) {
						handleCensusValidationException(electionId, cve);
					} catch (Exception e) {
						handleUnexpectedCensusUploadException(electionId, e);
					}
				}
			};
			add(uploadCensusFileButton);

			File censusExampleFile = AppContext.getInstance().getManagerBeanRemote().exportCensusExample();
			add(new DownloadLink("downloadExample", censusExampleFile));

		} catch (Exception e) {
			appLogger.error(e.getMessage(), e);
		}
	}

	private void handleCensusValidationException(long electionId, CensusValidationException cve) {
		String messageKey = cve.getMessage();
		appLogger.warn("Census upload validation failed. electionId={}, messageKey={}", electionId, messageKey, cve);
		if (messageKey == null) {
			SecurityUtils.error(getString(KEY_CENSUS_MANAGEMENT_ERR_BIF));
			return;
		}

		switch (messageKey) {
		case "censusManagementUploadMissingColumns":
		case "censusManagementUploadNoDataRows":
		case "censusManagementUploadFileError":
		case "censusManagementUploadUnknownFileType":
		case "censusManagementUploadOrgIdRequired":
		case "censusManagementProcessingInProgress":
			SecurityUtils.error(getString(messageKey));
			break;
		case "censusManagementUploadNullRequiredFields":
			SecurityUtils.error(new StringResourceModel(messageKey).setParameters(cve.getErrorRow(), cve.getErrorInfo()).getString());
			break;
		case "censusManagementUploadWrongVoteAmount":
			SecurityUtils.error(new StringResourceModel(messageKey).setParameters(cve.getErrorRow()).getString());
			break;
		case "censusManagementUploadWrongLanguage":
		case "censusManagementUploadWrongEmail":
		case "censusManagementUploadDuplicateOrgId":
		case "censusManagementUploadWrongCountry":
			SecurityUtils.error(new StringResourceModel(messageKey).setParameters(cve.getErrorInfo(), cve.getErrorRow()).getString());
			break;
		case "censusManagementOverwriteBlockedByVotes":
			SecurityUtils.error(new StringResourceModel(messageKey).setParameters(cve.getErrorInfo()).getString());
			break;
		default:
			SecurityUtils.error(getString(KEY_CENSUS_MANAGEMENT_ERR_BIF));
			break;
		}
	}

	private void handleUnexpectedCensusUploadException(long electionId, Exception e) {
		SecurityUtils.error(getString(KEY_CENSUS_MANAGEMENT_ERR_BIF));
		appLogger.error("Unexpected error processing census upload. electionId={}", electionId, e);
	}

	private void redirectToCensusDashboard(long electionId) {
		appLogger.info("Redirecting after census upload flow. electionId={}", electionId);
		setResponsePage(ElectionCensusDashboard.class, UtilsParameters.getId(electionId));
	}

}
