package net.lacnic.elections.adminweb.ui.admin.ipaccess;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.IpAccess;


public class IpAccessListPanel extends Panel {

	private static final long serialVersionUID = -8554113800494186242L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");	


	public IpAccessListPanel(String id) {
		super(id);

		List<IpAccess> ipAccessList = AppContext.getInstance().getManagerBeanRemote().getAllDisabledIPs();

		final ListView<IpAccess> ipAccessListView = new ListView<IpAccess>("ipAccessList", ipAccessList) {
			private static final long serialVersionUID = 2146073776795909288L;

			@Override
			protected void populateItem(final ListItem<IpAccess> item) {
				try {
					final IpAccess ipAccess = item.getModelObject();
					item.add(new Label("ip", ipAccess.getIp()));
					item.add(new Label("attemptCount", ipAccess.getAttemptCount()));
					item.add(new Label("firstAttemptDate", ipAccess.getFirstAttemptDate()));
					item.add(new Label("lastAttemptDate", ipAccess.getLastAttemptDate()));
				} catch (Exception e) {
					appLogger.error(e);
				}
			}
		};
		add(ipAccessListView);
	}

}
