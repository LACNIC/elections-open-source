package net.lacnic.elections.adminweb.ui.admin.election.organizations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.validators.OrganizationBulkDeleteActionValidator;
import net.lacnic.elections.adminweb.validators.OrganizationExcelFileValidator;
import net.lacnic.elections.adminweb.validators.OrganizationExcelFileValidator.UploadType;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.data.AsyncProcessingType;
import net.lacnic.elections.data.OrganizationBulkImportResult;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.exception.CensusValidationException;
import net.lacnic.elections.utils.EJBFactory;

public class UploadOrganizationsDebtorsFilePanel extends Panel {

	private static final long serialVersionUID = 8431118807675628444L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");
	private static final String JBOSSTEMPURI = resolveJbossTempUri();
	private static final String KEY_ORGANIZATIONS_MANAGEMENT_ERR_NO_FILE = "organizationsManagementErrNoFile";

	private File removeErrorsFile;
	private File upsertErrorsFile;
	private final File addOrganizationsExampleFile;
	private final File deleteOrganizationsExampleFile;
	private final File debtorsOrganizationsExampleFile;
	private final OrganizationBulkDeleteActionValidator bulkDeleteActionValidator = new OrganizationBulkDeleteActionValidator();

	public UploadOrganizationsDebtorsFilePanel(String id, Election election) {
		super(id);
		setOutputMarkupId(true);

		addOrganizationsExampleFile = AppContext.getInstance().getManagerBeanRemote().exportOrganizationsUpsertExample();
		deleteOrganizationsExampleFile = AppContext.getInstance().getManagerBeanRemote().exportOrganizationsDeleteExample();
		debtorsOrganizationsExampleFile = AppContext.getInstance().getManagerBeanRemote().exportOrganizationsDebtorsExample();

		final FileUploadField orgDebtorsFileInput = new FileUploadField("orgDebtorsFileInput");
		orgDebtorsFileInput.add(new OrganizationExcelFileValidator(UploadType.DEBTORS));
		add(orgDebtorsFileInput);

		Model<Boolean> overwriteDebtorsModel = Model.of(Boolean.FALSE);
		add(new CheckBox("overwriteDebtors", overwriteDebtorsModel));

		add(new SubmitLink("uploadOrgDebtorsFileButton") {
			private static final long serialVersionUID = 1457096547261590617L;

			@Override
				public void onSubmit() {
					if (orgDebtorsFileInput.getFileUpload() == null) {
						getSession().error(getString(KEY_ORGANIZATIONS_MANAGEMENT_ERR_NO_FILE));
						return;
					}
					try {
						byte[] content = orgDebtorsFileInput.getFileUpload().getBytes();
						String contentType = orgDebtorsFileInput.getFileUpload().getContentType();
						AppContext.getInstance().getManagerBeanRemote().validateOrganizationsDebtorsCanBeApplied(contentType, election.getElectionId(), content);
					boolean queued = AppContext.getInstance().getManagerBeanRemote().queueOrganizationsDebtorsUpdate(contentType, election.getElectionId(), content, overwriteDebtorsModel.getObject(),
							SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					if (queued) {
						SecurityUtils.markAsyncCompletionFeedbackPending(election.getElectionId(), AsyncProcessingType.ORGANIZATIONS);
						appLogger.info("Organizations debtors upload queued in admin UI. electionId={}, overwriteAll={}", election.getElectionId(), overwriteDebtorsModel.getObject());
					} else {
						getSession().error(getString(OrganizationsResourceKeys.PROCESSING_IN_PROGRESS));
						appLogger.info("Organizations debtors upload queue request skipped because another process is already running. electionId={}", election.getElectionId());
					}
					} catch (CensusValidationException cve) {
						handleValidationError(cve);
						return;
				} catch (Exception e) {
					appLogger.error(e.getMessage(), e);
					getSession().error(getString(OrganizationsResourceKeys.ERR_BIF));
					return;
				}
				setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		});

		final FileUploadField orgDeleteFileInput = new FileUploadField("orgDeleteFileInput");
		orgDeleteFileInput.add(new OrganizationExcelFileValidator(UploadType.DELETE));
		add(orgDeleteFileInput);
		WebMarkupContainer removeErrorsContainer = new WebMarkupContainer("removeErrorsContainer");
		removeErrorsContainer.setOutputMarkupPlaceholderTag(true);
		removeErrorsContainer.setVisible(false);
		DownloadLink downloadRemoveErrors = new DownloadLink("downloadRemoveErrors", new PropertyModel<>(this, "removeErrorsFile"));
		downloadRemoveErrors.setDeleteAfterDownload(false);
		removeErrorsContainer.add(downloadRemoveErrors);
		add(removeErrorsContainer);
		add(new SubmitLink("uploadDeleteOrganizationsFileButton") {
			private static final long serialVersionUID = -8157228383768785792L;

			@Override
				public void onSubmit() {
					removeErrorsContainer.setVisible(false);
					if (orgDeleteFileInput.getFileUpload() == null) {
						getSession().error(getString(KEY_ORGANIZATIONS_MANAGEMENT_ERR_NO_FILE));
						return;
					}
					try {
						byte[] content = orgDeleteFileInput.getFileUpload().getBytes();
						String contentType = orgDeleteFileInput.getFileUpload().getContentType();
						bulkDeleteActionValidator.validateDeleteExcelBeforeSubmit(contentType, election.getElectionId(), content);
					boolean queued = AppContext.getInstance().getManagerBeanRemote().queueOrganizationsDeleteFromExcel(contentType, election.getElectionId(), content, SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					if (queued) {
						SecurityUtils.markAsyncCompletionFeedbackPending(election.getElectionId(), AsyncProcessingType.ORGANIZATIONS);
						appLogger.info("Organizations delete upload queued in admin UI. electionId={}", election.getElectionId());
					} else {
						getSession().error(getString(OrganizationsResourceKeys.PROCESSING_IN_PROGRESS));
						appLogger.info("Organizations delete upload queue request skipped because another process is already running. electionId={}", election.getElectionId());
					}
				} catch (CensusValidationException cve) {
					handleValidationError(cve);
					return;
					} catch (Exception e) {
						appLogger.error(e.getMessage(), e);
						getSession().error(getString(OrganizationsResourceKeys.ERR_BIF));
						return;
					}
					setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(election.getElectionId()));
				}
			});

		final FileUploadField orgUpsertFileInput = new FileUploadField("orgUpsertFileInput");
		orgUpsertFileInput.add(new OrganizationExcelFileValidator(UploadType.UPSERT));
		add(orgUpsertFileInput);
		Model<Boolean> overwriteAllOrganizationsModel = Model.of(Boolean.FALSE);
		add(new CheckBox("overwriteAllOrganizations", overwriteAllOrganizationsModel));
		WebMarkupContainer upsertErrorsContainer = new WebMarkupContainer("upsertErrorsContainer");
		upsertErrorsContainer.setOutputMarkupPlaceholderTag(true);
		upsertErrorsContainer.setVisible(false);
		DownloadLink downloadUpsertErrors = new DownloadLink("downloadUpsertErrors", new PropertyModel<>(this, "upsertErrorsFile"));
		downloadUpsertErrors.setDeleteAfterDownload(false);
		upsertErrorsContainer.add(downloadUpsertErrors);
		add(upsertErrorsContainer);

		add(new SubmitLink("uploadUpsertOrganizationsFileButton") {
			private static final long serialVersionUID = -6896034199971155470L;

			@Override
			public void onSubmit() {
				upsertErrorsContainer.setVisible(false);
				upsertErrorsFile = null;
				if (orgUpsertFileInput.getFileUpload() == null) {
					getSession().error(getString(KEY_ORGANIZATIONS_MANAGEMENT_ERR_NO_FILE));
					return;
				}
				try {
					byte[] content = orgUpsertFileInput.getFileUpload().getBytes();
					String contentType = orgUpsertFileInput.getFileUpload().getContentType();
					boolean overwriteAllOrganizations = Boolean.TRUE.equals(overwriteAllOrganizationsModel.getObject());
					OrganizationBulkImportResult validationResult = AppContext.getInstance().getManagerBeanRemote().validateOrganizationsUpsertExcelDetailed(contentType, content);
					if (validationResult != null && validationResult.hasErrors()) {
						prepareUpsertErrorReport(upsertErrorsContainer, validationResult);
						getSession().error(getString("organizationsManagementUpsertValidationErrors"));
						return;
					}
					bulkDeleteActionValidator.validateUpsertOverwriteBeforeSubmit(contentType, election.getElectionId(), content, overwriteAllOrganizations);
					boolean queued = AppContext.getInstance().getManagerBeanRemote().queueOrganizationsUpsertFromExcel(contentType, election.getElectionId(), content, overwriteAllOrganizations,
							SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					if (queued) {
						SecurityUtils.markAsyncCompletionFeedbackPending(election.getElectionId(), AsyncProcessingType.ORGANIZATIONS);
						appLogger.info("Organizations upsert upload queued in admin UI. electionId={}, overwriteAll={}", election.getElectionId(), overwriteAllOrganizations);
					} else {
						getSession().error(getString(OrganizationsResourceKeys.PROCESSING_IN_PROGRESS));
						appLogger.info("Organizations upsert upload queue request skipped because another process is already running. electionId={}", election.getElectionId());
					}
				} catch (CensusValidationException cve) {
					handleValidationError(cve);
					return;
				} catch (Exception ioe) {
					appLogger.error(ioe.getMessage(), ioe);
					getSession().error(getString(OrganizationsResourceKeys.ERR_BIF));
					return;
				}
				setResponsePage(ElectionOrganizationsDashboard.class, UtilsParameters.getId(election.getElectionId()));
			}
		});

		DownloadLink downloadAddOrganizationsExample = new DownloadLink("downloadAddOrganizationsExample", new PropertyModel<>(this, "addOrganizationsExampleFile"));
		downloadAddOrganizationsExample.setDeleteAfterDownload(false);
		downloadAddOrganizationsExample.setVisible(addOrganizationsExampleFile != null && addOrganizationsExampleFile.exists());
		add(downloadAddOrganizationsExample);

		DownloadLink downloadDeleteOrganizationsExample = new DownloadLink("downloadDeleteOrganizationsExample", new PropertyModel<>(this, "deleteOrganizationsExampleFile"));
		downloadDeleteOrganizationsExample.setDeleteAfterDownload(false);
		downloadDeleteOrganizationsExample.setVisible(deleteOrganizationsExampleFile != null && deleteOrganizationsExampleFile.exists());
		add(downloadDeleteOrganizationsExample);

		DownloadLink downloadDebtorsOrganizationsExample = new DownloadLink("downloadDebtorsOrganizationsExample", new PropertyModel<>(this, "debtorsOrganizationsExampleFile"));
		downloadDebtorsOrganizationsExample.setDeleteAfterDownload(false);
		downloadDebtorsOrganizationsExample.setVisible(debtorsOrganizationsExampleFile != null && debtorsOrganizationsExampleFile.exists());
		add(downloadDebtorsOrganizationsExample);
	}

	private void handleValidationError(CensusValidationException cve) {
		appLogger.error(cve.getMessage(), cve);
		if (cve.getMessage().matches(
				"organizationsManagementUploadMissingColumns|organizationsManagementUploadNoDataRows|organizationsManagementUploadFileError|organizationsManagementUploadUnknownFileType|organizationsManagementUploadDuplicateOrgId|organizationsManagementUpsertMissingColumns|organizationsManagementProcessingInProgress")) {
			getSession().error(getString(cve.getMessage()));
		} else if (cve.getMessage().matches("organizationsManagementUploadNullRequiredFields")) {
			getSession().error(new StringResourceModel(cve.getMessage()).setParameters(cve.getErrorRow()).getString());
		} else if (cve.getMessage().matches("organizationsManagementMissingOrgIds")) {
			getSession().error(new StringResourceModel(cve.getMessage()).setParameters(cve.getErrorInfo()).getString());
		} else if (cve.getMessage().matches("organizationsManagementDeleteBlockedByNominations|organizationsManagementDeleteBlockedBySupports")) {
			getSession().error(new StringResourceModel(cve.getMessage()).setParameters(cve.getErrorInfo()).getString());
		} else {
			getSession().error(getString(OrganizationsResourceKeys.ERR_BIF));
		}
	}

	public File getRemoveErrorsFile() {
		return removeErrorsFile;
	}

	public File getUpsertErrorsFile() {
		return upsertErrorsFile;
	}

	public File getAddOrganizationsExampleFile() {
		return addOrganizationsExampleFile;
	}

	public File getDeleteOrganizationsExampleFile() {
		return deleteOrganizationsExampleFile;
	}

	public File getDebtorsOrganizationsExampleFile() {
		return debtorsOrganizationsExampleFile;
	}

	private void prepareUpsertErrorReport(WebMarkupContainer upsertErrorsContainer, OrganizationBulkImportResult validationResult) {
		if (upsertErrorsContainer == null || validationResult == null || validationResult.getErrorReport() == null || validationResult.getErrorReport().length == 0) {
			return;
		}
		try {
			upsertErrorsFile = writeReport("errores_alta_actualizacion_organizaciones", validationResult.getErrorReport());
			upsertErrorsContainer.setVisible(upsertErrorsFile != null && upsertErrorsFile.exists());
		} catch (IOException e) {
			upsertErrorsFile = null;
			upsertErrorsContainer.setVisible(false);
			appLogger.error("Unable to prepare organizations upsert error report for download", e);
		}
	}

	private File writeReport(String prefix, byte[] content) throws IOException {
		File out;
		if (JBOSSTEMPURI != null && !JBOSSTEMPURI.trim().isEmpty()) {
			out = new File(JBOSSTEMPURI + prefix + "_" + UUID.randomUUID() + ".xlsx");
		} else {
			out = File.createTempFile(prefix + "_" + UUID.randomUUID(), ".xlsx");
		}
		try (FileOutputStream fos = new FileOutputStream(out)) {
			fos.write(content);
		}
		return out;
	}

	private static String resolveJbossTempUri() {
		try {
			return EJBFactory.getJbossTempUri();
		} catch (Exception e) {
			return "";
		}
	}
}
