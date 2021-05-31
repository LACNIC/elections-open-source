package net.lacnic.elections.admin.web.panel.admin;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.elections.admin.app.AppContext;
import net.lacnic.elections.admin.app.SecurityUtils;
import net.lacnic.elections.admin.dashboard.admin.DashboardPlantillasVer;
import net.lacnic.elections.admin.web.commons.TipoTemplateValidator;
import net.lacnic.elections.admin.wicket.util.UtilsParameters;
import net.lacnic.elections.domain.ElectionEmailTemplate;

public class AddTemplateBasePanel extends Panel {

	private static final long serialVersionUID = -6525820489640825347L;
	private ElectionEmailTemplate template;

	public AddTemplateBasePanel(String id) {
		super(id);
		template = new ElectionEmailTemplate();

		Form<Void> form = new Form<>("form");
		add(form);

		final TextField<String> tipoTextField = new TextField<>("tipo", new PropertyModel<>(template, "tipoTemplate"));
		tipoTextField.setRequired(true);
		tipoTextField.add(new TipoTemplateValidator());
		form.add(tipoTextField);

		TextField<String> asuntoEspanol = new TextField<>("asuntoEspanol", new PropertyModel<>(template, "asuntoES"));
		asuntoEspanol.add(StringValidator.maximumLength(1000));
		asuntoEspanol.setRequired(true);
		form.add(asuntoEspanol);

		TextField<String> asuntoIngles = new TextField<>("asuntoIngles", new PropertyModel<>(template, "asuntoEN"));
		asuntoIngles.add(StringValidator.maximumLength(1000));
		asuntoIngles.setRequired(true);
		form.add(asuntoIngles);

		TextField<String> asuntoPortugues = new TextField<>("asuntoPortugues", new PropertyModel<>(template, "asuntoPT"));
		asuntoPortugues.add(StringValidator.maximumLength(1000));
		asuntoPortugues.setRequired(true);
		form.add(asuntoPortugues);

		TextArea<String> cuerpoEspanol = new TextArea<>("cuerpoEspanol", new PropertyModel<>(template, "cuerpoES"));
		cuerpoEspanol.add(StringValidator.maximumLength(2000));
		cuerpoEspanol.setRequired(true);
		form.add(cuerpoEspanol);

		TextArea<String> cuerpoIngles = new TextArea<>("cuerpoIngles", new PropertyModel<>(template, "cuerpoEN"));
		cuerpoIngles.add(StringValidator.maximumLength(2000));
		cuerpoIngles.setRequired(true);
		form.add(cuerpoIngles);

		TextArea<String> cuerpoPortugues = new TextArea<>("cuerpoPortugues", new PropertyModel<>(template, "cuerpoPT"));
		cuerpoPortugues.add(StringValidator.maximumLength(2000));
		cuerpoPortugues.setRequired(true);
		form.add(cuerpoPortugues);

		form.add(new Button("guardar") {

			private static final long serialVersionUID = 1073607359256986749L;

			@Override
			public void onSubmit() {
				boolean agregarP = AppContext.getInstance().getManagerBeanRemote().agregarPlantillaBase(template, SecurityUtils.getAdminId(), SecurityUtils.getIPClient());
				if (agregarP) {
					SecurityUtils.info(getString("baseMailTemplAddExitoAdd"));
					setResponsePage(DashboardPlantillasVer.class, UtilsParameters.getId(0L));
				} else {
					SecurityUtils.error(getString("baseMailTemplAddErrorAdd"));
				}
			}
		});

		form.add(new Link<Void>("volver") {

			private static final long serialVersionUID = 3552524670657820059L;

			@Override
			public void onClick() {
				setResponsePage(DashboardPlantillasVer.class, UtilsParameters.getId(0L));
			}
		});

	}

}