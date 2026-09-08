package net.lacnic.elections.adminweb.ui.home;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ResourceLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.lang.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.bases.DashboardPublicBasePage;
import net.lacnic.elections.utils.PublicPhotoResizeProcessor;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class PublicPhotoResizeDashboard extends DashboardPublicBasePage {

	private static final long serialVersionUID = 5407314255722391645L;
	private static final Logger appLogger = LoggerFactory.getLogger("webAdminAppLogger");

	private byte[] processedPhotoBytes;
	private String processedPhotoFileName;
	private boolean captchaEnabled;

	public PublicPhotoResizeDashboard(PageParameters params) {
		super(params);

		add(new FeedbackPanel("feedbackPanel"));

		Form<Void> resizeForm = new Form<>("resizeForm");
		resizeForm.setMultiPart(true);
		resizeForm.setMaxSize(Bytes.bytes(PublicPhotoResizeProcessor.MAX_INPUT_SIZE_BYTES));
		add(resizeForm);

		FileUploadField sourcePhoto = new FileUploadField("sourcePhoto");
		resizeForm.add(sourcePhoto);

		String dataSiteKey = AppContext.getInstance().getManagerBeanRemote().getDataSiteKey();
		captchaEnabled = AppContext.getInstance().getManagerBeanRemote().isShowCaptcha() && StringUtils.isNotBlank(dataSiteKey);

		WebMarkupContainer captcha = new WebMarkupContainer("reCaptcha");
		captcha.add(new AttributeModifier("data-sitekey", StringUtils.defaultString(dataSiteKey)));
		captcha.setVisibilityAllowed(captchaEnabled);
		resizeForm.add(captcha);

		resizeForm.add(new Button("processPhotoButton") {
			private static final long serialVersionUID = -8958354991121884848L;

			@Override
			public void onSubmit() {
				if (!validateCaptcha()) {
					clearProcessedPhoto();
					return;
				}

				FileUpload upload = sourcePhoto.getFileUpload();
				PublicPhotoResizeProcessor.ProcessingResult result = PublicPhotoResizeProcessor.process(
						upload != null ? upload.getBytes() : null,
						upload != null ? upload.getClientFileName() : null);
				if (!result.isSuccess()) {
					handleFailure(result.getFailureReason());
					clearProcessedPhoto();
					return;
				}

				processedPhotoBytes = result.getOutputBytes();
				processedPhotoFileName = result.getOutputFileName();
				info(getString("publicPhotoResizeSuccess"));
			}
		});

		Image previewImage = new Image("previewImage", buildInlinePhotoResource()) {
			private static final long serialVersionUID = 491832384240093284L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(hasProcessedPhoto());
			}
		};
		previewImage.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS, "img-fluid img-thumbnail rounded"));
		previewImage.add(AttributeModifier.replace("alt", getString("publicPhotoResizePreviewAlt")));
		add(previewImage);

		Label previewPlaceholder = new Label("previewPlaceholder", getString("publicPhotoResizePreviewPlaceholder")) {
			private static final long serialVersionUID = 6977895363573535159L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!hasProcessedPhoto());
			}
		};
		add(previewPlaceholder);

		Label outputInfo = new Label("outputInfo", new LoadableDetachableModel<String>() {
			private static final long serialVersionUID = -7678475947288617184L;

			@Override
			protected String load() {
				if (!hasProcessedPhoto()) {
					return "";
				}
				long sizeKb = processedPhotoBytes.length / 1024L;
				return getString("publicPhotoResizeSummary", null, String.valueOf(sizeKb));
			}
		}) {
			private static final long serialVersionUID = 3689108895039533697L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(hasProcessedPhoto());
			}
		};
		add(outputInfo);

		ResourceLink<Void> downloadLink = new ResourceLink<>("downloadLink", buildDownloadPhotoResource()) {
			private static final long serialVersionUID = -2189871669768507418L;

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(hasProcessedPhoto());
			}
		};
		add(downloadLink);
	}

	@Override
	protected Class validateToken(PageParameters params) {
		return null;
	}

	private boolean validateCaptcha() {
		if (!captchaEnabled) {
			return true;
		}
		HttpServletRequest request = (HttpServletRequest) getRequest().getContainerRequest();
		String captchaResponse = request.getParameter("g-recaptcha-response");
		boolean valid = AppContext.getInstance().getManagerBeanRemote().isValidCaptchaResponse(captchaResponse);
		if (!valid) {
			error(getString("areYouRobot"));
		}
		return valid;
	}

	private void handleFailure(PublicPhotoResizeProcessor.FailureReason failureReason) {
		if (failureReason == null) {
			error(getString("publicPhotoResizeErrorProcessing"));
			return;
		}
		switch (failureReason) {
		case EMPTY_UPLOAD:
			error(getString("publicPhotoResizeErrorEmptyUpload"));
			break;
		case INPUT_TOO_LARGE:
			error(getString("publicPhotoResizeErrorInputTooLarge"));
			break;
		case INVALID_IMAGE_FORMAT:
			error(getString("publicPhotoResizeErrorInvalidImage"));
			break;
		case OUTPUT_TOO_LARGE:
			error(getString("publicPhotoResizeErrorOutputTooLarge"));
			break;
		case PROCESSING_ERROR:
		default:
			error(getString("publicPhotoResizeErrorProcessing"));
			break;
		}
	}

	private boolean hasProcessedPhoto() {
		return processedPhotoBytes != null && processedPhotoBytes.length > 0;
	}

	private void clearProcessedPhoto() {
		processedPhotoBytes = null;
		processedPhotoFileName = null;
	}

	private IResource buildInlinePhotoResource() {
		return new AbstractResource() {
			private static final long serialVersionUID = 5577027832906588781L;

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes) {
				ResourceResponse response = new ResourceResponse();
				if (!hasProcessedPhoto()) {
					response.setError(HttpServletResponse.SC_NOT_FOUND);
					return response;
				}
				response.setContentType("image/jpeg");
				response.setCacheDuration(Duration.ZERO);
				response.setContentDisposition(ContentDisposition.INLINE);
				response.setContentLength(processedPhotoBytes.length);
				response.setWriteCallback(new WriteCallback() {
					@Override
					public void writeData(Attributes attributes) {
						attributes.getResponse().write(processedPhotoBytes);
					}
				});
				return response;
			}
		};
	}

	private IResource buildDownloadPhotoResource() {
		return new AbstractResource() {
			private static final long serialVersionUID = 7048035115245006401L;

			@Override
			protected ResourceResponse newResourceResponse(Attributes attributes) {
				ResourceResponse response = new ResourceResponse();
				if (!hasProcessedPhoto()) {
					response.setError(HttpServletResponse.SC_NOT_FOUND);
					return response;
				}
				response.setContentType("image/jpeg");
				response.setCacheDuration(Duration.ZERO);
				response.setContentDisposition(ContentDisposition.ATTACHMENT);
				response.setFileName(StringUtils.defaultIfBlank(processedPhotoFileName, "photo_200x200.jpg"));
				response.setContentLength(processedPhotoBytes.length);
				response.setWriteCallback(new WriteCallback() {
					@Override
					public void writeData(Attributes attributes) {
						try {
							attributes.getResponse().write(processedPhotoBytes);
						} catch (Exception ex) {
							appLogger.error("Error writing resized photo to response", ex);
						}
					}
				});
				return response;
			}
		};
	}
}
