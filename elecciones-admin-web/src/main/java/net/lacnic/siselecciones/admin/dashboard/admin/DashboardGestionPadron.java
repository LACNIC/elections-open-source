package net.lacnic.siselecciones.admin.dashboard.admin;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.web.bases.DashboardAdminBasePage;
import net.lacnic.siselecciones.admin.web.commons.GestionEleccionStatusPanel;
import net.lacnic.siselecciones.admin.web.panel.usuariopadron.CamposUsuarioPadronPanel;
import net.lacnic.siselecciones.admin.web.panel.usuariopadron.GestionListaUsuariosPadronPanel;
import net.lacnic.siselecciones.admin.web.panel.usuariopadron.SubirArchivoUsuarioPadronPanel;
import net.lacnic.siselecciones.admin.wicket.util.UtilsParameters;
import net.lacnic.siselecciones.dominio.Eleccion;
import net.lacnic.siselecciones.dominio.UsuarioPadron;
import net.lacnic.siselecciones.utils.ValidacionPadronException;

@AuthorizeInstantiation("siselecciones-only-one")
public class DashboardGestionPadron extends DashboardAdminBasePage {

	private static final long serialVersionUID = 1L;
	private Eleccion eleccion;
	private UsuarioPadron up = new UsuarioPadron();

	public DashboardGestionPadron(PageParameters params) {
		super(params);
		setEleccion(Contexto.getInstance().getManagerBeanRemote().obtenerEleccion(UtilsParameters.getIdAsLong(params)));
		add(new FeedbackPanel("feedback"));
		add(new GestionEleccionStatusPanel("tabPadron", eleccion));
		add(new PadronEleccionForm("eleccionPadronForm", eleccion));

		Form<Void> formUsuario = new Form<>("formUsuario");
		formUsuario.add(new CamposUsuarioPadronPanel("camposUsuarioPadronPanel", up));
		add(formUsuario);

		formUsuario.add(new Button("agregar") {
			private static final long serialVersionUID = -5396612472447055621L;

			@Override
			public void onSubmit() {
				super.onSubmit();
				try {
					Contexto.getInstance().getManagerBeanRemote().agregarUsuarioPadron(getEleccion().getIdEleccion(), getUp(), SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
					getSession().info(getString("censusManagementExito"));
					setResponsePage(DashboardGestionPadron.class, UtilsParameters.getId(getEleccion().getIdEleccion()));
				} catch (ValidacionPadronException e) {
					error(getString(e.getMessage()));
				}
			}
		});

		add(new AjaxLazyLoadPanel<GestionListaUsuariosPadronPanel>("gestionUsuarioPadron") {
			private static final long serialVersionUID = 8165854022863553760L;

			@Override
			public GestionListaUsuariosPadronPanel getLazyLoadComponent(String markupId) {
				return new GestionListaUsuariosPadronPanel(markupId, eleccion);
			}
		});

	}

	public final class PadronEleccionForm extends Form<Void> {

		private static final long serialVersionUID = 2351447413365706203L;

		public PadronEleccionForm(String id, final Eleccion eleccion) {
			super(id);

			SubirArchivoUsuarioPadronPanel camposEleccionPadronPanel = new SubirArchivoUsuarioPadronPanel("subirArchivoUsuarioPadronPanel", eleccion, DashboardGestionPadron.class);
			camposEleccionPadronPanel.setOutputMarkupId(true);
			add(camposEleccionPadronPanel);

			Button siguiente = new Button("siguiente") {
				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onSubmit() {
					try {
						setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			siguiente.setEnabled(getEleccion().isPadronSeteado());
			add(siguiente);

			Link<Void> saltar = new Link<Void>("saltar") {

				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						setResponsePage(DashboardGestionCandidatos.class, UtilsParameters.getId(eleccion.getIdEleccion()));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(saltar);

			Link<Void> atras = new Link<Void>("atras") {

				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onClick() {
					try {
						setResponsePage(DashboardGestionEleccion.class, UtilsParameters.getId(eleccion.getIdEleccion()));
					} catch (Exception e) {
						error(e.getMessage());
					}
				}
			};
			add(atras);

		}
	}

	public Eleccion getEleccion() {
		return eleccion;
	}

	public void setEleccion(Eleccion eleccion) {
		this.eleccion = eleccion;
	}

	public UsuarioPadron getUp() {
		return up;
	}

	public void setUp(UsuarioPadron up) {
		this.up = up;
	}
}