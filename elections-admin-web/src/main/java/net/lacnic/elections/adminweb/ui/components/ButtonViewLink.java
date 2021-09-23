package net.lacnic.elections.adminweb.ui.components;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.domain.ActivityType;
import net.lacnic.elections.utils.EJBFactory;


public abstract class ButtonViewLink extends Panel {

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private static final long serialVersionUID = -8151328146531390609L;
	private WebMarkupContainer container;
	private AjaxLink<Void> ask;
	private Link<Void> confirm;
	private AjaxLink<Void> cancel;


	public ButtonViewLink(String id, String voterLink,String userAdminId, ActivityType activityType, String description, String ip, Long electionId) {
		super(id);
		appLogger.info(voterLink);

		setOutputMarkupPlaceholderTag(true);

		container = new WebMarkupContainer("container");
		container.setOutputMarkupPlaceholderTag(true);
		container.setVisible(false);
		add(container);

		ask = new AjaxLink<Void>("ask") {
			private static final long serialVersionUID = -1763298278543164951L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				container.setVisible(true);
				ask.setVisible(false);
				target.add(ButtonViewLink.this);
				EJBFactory.getInstance().getElectionsManagerEJB().persistActivity(userAdminId,activityType,  description,  ip,  electionId);
				container.setVisible(true);
				ask.setVisible(false);
				setResponsePage(new RedirectPage(voterLink));

			}
		};
		ask.setOutputMarkupPlaceholderTag(true);
		add(ask);

		
		

	}

	public abstract void onConfirm();

}
