package net.lacnic.elections.admin.web.elecciones;

import java.util.Arrays;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.UrlValidator;

import net.lacnic.elections.admin.wicket.util.Time24HoursValidator;
import net.lacnic.elections.domain.ElectionCategory;
import net.lacnic.elections.domain.Election;

public class CamposEleccionDetallePanel extends Panel {

	private static final long serialVersionUID = -7217245542954325281L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	public CamposEleccionDetallePanel(String id, Election eleccion) {
		super(id);
		try {
			setOutputMarkupId(true);

			DropDownChoice<ElectionCategory> selectCategoria = new DropDownChoice<>("selectCategoria", new PropertyModel<>(eleccion, "categoria"), Arrays.asList(ElectionCategory.values()));
			selectCategoria.setRequired(true);
			add(selectCategoria);

			WebMarkupContainer titulosEleccion = new WebMarkupContainer("titulosEleccion");
			titulosEleccion.setOutputMarkupPlaceholderTag(true);
			titulosEleccion.setVisible(!eleccion.isOnlySp());
			add(titulosEleccion);

			TextField<String> tituloEspanol = new TextField<>("tituloEspanol", new PropertyModel<>(eleccion, "tituloEspanol"));
			tituloEspanol.add(StringValidator.maximumLength(1000));
			tituloEspanol.setRequired(true);
			add(tituloEspanol);

			TextField<String> tituloIngles = new TextField<>("tituloIngles", new PropertyModel<>(eleccion, "tituloIngles"));
			tituloIngles.add(StringValidator.maximumLength(1000));
			tituloIngles.setRequired(true);
			titulosEleccion.add(tituloIngles);

			TextField<String> tituloPortugues = new TextField<>("tituloPortugues", new PropertyModel<>(eleccion, "tituloPortugues"));
			tituloPortugues.add(StringValidator.maximumLength(1000));
			tituloPortugues.setRequired(true);
			titulosEleccion.add(tituloPortugues);

			WebMarkupContainer descripcionesEleccion = new WebMarkupContainer("descripcionesEleccion");
			descripcionesEleccion.setOutputMarkupPlaceholderTag(true);
			descripcionesEleccion.setVisible(!eleccion.isOnlySp());
			add(descripcionesEleccion);

			TextArea<String> descripcionEspanol = new TextArea<>("descripcionEspanol", new PropertyModel<>(eleccion, "descripcionEspanol"));
			descripcionEspanol.add(StringValidator.maximumLength(2000));
			descripcionEspanol.setRequired(true);
			add(descripcionEspanol);

			TextArea<String> descripcionIngles = new TextArea<>("descripcionIngles", new PropertyModel<>(eleccion, "descripcionIngles"));
			descripcionIngles.add(StringValidator.maximumLength(2000));
			descripcionIngles.setRequired(true);
			descripcionesEleccion.add(descripcionIngles);

			TextArea<String> descripcionPortugues = new TextArea<>("descripcionPortugues", new PropertyModel<>(eleccion, "descripcionPortugues"));
			descripcionPortugues.add(StringValidator.maximumLength(2000));
			descripcionPortugues.setRequired(true);
			descripcionesEleccion.add(descripcionPortugues);

			WebMarkupContainer urlsEleccion = new WebMarkupContainer("urlsEleccion");
			urlsEleccion.setOutputMarkupPlaceholderTag(true);
			urlsEleccion.setVisible(!eleccion.isOnlySp());
			add(urlsEleccion);

			TextField<String> linkEspanol = new TextField<>("linkEspanol", new PropertyModel<>(eleccion, "linkEspanol"));
			linkEspanol.add(StringValidator.maximumLength(1000));
			linkEspanol.add(new UrlValidator());
			linkEspanol.setRequired(true);
			add(linkEspanol);

			TextField<String> linkIngles = new TextField<>("linkIngles", new PropertyModel<>(eleccion, "linkIngles"));
			linkIngles.add(StringValidator.maximumLength(1000));
			linkIngles.add(new UrlValidator());
			linkIngles.setRequired(true);
			urlsEleccion.add(linkIngles);

			TextField<String> linkPortugues = new TextField<>("linkPortugues", new PropertyModel<>(eleccion, "linkPortugues"));
			linkPortugues.add(StringValidator.maximumLength(1000));
			linkPortugues.add(new UrlValidator());
			linkPortugues.setRequired(true);
			urlsEleccion.add(linkPortugues);

			AjaxCheckBox solosp = new AjaxCheckBox("solosp", new PropertyModel<>(eleccion, "solosp")) {

				private static final long serialVersionUID = 7324854310046050015L;

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					descripcionesEleccion.setVisible(!eleccion.isOnlySp());
					titulosEleccion.setVisible(!eleccion.isOnlySp());
					urlsEleccion.setVisible(!eleccion.isOnlySp());
					target.add(descripcionesEleccion);
					target.add(titulosEleccion);
					target.add(urlsEleccion);

				}
			};
			add(solosp);

			TextField<Integer> maxCandidatos = new TextField<>("maxCandidatos", new PropertyModel<>(eleccion, "maxCandidatos"));
			maxCandidatos.setRequired(true);
			maxCandidatos.add(RangeValidator.range(1, 100));
			add(maxCandidatos);

			TextField<Integer> diffUTC = new TextField<>("diffUTC", new PropertyModel<>(eleccion, "diffUTC"));
			diffUTC.setRequired(true);
			diffUTC.add(RangeValidator.range(1, 12));
			add(diffUTC);

			TextField<String> fechaInicio = new TextField<>("fechaInicio", new PropertyModel<>(eleccion, "auxFechaInicio"));
			fechaInicio.setRequired(true);
			add(fechaInicio);

			TextField<String> fechaFin = new TextField<>("fechaFin", new PropertyModel<>(eleccion, "auxFechaFin"));
			fechaFin.setRequired(true);
			add(fechaFin);

			TextField<String> horaInicio = new TextField<>("horaInicio", new PropertyModel<>(eleccion, "auxHoraInicio"));
			horaInicio.setRequired(true);
			horaInicio.add(new Time24HoursValidator());
			add(horaInicio);

			TextField<String> horaFin = new TextField<>("horaFin", new PropertyModel<>(eleccion, "auxHoraFin"));
			horaFin.setRequired(true);
			horaFin.add(new Time24HoursValidator());
			add(horaFin);

			EmailTextField remitentePorDefecto = new EmailTextField("remitentePorDefecto", new PropertyModel<>(eleccion, "remitentePorDefecto"));
			remitentePorDefecto.setRequired(true);
			add(remitentePorDefecto);

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

}