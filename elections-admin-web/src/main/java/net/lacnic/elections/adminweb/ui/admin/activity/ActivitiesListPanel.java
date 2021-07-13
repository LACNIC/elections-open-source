package net.lacnic.elections.adminweb.ui.admin.activity;

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Activity;


public class ActivitiesListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private List<Activity> activitiesList; 


	public ActivitiesListPanel(String id, long electionId){
		super(id);
		if(electionId == -1)
			activitiesList = AppContext.getInstance().getManagerBeanRemote().getActivitiesAll();
		else
			activitiesList = AppContext.getInstance().getManagerBeanRemote().getElectionActivities(electionId);
		init(activitiesList);
	}

	private void init(List<Activity> activities) {
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

			final ListView<Activity> activitiesListView = new ListView<Activity>("activitiesList", activities) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Activity> item) {
					final Activity actual = item.getModelObject();
					try {
						item.add(new Label("userName", actual.getUserName()));
						item.add(new Label("activityType", actual.getActivityType().toString()));
						item.add(new Label("electionId", actual.getElectionId()));
						item.add(new Label("ip", actual.getIp()));
						item.add(new Label("timestamp", sdf.format(actual.getTimestamp())));
						item.add(new MultiLineLabel("description", actual.getDescription()));
					} catch (Exception e) {
						appLogger.error(e);
						error(e.getMessage());
					}
				}
			};
			add(activitiesListView);
		} catch (Exception e) {
			appLogger.error(e);
			error(e.getMessage());
		}
	}

}
