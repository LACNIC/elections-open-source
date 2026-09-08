package net.lacnic.elections.adminweb.ui.publicresource;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

public class CandidatePublicPhotoResourceReference extends ResourceReference {

	private static final long serialVersionUID = 2445494201710189294L;
	private static final CandidatePublicPhotoResource RESOURCE = new CandidatePublicPhotoResource();

	public CandidatePublicPhotoResourceReference() {
		super(CandidatePublicPhotoResourceReference.class, "candidate-public-photo");
	}

	@Override
	public IResource getResource() {
		return RESOURCE;
	}
}
