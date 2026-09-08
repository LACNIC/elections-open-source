package net.lacnic.elections.adminweb.ui.publicresource;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;

import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.util.string.StringValue;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.services.publicelection.PublicElectionPhotoSnapshot;

public class CandidatePublicPhotoResource extends AbstractResource {

	private static final long serialVersionUID = -202145791821051094L;
	private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

	@Override
	protected ResourceResponse newResourceResponse(Attributes attributes) {
		Long electionId = resolveElectionId(attributes);
		Long candidateId = resolveCandidateId(attributes);
		if (electionId == null || electionId.longValue() <= 0L || candidateId == null || candidateId.longValue() <= 0L) {
			return buildNotFoundResponse();
		}

		try {
			PublicElectionPhotoSnapshot snapshot = AppContext.getInstance().getMonitorBeanRemote()
					.getPublicElectionPhotoSnapshot(electionId);
			PublicElectionPhotoSnapshot.CandidatePhotoData photoData = resolvePhotoData(snapshot, candidateId);
			byte[] pictureInfo = photoData != null ? photoData.getPictureBytes() : null;
			if (pictureInfo == null || pictureInfo.length == 0) {
				return buildNotFoundResponse();
			}

			String contentType = resolveImageContentType(photoData.getPictureExtension(), pictureInfo);
			return buildOkResponse(pictureInfo, contentType);
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

	private Long resolveCandidateId(Attributes attributes) {
		if (attributes == null || attributes.getParameters() == null) {
			return null;
		}
		StringValue candidateIdValue = attributes.getParameters().get("candidateId");
		return candidateIdValue != null ? candidateIdValue.toLongObject() : null;
	}

	private ResourceResponse buildOkResponse(byte[] content, String contentType) {
		ResourceResponse response = new ResourceResponse();
		response.setStatusCode(200);
		response.setContentType(contentType);
		response.setCacheDuration(Duration.ofMinutes(5));
		response.setWriteCallback(new WriteCallback() {
			@Override
			public void writeData(Attributes attributes) throws IOException {
				attributes.getResponse().write(content);
			}
		});
		return response;
	}

	private ResourceResponse buildNotFoundResponse() {
		ResourceResponse response = new ResourceResponse();
		response.setStatusCode(404);
		response.setCacheDuration(Duration.ZERO);
		return response;
	}

	private PublicElectionPhotoSnapshot.CandidatePhotoData resolvePhotoData(PublicElectionPhotoSnapshot snapshot, Long candidateId) {
		if (snapshot == null || candidateId == null || snapshot.getCandidates() == null) {
			return null;
		}
		for (PublicElectionPhotoSnapshot.CandidatePhotoData photoData : snapshot.getCandidates()) {
			if (photoData == null || photoData.getCandidateId() == null) {
				continue;
			}
			if (candidateId.equals(photoData.getCandidateId())) {
				return photoData;
			}
		}
		return null;
	}

	private String resolveImageContentType(String pictureExtension, byte[] pictureInfo) {
		String extension = pictureExtension != null ? pictureExtension.trim().toLowerCase(Locale.ROOT) : "";
		switch (extension) {
		case "jpg":
		case "jpeg":
			return "image/jpeg";
		case "png":
			return "image/png";
		case "gif":
			return "image/gif";
		case "webp":
			return "image/webp";
		default:
			return resolveContentTypeBySignature(pictureInfo);
		}
	}

	private String resolveContentTypeBySignature(byte[] content) {
		if (startsWith(content, (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47,
				(byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A)) {
			return "image/png";
		}
		if (startsWith(content, (byte) 0xFF, (byte) 0xD8, (byte) 0xFF)) {
			return "image/jpeg";
		}
		if (startsWith(content, (byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x38)) {
			return "image/gif";
		}
		return DEFAULT_CONTENT_TYPE;
	}

	private boolean startsWith(byte[] content, byte... signature) {
		if (content == null || signature == null || content.length < signature.length) {
			return false;
		}
		for (int i = 0; i < signature.length; i++) {
			if (content[i] != signature[i]) {
				return false;
			}
		}
		return true;
	}
}
