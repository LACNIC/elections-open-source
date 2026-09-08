package net.lacnic.elections.adminweb.ui.admin.customization;

import org.apache.commons.io.FilenameUtils;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.validators.CandidatePictureUploadValidator;
import net.lacnic.elections.adminweb.ui.components.OnOffSwitch;
import net.lacnic.elections.adminweb.wicket.util.ImageResource;
import net.lacnic.elections.domain.Customization;

public class CustomizationPanel extends Panel {

	private static final long serialVersionUID = 6920524802771454293L;
	private static final String IMAGE_PATH = "image/";

	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private Customization customization;

	private byte[] contPicSmallLogo;
	private String picSmallLogo;
	private byte[] contPicBigLogo;
	private String picBigLogo;
	private byte[] contPicSymbol;
	private String picSymbol;
	private String siteTitle;
	private String loginTitle;
	private String homeHtml;

	public CustomizationPanel(String id) {
		super(id);

		String pictureFileExtension = "";

		customization = AppContext.getInstance().getManagerBeanRemote().getCustomization();

		picSmallLogo = customization.getPicSmallLogo();
		picBigLogo = customization.getPicBigLogo();
		picSymbol = customization.getPicSymbol();
		contPicSmallLogo = customization.getContPicSmallLogo();
		contPicBigLogo = customization.getContPicBigLogo();
		contPicSymbol = customization.getContPicSymbol();
		siteTitle = customization.getSiteTitle();
		loginTitle = customization.getLoginTitle();
		homeHtml = customization.getHomeHtml();

		Form<Void> form = new Form<>("customizationForm");
		add(form);

		try {
			if (contPicSmallLogo == null) {
				form.add(new ContextImage("picSmallLogo", IMAGE_PATH + picSmallLogo));
			} else {
				pictureFileExtension = FilenameUtils.getExtension(picSmallLogo);
				form.add(new NonCachingImage("picSmallLogo", new ImageResource(contPicSmallLogo, pictureFileExtension)));
			}

			if (contPicBigLogo == null) {
				form.add(new ContextImage("picBigLogo", IMAGE_PATH + picBigLogo));
			} else {
				pictureFileExtension = FilenameUtils.getExtension(picBigLogo);
				form.add(new NonCachingImage("picBigLogo", new ImageResource(contPicBigLogo, pictureFileExtension)));
			}

			if (contPicSymbol == null) {
				form.add(new ContextImage("picSymbol", IMAGE_PATH + picSymbol));
			} else {
				pictureFileExtension = FilenameUtils.getExtension(picSymbol);
				form.add(new NonCachingImage("picSymbol", new ImageResource(contPicSymbol, pictureFileExtension)));
			}

			final FileUploadField picSmallLogoUploadField = new FileUploadField("picSmallLogoUpload");
			form.add(picSmallLogoUploadField);

			final FileUploadField picBigLogoUploadField = new FileUploadField("picBigLogoUpload");
			form.add(picBigLogoUploadField);

			final FileUploadField picSymbolUploadField = new FileUploadField("picSymbolUpload");
			form.add(picSymbolUploadField);

			final TextField<String> siteTitleTextField = new TextField<>("siteTitle", new PropertyModel<>(CustomizationPanel.this, "siteTitle"));
			siteTitleTextField.setRequired(true);
			form.add(siteTitleTextField);

			final TextField<String> loginTitleTextField = new TextField<>("loginTitle", new PropertyModel<>(CustomizationPanel.this, "loginTitle"));
			loginTitleTextField.setRequired(true);
			form.add(loginTitleTextField);

			OnOffSwitch showHomeCtrl = new OnOffSwitch("advCustomizationShowHome", new PropertyModel<>(customization, "showHome")) {
				private static final long serialVersionUID = -3214185498258791153L;

				@Override
				protected void action() {
					customization.setShowHome(!customization.isShowHome());
				}
			};
			form.add(showHomeCtrl);

			TextArea<String> homehtmlTxtAr = new TextArea<>("homeHtml", new PropertyModel<>(customization, "homeHtml"));
			form.add(homehtmlTxtAr);

			form.add(new Button("save") {
				private static final long serialVersionUID = 4683058294127551505L;

				@Override
				public void onSubmit() {
					try {
						FileUpload fileUploadBigLogo = picBigLogoUploadField.getFileUpload();
						FileUpload fileUploadSmallLogo = picSmallLogoUploadField.getFileUpload();
						FileUpload fileUploadSymbol = picSymbolUploadField.getFileUpload();
						boolean error = false;

						if (fileUploadBigLogo != null) {
							CandidatePictureUploadValidator.PictureUploadResult validationResult = CandidatePictureUploadValidator.validateAndBuild(fileUploadBigLogo);
							if (!validationResult.isValid()) {
								error = true;
								handlePictureUploadValidationError(validationResult.getFailureReason());
							} else {
								customization.setPicBigLogo(fileUploadBigLogo.getClientFileName());
								customization.setContPicBigLogo(validationResult.getPictureInfo());
							}
						}

						if ((!error) && (fileUploadSmallLogo != null)) {
							CandidatePictureUploadValidator.PictureUploadResult validationResult = CandidatePictureUploadValidator.validateAndBuild(fileUploadSmallLogo);
							if (!validationResult.isValid()) {
								error = true;
								handlePictureUploadValidationError(validationResult.getFailureReason());
							} else {
								customization.setPicSmallLogo(fileUploadSmallLogo.getClientFileName());
								customization.setContPicSmallLogo(validationResult.getPictureInfo());
							}
						}

						if ((!error) && (fileUploadSymbol != null)) {
							CandidatePictureUploadValidator.PictureUploadResult validationResult = CandidatePictureUploadValidator.validateAndBuild(fileUploadSymbol);
							if (!validationResult.isValid()) {
								error = true;
								handlePictureUploadValidationError(validationResult.getFailureReason());
							} else {
								customization.setPicSymbol(fileUploadSymbol.getClientFileName());
								customization.setContPicSymbol(validationResult.getPictureInfo());
							}
						}

						if ((getLoginTitle() == null) || (getLoginTitle().compareTo("") == 0)) {
							setLoginTitle(getString("advCustomizationDefaultSiteTitle"));
						}
						if ((getSiteTitle() == null) || (getSiteTitle().compareTo("") == 0)) {
							setSiteTitle(getString("advCustomizationDefaultSiteTitle"));
						}
						customization.setLoginTitle(getLoginTitle());
						customization.setSiteTitle(getSiteTitle());

						if ((!error)) {
							AppContext.getInstance().getManagerBeanRemote().updateCustomization(customization);
							getSession().info(getString("advCustomizationEditSuccess"));
						}

					} catch (Exception e) {
						getSession().error(getString("advCustomizationProcessingError"));
					}
				}
			});

		} catch (Exception e) {
			appLogger.error(e.getMessage());
		}
	}

	private void handlePictureUploadValidationError(CandidatePictureUploadValidator.FailureReason failureReason) {
		if (failureReason == CandidatePictureUploadValidator.FailureReason.INVALID_SIZE) {
			getSession().error(getString("advCustomizationPhotoSizeError"));
			return;
		}
		if (failureReason == CandidatePictureUploadValidator.FailureReason.INVALID_FORMAT) {
			getSession().error(getString("advCustomizationExtensionError"));
			return;
		}
		getSession().error(getString("advCustomizationProcessingError"));
	}

	public Customization getCustomization() {
		return customization;
	}

	public void setCustomization(Customization customization) {
		this.customization = customization;
	}

	public byte[] getContPicSmallLogo() {
		return contPicSmallLogo;
	}

	public void setContPicSmallLogo(byte[] contPicSmallLogo) {
		this.contPicSmallLogo = contPicSmallLogo;
	}

	public String getPicSmallLogo() {
		return picSmallLogo;
	}

	public void setPicSmallLogo(String picSmallLogo) {
		this.picSmallLogo = picSmallLogo;
	}

	public byte[] getContPicBigLogo() {
		return contPicBigLogo;
	}

	public void setContPicBigLogo(byte[] contPicBigLogo) {
		this.contPicBigLogo = contPicBigLogo;
	}

	public String getPicBigLogo() {
		return picBigLogo;
	}

	public void setPicBigLogo(String picBigLogo) {
		this.picBigLogo = picBigLogo;
	}

	public byte[] getContPicSymbol() {
		return contPicSymbol;
	}

	public void setContPicSymbol(byte[] contPicSymbol) {
		this.contPicSymbol = contPicSymbol;
	}

	public String getPicSymbol() {
		return picSymbol;
	}

	public void setPicSymbol(String picSymbol) {
		this.picSymbol = picSymbol;
	}

	public String getSiteTitle() {
		return siteTitle;
	}

	public void setSiteTitle(String siteTitle) {
		this.siteTitle = siteTitle;
	}

	public String getLoginTitle() {
		return loginTitle;
	}

	public void setLoginTitle(String loginTitle) {
		this.loginTitle = loginTitle;
	}

	public String getHomeHtml() {
		return homeHtml;
	}

	public void setHomeHtml(String homeHtml) {
		this.homeHtml = homeHtml;
	}

}
