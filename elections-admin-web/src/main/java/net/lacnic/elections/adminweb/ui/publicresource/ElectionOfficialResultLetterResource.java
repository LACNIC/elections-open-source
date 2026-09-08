package net.lacnic.elections.adminweb.ui.publicresource;

import java.io.IOException;
import java.time.Duration;

import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.string.StringValue;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.ui.commons.ElectionResultLetterSupport;
import net.lacnic.elections.domain.LanguageCode;
import net.lacnic.elections.domain.services.publicelection.PublicElectionOfficialResultSnapshot;

public class ElectionOfficialResultLetterResource extends AbstractResource {

	private static final long serialVersionUID = -7176239069251366935L;

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		Long electionId = resolveElectionId(attributes);
		LanguageCode languageCode = resolveLanguageCode(attributes);
		if (electionId == null || electionId.longValue() <= 0L) {
			return buildNotFoundResponse();
		}

		try {
			PublicElectionOfficialResultSnapshot snapshot = AppContext.getInstance().getMonitorBeanRemote()
					.getPublicElectionOfficialResultSnapshot(electionId);
			PublicElectionOfficialResultSnapshot.OfficialResultLetterData letterData = snapshot != null
					? snapshot.getResultLetterData(languageCode)
					: null;
			byte[] content = letterData != null ? letterData.getContent() : null;
			if (content == null || content.length == 0) {
				return buildNotFoundResponse();
			}

			LanguageCode fileLanguageCode = letterData.getLanguageCode() != null ? letterData.getLanguageCode() : languageCode;
			String contentType = ElectionResultLetterSupport.resolveContentType(content);
			String fileName = ElectionResultLetterSupport.resolveFileName(electionId.longValue(), fileLanguageCode, content);
			String cacheBuster = resolveCacheBuster(attributes);
			if (hasText(cacheBuster)) {
				fileName = ElectionResultLetterSupport.appendUniqueSuffix(fileName, cacheBuster);
			}

			ResourceResponse response = new ResourceResponse();
			response.setStatusCode(200);
			response.setContentType(contentType);
			response.setCacheDuration(Duration.ofMinutes(5));
			response.setContentDisposition(ContentDisposition.ATTACHMENT);
			response.setFileName(fileName);
			response.setContentLength(content.length);
			response.setWriteCallback(new WriteCallback() {
				@Override
				public void writeData(Attributes attributes) throws IOException {
					attributes.getResponse().write(content);
				}
			});
			return response;
		} catch (Exception e) {
			return buildNotFoundResponse();
		}
	}

	private Long resolveElectionId(Attributes attributes) {
		if (attributes == null || attributes.getParameters() == null) {
			return null;
		}
		StringValue electionIdValue = attributes.getParameters().get("electionId");
		return electionIdValue != null ? electionIdValue.toLongObject() : null;
	}

	private LanguageCode resolveLanguageCode(Attributes attributes) {
		if (attributes == null || attributes.getParameters() == null) {
			return LanguageCode.SP;
		}
		return LanguageCode.fromValueOrDefault(attributes.getParameters().get("lang").toString(""), LanguageCode.SP);
	}

	private String resolveCacheBuster(Attributes attributes) {
		if (attributes == null || attributes.getParameters() == null) {
			return null;
		}
		return attributes.getParameters().get("ts").toString("");
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private ResourceResponse buildNotFoundResponse() {
		ResourceResponse response = new ResourceResponse();
		response.setStatusCode(404);
		response.setCacheDuration(Duration.ZERO);
		return response;
	}
}
