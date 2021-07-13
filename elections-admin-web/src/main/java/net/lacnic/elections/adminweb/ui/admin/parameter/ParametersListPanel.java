package net.lacnic.elections.adminweb.ui.admin.parameter;

import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Parameter;


public class ParametersListPanel extends Panel {

	private static final long serialVersionUID = -8554113800494186242L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private List<Parameter> parametersList;


	public ParametersListPanel(String id) {
		super(id);

		parametersList = AppContext.getInstance().getManagerBeanRemote().getParametersAll();

		final ListView<Parameter> parametersListView = new ListView<Parameter>("parametersList", parametersList) {

			private static final long serialVersionUID = 2146073776795909288L;

			@Override
			protected void populateItem(final ListItem<Parameter> item) {
				try {
					final Parameter parameter = item.getModelObject();

					item.add(new Label("key", parameter.getKey()));
					item.add(new Label("value", parameter.getValue()));

					BookmarkablePageLink<Void> editParameter = new BookmarkablePageLink<>("editParameter", EditParameterDashboard.class, UtilsParameters.getClaveId(parameter.getKey()));
					editParameter.setMarkupId("editParameter" + item.getIndex());
					item.add(editParameter);

					ButtonDeleteWithConfirmation remove = new ButtonDeleteWithConfirmation("remove", item.getIndex()) {
						private static final long serialVersionUID = -7529344711667630816L;

						@Override
						public void onConfirm() {					
							AppContext.getInstance().getManagerBeanRemote().removeParameter(parameter.getKey(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
							getSession().info(getString("advParameterExito"));
							setResponsePage(ParametersDashboard.class); 
						}
					};
					remove.setMarkupId("removeParameter" + item.getIndex());
					item.add(remove);

				} catch (Exception e) {
					appLogger.error(e);
				}
			}
		};
		add(parametersListView);
	}

}
