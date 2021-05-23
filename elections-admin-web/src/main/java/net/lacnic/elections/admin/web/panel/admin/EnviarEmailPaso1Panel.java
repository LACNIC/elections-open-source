package net.lacnic.elections.admin.web.panel.admin;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.dashboard.admin.DashboardEnviarEmailPaso2;
import net.lacnic.elections.admin.dashboard.admin.DashboardPlantillasVer;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.TemplateEleccion;

public class EnviarEmailPaso1Panel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public EnviarEmailPaso1Panel(String id, TemplateEleccion template) {
		super(id);
		try {

			Form<Void> f = new Form<>("form");
			add(f);

			TextField<String> remitente = new TextField<>("remitente", new PropertyModel<>(template, "eleccion.remitentePorDefecto"));
			f.add(remitente);
			setOutputMarkupId(true);

			TextField<String> asuntoEspanol = new TextField<>("asuntoEspanol", new PropertyModel<>(template, "asuntoES"));
			asuntoEspanol.add(StringValidator.maximumLength(1000));
			asuntoEspanol.setRequired(true);
			f.add(asuntoEspanol);

			TextField<String> asuntoIngles = new TextField<>("asuntoIngles", new PropertyModel<>(template, "asuntoEN"));
			asuntoIngles.add(StringValidator.maximumLength(1000));
			asuntoIngles.setRequired(true);
			f.add(asuntoIngles);

			TextField<String> asuntoPortugues = new TextField<>("asuntoPortugues", new PropertyModel<>(template, "asuntoPT"));
			asuntoPortugues.add(StringValidator.maximumLength(1000));
			asuntoPortugues.setRequired(true);
			f.add(asuntoPortugues);

			TextArea<String> cuerpoEspanol = new TextArea<>("cuerpoEspanol", new PropertyModel<>(template, "cuerpoES"));
			cuerpoEspanol.add(StringValidator.maximumLength(2000));
			cuerpoEspanol.setRequired(true);
			f.add(cuerpoEspanol);

			TextArea<String> cuerpoIngles = new TextArea<>("cuerpoIngles", new PropertyModel<>(template, "cuerpoEN"));
			cuerpoIngles.add(StringValidator.maximumLength(2000));
			cuerpoIngles.setRequired(true);
			f.add(cuerpoIngles);

			TextArea<String> cuerpoPortugues = new TextArea<>("cuerpoPortugues", new PropertyModel<>(template, "cuerpoPT"));
			cuerpoPortugues.add(StringValidator.maximumLength(2000));
			cuerpoPortugues.setRequired(true);
			f.add(cuerpoPortugues);

			f.add(new Button("siguiente") {

				private static final long serialVersionUID = 1073607359256986749L;

				@Override
				public void onSubmit() {
					try {
						AppContext.getInstance().getManagerBeanRemote().modificarTemplateEleccion(template);
						Long idEleccion = template.getEleccion().getIdEleccion();
						AppContext.getInstance().getManagerBeanRemote().modificarTemplateEleccion(template);
						setResponsePage(new DashboardEnviarEmailPaso2(template, UtilsParameters.getId(idEleccion)));
					} catch (Exception e) {
						appLogger.error(e);
					}
				}
			});

			f.add(new Link<Void>("volver") {

				private static final long serialVersionUID = -1139916268825131175L;

				@Override
				public void onClick() {
					setResponsePage(DashboardPlantillasVer.class, UtilsParameters.getId(template.getEleccion().getIdEleccion()));
				}

			});

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}