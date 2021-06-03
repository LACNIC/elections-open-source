package net.lacnic.elections.adminweb.dashboard.admin;

import java.io.File;

import javax.servlet.ServletContext;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.adminweb.app.SecurityUtils;
import net.lacnic.elections.adminweb.web.bases.DashboardAdminBasePage;
import net.lacnic.elections.adminweb.web.commons.OnOffSwitch;
import net.lacnic.elections.adminweb.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.utils.FilesUtils;

@AuthorizeInstantiation("elections-only-one")
public class DashboardConfiguracion extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1392182581021963077L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public DashboardConfiguracion(PageParameters params) {
		super(params);
		try {
			ServletContext context = ((WebApplication) WebApplication.get()).getServletContext();
			String filePath = context.getRealPath("/");
			
			
			long idEleccion = UtilsParameters.getIdAsLong(params);
			Election eleccion = AppContext.getInstance().getManagerBeanRemote().getElection(idEleccion);
			add(new Label("tituloEleccion", eleccion.getTitleSpanish()));

			add(new OnOffSwitch("linkVotacion", new PropertyModel<>(eleccion, "habilitadoLinkVotacion")) {

				private static final long serialVersionUID = -8757419944224081522L;

				@Override
				protected void accion() {
					AppContext.getInstance().getManagerBeanRemote().setVoteLinkStatus(idEleccion, eleccion.isVotingLinkAvailable(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
				}

			});

			OnOffSwitch linkResultados = new OnOffSwitch("linkResultados", new PropertyModel<>(eleccion, "habilitadoLinkResultado")) {

				private static final long serialVersionUID = -3214185498258791153L;

				@Override
				protected void accion() {
					AppContext.getInstance().getManagerBeanRemote().setResultsLinkStatus(idEleccion, eleccion.isResultLinkAvailable(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());

				}

			};
			linkResultados.setEnabled(eleccion.isFinished());
			add(linkResultados);

			OnOffSwitch habilitadoLinkAuditor = new OnOffSwitch("linkAuditoria", new PropertyModel<>(eleccion, "habilitadoLinkAuditor")) {

				private static final long serialVersionUID = -2218718810528752527L;

				@Override
				protected void accion() {
					AppContext.getInstance().getManagerBeanRemote().setAuditLinkStatus(idEleccion, eleccion.isAuditorLinkAvailable(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());

				}

			};
			habilitadoLinkAuditor.setEnabled(eleccion.isFinished());
			add(habilitadoLinkAuditor);

			OnOffSwitch solicitarRevision = new OnOffSwitch("linkRevision", new PropertyModel<>(eleccion, "solicitarRevision")) {

				private static final long serialVersionUID = 476112946371106638L;

				@Override
				protected void accion() {
					AppContext.getInstance().getManagerBeanRemote().requestElectionRevision(idEleccion, eleccion.isRevisionRequest(), SecurityUtils.getUserAdminId(), SecurityUtils.getClientIp());
					setResponsePage(DashboardConfiguracion.class, UtilsParameters.getId(idEleccion));
				}

			};
			add(solicitarRevision);

			Link<Void> atras = new Link<Void>("atras") {

				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						setResponsePage(SecurityUtils.getHomePage());
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(atras);
			
			File archivoPDF = AppContext.getInstance().getVoterBeanRemote().getElectionRolesRevisionDocument(filePath);
			add(new DownloadLink("documento",archivoPDF));

		} catch (Exception e) {
			appLogger.error(e);
		}

	}
}
