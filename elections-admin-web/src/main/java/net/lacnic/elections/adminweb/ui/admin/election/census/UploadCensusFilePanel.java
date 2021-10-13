package net.lacnic.elections.adminweb.ui.admin.election.census;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.protocol.http.WebApplication;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.exception.CensusValidationException;


public class UploadCensusFilePanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private byte[] censusFile;
	private String fileName;

	public UploadCensusFilePanel(String id, Election election, Class responsePage) {
		super(id);
		try {
			setOutputMarkupId(true);

			final FileUploadField censusFileInput = new FileUploadField("censusFileInput");
			add(censusFileInput);

			SubmitLink uploadCensusFileButton = new SubmitLink("uploadCensusFileButton") {
				private static final long serialVersionUID = 1204295872124958662L;

				@Override
				public void onSubmit() {
					if (censusFileInput.getFileUpload() == null)
						getSession().error(getString("censusManagementErrNoFile"));
					else {
						setCensusFile(censusFileInput.getFileUpload().getBytes());
						setFileName(censusFileInput.getFileUpload().getClientFileName());
						try {
							AppContext.getInstance().getManagerBeanRemote().updateElectionCensus(censusFileInput.getFileUpload().getContentType(), election.getElectionId(), getCensusFile(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
							getSession().info(getString("censusManagementSuccessUploadFile"));
							if (responsePage != null)
								setResponsePage(responsePage, UtilsParameters.getId(election.getElectionId()));
						} catch (CensusValidationException cve) {
							appLogger.error(cve);
							if(cve.getMessage().matches("censusManagementUploadMissingColumns|censusManagementUploadFileError|censusManagementUploadUnknownFileType")) {
								getSession().error(getString(cve.getMessage()));
							} else if (cve.getMessage().matches("censusManagementUploadNullRequiredFields|censusManagementUploadWrongVoteAmount")) {
								getSession().error(new StringResourceModel(cve.getMessage()).setParameters(cve.getErrorInfo(), cve.getErrorRow()).getString());
							} else if (cve.getMessage().matches("censusManagementUploadWrongLanguage|censusManagementUploadWrongEmail|censusManagementUploadDuplicateEmail|censusManagementUploadWrongCountry")) {
								getSession().error(new StringResourceModel(cve.getMessage()).setParameters(cve.getErrorInfo(), cve.getErrorRow()).getString());
							} else {
								getSession().error(getString("censusManagementErrBif"));
							}
							setResponsePage(responsePage, UtilsParameters.getId(election.getElectionId()));
						} catch (Exception e) {
							if (e.getMessage().equals("BiffException"))
								getSession().error(getString("censusManagementErrBif"));
							appLogger.error(e);
							setResponsePage(responsePage, UtilsParameters.getId(election.getElectionId()));
						}
					}
				}
			};
			add(uploadCensusFileButton);

			ServletContext context = ((WebApplication) WebApplication.get()).getServletContext();
			String baseFilePath = context.getRealPath("/");
			File censusExampleFile = AppContext.getInstance().getManagerBeanRemote().exportCensusExample(baseFilePath);
			add(new DownloadLink("downloadExample", censusExampleFile));

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	public byte[] getCensusFile() {
		return censusFile;
	}

	public void setCensusFile(byte[] censusFile) {
		this.censusFile = censusFile;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
