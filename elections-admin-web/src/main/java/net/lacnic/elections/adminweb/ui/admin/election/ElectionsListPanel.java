package net.lacnic.elections.adminweb.ui.admin.election;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardConfiguracion;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardEstadisticas;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardPlantillasVer;
import net.lacnic.elections.adminweb.dashboard.admin.DashboardReview;
import net.lacnic.elections.adminweb.ui.admin.election.auditors.ElectionAuditorsDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.candidates.ElectionCandidatesDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.census.ElectionCensusDashboard;
import net.lacnic.elections.adminweb.ui.admin.election.detail.ElectionDetailDashboard;
import net.lacnic.elections.adminweb.ui.components.ButtonDeleteWithConfirmation;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;


public class ElectionsListPanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private long userAdminId;


	public ElectionsListPanel(String id, PageParameters pars) {
		super(id);
		List<Election> electionsList = new ArrayList<>();
		Long authorizedElectionId = SecurityUtils.getAuthorizedElectionId();
		if (authorizedElectionId != 0) {
			electionsList = new ArrayList<>();
			electionsList.add(AppContext.getInstance().getManagerBeanRemote().getElection(authorizedElectionId));
		} else {
			if (UtilsParameters.isAll(pars)) {
				electionsList = AppContext.getInstance().getManagerBeanRemote().getElectionsAllOrderCreationDate();
			} else {
				electionsList = AppContext.getInstance().getManagerBeanRemote().getElectionsLightThisYear();
			}
		}
		init(electionsList);
	}

	private void init(List<Election> electionsList) {
		try {

			setOutputMarkupPlaceholderTag(true);

			final ListView<Election> dataViewElecciones = new ListView<Election>("electionsList", electionsList) {
				private static final long serialVersionUID = 1786359392545666490L;

				@Override
				protected void populateItem(ListItem<Election> item) {
					final Election currentElection = item.getModelObject();
					try {
						Label titles = new Label("titles", currentElection.getTitleSpanish());
						titles.setEscapeModelStrings(false);

						item.add(new Label("creationDate", new SimpleDateFormat("dd/MM/yyyy").format(currentElection.getCreationDate())));
						item.add(new Label("startDate", currentElection.getStartDateString()));
						item.add(new Label("endDate", currentElection.getEndDateString()));

						item.add(new BookmarkablePageLink<Void>("electionDetail", ViewElectionDashboard.class, UtilsParameters.getId(currentElection.getElectionId())).add(titles));

						BookmarkablePageLink<Void> editElection = new BookmarkablePageLink<>("editElection", ElectionDetailDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						editElection.setMarkupId("editElection" + currentElection.getElectionId());
						item.add(editElection);

						BookmarkablePageLink<Void> census = new BookmarkablePageLink<>("manageCensus", ElectionCensusDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						census.setMarkupId("electionCensus" + currentElection.getElectionId());
						item.add(census);

						BookmarkablePageLink<Void> candidates = new BookmarkablePageLink<>("candidates", ElectionCandidatesDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						candidates.setMarkupId("electionCandidates" + currentElection.getElectionId());
						item.add(candidates);

						BookmarkablePageLink<Void> auditors = new BookmarkablePageLink<>("auditors", ElectionAuditorsDashboard.class, UtilsParameters.getId(currentElection.getElectionId()));
						auditors.setMarkupId("electionAuditors" + currentElection.getElectionId());
						item.add(auditors);

						if (currentElection.isCandidatesSet())
							candidates.add(new AttributeModifier("class", "btn-circle btn-primary btn-sm"));
						if (currentElection.isElectorsSet())
							census.add(new AttributeModifier("class", "btn-circle btn-primary btn-sm"));
						if (currentElection.isAuditorsSet())
							auditors.add(new AttributeModifier("class", "btn-circle btn-primary btn-sm"));

						ButtonDeleteWithConfirmation buttonDeleteWithConfirmation = new ButtonDeleteWithConfirmation("removeElection", currentElection.getElectionId()) {
							private static final long serialVersionUID = -2068256428165604654L;

							@Override
							public void onConfirm() {
								try {
									boolean isNew = true;
									boolean isJoint = false;
									// Valido si la eleccion esta junta a otra, entonces NO puedo modificar la fecha de inicio
									if (currentElection.getElectionId() == 0) {
										isNew = true;
									} else {
										isNew = false;
										isJoint = AppContext.getInstance().getManagerBeanRemote().isJointElection(currentElection.getElectionId());
									};

									if ((!isNew) && (isJoint)) {
										SecurityUtils.error(getString("deleteElectionErrorJoint"));
										setResponsePage(ElectionsDashboard.class);
									} else {									
										AppContext.getInstance().getManagerBeanRemote().removeElection(currentElection.getElectionId(), currentElection.getTitleSpanish(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
										SecurityUtils.info(getString("deleteElectionSuccess"));
										setResponsePage(ElectionsDashboard.class);
									}
								} catch (Exception e) {
									appLogger.error(e);
								}
							}
						};
						item.add(buttonDeleteWithConfirmation);
						buttonDeleteWithConfirmation.setVisible(SecurityUtils.getAuthorizedElectionId() == 0);

						BookmarkablePageLink<Void> revisionEleccion = new BookmarkablePageLink<>("revision", DashboardReview.class, UtilsParameters.getId(currentElection.getElectionId()));
						revisionEleccion.setMarkupId("revision" + currentElection.getElectionId());
						revisionEleccion.setVisible(currentElection.isRevisionRequest());
						item.add(revisionEleccion);
						
						BookmarkablePageLink<Void> gestionDeMailLink = new BookmarkablePageLink<>("manageEmails", DashboardPlantillasVer.class, UtilsParameters.getId(currentElection.getElectionId()));
						gestionDeMailLink.setMarkupId("manageEmails" + currentElection.getElectionId());
						item.add(gestionDeMailLink); 
						
						BookmarkablePageLink<Void> seeElectionStats = new BookmarkablePageLink<>("seeStats", DashboardEstadisticas.class, UtilsParameters.getId(currentElection.getElectionId()));
						seeElectionStats.setMarkupId("seeStats" + currentElection.getElectionId());
						item.add(seeElectionStats);

						BookmarkablePageLink<Void> configuracion = new BookmarkablePageLink<>("configuration", DashboardConfiguracion.class, UtilsParameters.getId(currentElection.getElectionId()));
						configuracion.setMarkupId("configuration" + currentElection.getElectionId());
						item.add(configuracion);
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(dataViewElecciones);

		} catch (Exception e) {
			error(e.getMessage());
		}
	}

	public long getUserAdminId() {
		return userAdminId;
	}

}
