package net.lacnic.elections.adminweb.ui.token;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.ui.token.AcceptNominationActionBannerFactory.AcceptNominationActionBanner;
import net.lacnic.elections.adminweb.ui.token.page.GenericAcceptNominationTasksPage;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.pre.ElectionTaskKey;

import net.lacnic.elections.adminweb.wicket.util.MarkupLiterals;
public class AcceptNominationActionBannerPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final String token;

	public AcceptNominationActionBannerPanel(String id, String token, AcceptNominationTaskResolution resolution) {
		super(id);
		this.token = token;

		AcceptNominationActionBanner banner = new AcceptNominationActionBannerFactory().build(resolution);

		WebMarkupContainer bannerCard = new WebMarkupContainer("bannerCard");
		bannerCard.add(AttributeModifier.replace(MarkupLiterals.HTML_ATTRIBUTE_CLASS,  banner.getToneCssClass()));
		add(bannerCard);

		StringResourceModel messageModel = new StringResourceModel(banner.getMessageKey(), this, null);
		if (banner.getMessageParams() != null && banner.getMessageParams().length > 0) {
			messageModel.setParameters(banner.getMessageParams());
		}
		bannerCard.add(new Label("bannerMessage", messageModel));
		String targetTaskLabel = resolveTaskLabel(banner);

		StringResourceModel taskHintModel = new StringResourceModel("acceptNominationActionBannerNextTask", this, null).setParameters(targetTaskLabel);
		Label bannerTaskHint = new Label("bannerTaskHint", taskHintModel);
		bannerTaskHint.setVisible(targetTaskLabel != null && !targetTaskLabel.isEmpty());
		bannerCard.add(bannerTaskHint);

		BookmarkablePageLink<Void> bannerAction = new BookmarkablePageLink<Void>("bannerAction", GenericAcceptNominationTasksPage.class, buildPageParameters(banner));
		bannerAction.setVisible(banner.getTargetTaskKey() != null);
		StringResourceModel ctaModel = new StringResourceModel("acceptNominationActionBannerCtaWithTask", this, null).setParameters(targetTaskLabel);
		bannerAction.add(new Label("bannerActionLabel", ctaModel));
		bannerCard.add(bannerAction);
	}

	private PageParameters buildPageParameters(AcceptNominationActionBanner banner) {
		PageParameters params = UtilsParameters.getToken(token);
		ElectionTaskKey taskKey = banner != null ? banner.getTargetTaskKey() : null;
		if (taskKey != null) {
			params.add(AcceptNominationTaskResolver.TASK_PARAM, taskKey.name());
		}
		AcceptNominationTaskMode mode = banner != null && banner.getTargetMode() != null ? banner.getTargetMode() : AcceptNominationTaskMode.COMPLETE;
		params.add(AcceptNominationTaskResolver.MODE_PARAM, mode.getParameterValue());
		return params;
	}

	private String resolveTaskLabel(AcceptNominationActionBanner banner) {
		if (banner == null || banner.getTargetTaskKey() == null) {
			return "";
		}
		return getString("electionTaskKey." + banner.getTargetTaskKey().name());
	}
}
