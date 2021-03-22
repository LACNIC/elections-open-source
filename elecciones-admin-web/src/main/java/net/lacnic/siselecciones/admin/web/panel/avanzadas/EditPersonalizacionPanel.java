package net.lacnic.siselecciones.admin.web.panel.avanzadas;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;

import net.lacnic.siselecciones.admin.app.Contexto;
import net.lacnic.siselecciones.admin.app.SecurityUtils;
import net.lacnic.siselecciones.admin.web.commons.LinkValidator;
import net.lacnic.siselecciones.admin.web.commons.OnOffSwitch;
import net.lacnic.siselecciones.admin.wicket.util.ImageResource;
import net.lacnic.siselecciones.dominio.Personalizacion;

public class EditPersonalizacionPanel extends Panel {

	private static final long serialVersionUID = 6920524802771454293L;

	private static final Logger appLogger = LogManager.getLogger("webAdminAppLogger");

	private Personalizacion personalizacion;

	private byte[] archivoPicSmallLogo;
	private String nombreArchivoSmallLogo;
	private byte[] archivoPicBigLogo;
	private String nombreArchivoBigLogo;
	private byte[] archivoPicSymbol;
	private String nombreArchivoSymbol;
	private String tituloSitio;
	private String tituloLogin;
	private String homeHtml;
	

	public EditPersonalizacionPanel(String id) {
		super(id);

		String ext = "";

		personalizacion = Contexto.getInstance().getManagerBeanRemote().getPersonalizacion();

		nombreArchivoSmallLogo = personalizacion.getPicSmallLogo();
		nombreArchivoBigLogo = personalizacion.getPicBigLogo();
		nombreArchivoSymbol = personalizacion.getPicSimbolo();
		archivoPicSmallLogo = personalizacion.getContPicSmallLogo();
		archivoPicBigLogo = personalizacion.getContPicBigLogo();
		archivoPicSymbol = personalizacion.getContPicSimbolo();
		tituloSitio = personalizacion.getTituloSitio();
		tituloLogin = personalizacion.getTituloLogin();
		homeHtml = personalizacion.getHomeHtml();
		
		Form<Void> form = new Form<>("formPersonalizacion");

		add(form);

		try {
			if (archivoPicSmallLogo == null) {
				form.add(new ContextImage("fotoSmallLogo","image/" + nombreArchivoSmallLogo));
			} else {
				ext = FilenameUtils.getExtension(nombreArchivoSmallLogo);
				form.add(new NonCachingImage("fotoSmallLogo", new ImageResource(archivoPicSmallLogo, ext)));
			}

			if (archivoPicBigLogo == null) {
				form.add(new ContextImage("fotoBigLogo","image/" + nombreArchivoBigLogo));
			} else {
				ext = FilenameUtils.getExtension(nombreArchivoBigLogo);
				form.add(new NonCachingImage("fotoBigLogo", new ImageResource(archivoPicBigLogo, ext)));
			}

			if (archivoPicSymbol == null) {
				form.add(new ContextImage("fotoSymbolLogo","image/" + nombreArchivoSymbol));
			} else {
				ext = FilenameUtils.getExtension(nombreArchivoSymbol);
				form.add(new NonCachingImage("fotoSymbolLogo", new ImageResource(archivoPicSymbol, ext)));
			}

			final FileUploadField fileFotoSmallLogo = new FileUploadField("smallLogoPic");
			form.add(fileFotoSmallLogo);

			final FileUploadField fileFotoBigLogo = new FileUploadField("bigLogoPic");
			form.add(fileFotoBigLogo);

			final FileUploadField fileFotoSymbol = new FileUploadField("symbolPic");
			form.add(fileFotoSymbol);
			
			final TextField<String> tituloSitioTextField = new TextField<>("tituloSitio", new PropertyModel<>(EditPersonalizacionPanel.this, "tituloSitio"));
			tituloSitioTextField.setRequired(true);
			form.add(tituloSitioTextField);
			
			final TextField<String> tituloLoginTextField = new TextField<>("tituloLogin", new PropertyModel<>(EditPersonalizacionPanel.this, "tituloLogin"));
			tituloLoginTextField.setRequired(true);
			form.add(tituloLoginTextField);
			
			OnOffSwitch showHomeCtrl = new OnOffSwitch("showHome", new PropertyModel<>(personalizacion, "showHome")) {

				private static final long serialVersionUID = -3214185498258791153L;

				@Override
				protected void accion() {
					personalizacion.setShowHome(!personalizacion.isShowHome());
				}

			};
			form.add(showHomeCtrl);
			
			TextArea<String> homehtmlTxtAr = new TextArea<>("homeHtml", new PropertyModel<>(personalizacion, "homeHtml"));		
			form.add(homehtmlTxtAr);

			form.add(new Button("editarPersonalizacion") {

				private static final long serialVersionUID = 4683058294127551505L;

				@Override
				public void onSubmit() {

					try {
						FileUpload fileUploadBigLogo = fileFotoBigLogo.getFileUpload();
						FileUpload fileUploadSmallLogo = fileFotoSmallLogo.getFileUpload();
						FileUpload fileUploadSymbol = fileFotoSymbol.getFileUpload();
						boolean error = false;
						boolean hayArchivo = false;

						if (fileUploadBigLogo != null) {
							// valido extensión y subo archivo
							if (!(fileUploadBigLogo.getClientFileName().split("\\.")[1].matches("jpg|jpeg|png|gif|JPG|JPEG|PNG|GIF"))) {
								error = true;
								getSession().error(getString("advEditPersonalizErrExt"));
							} else {

								// subo el archivo	
								personalizacion.setPicBigLogo(fileUploadBigLogo.getClientFileName());
								personalizacion.setContPicBigLogo(fileUploadBigLogo.getBytes());
								hayArchivo = true;
							}
						}

						if ((!error) && (fileUploadSmallLogo != null)) {
							// valido extensión y subo archivo
							if (!(fileUploadSmallLogo.getClientFileName().split("\\.")[1].matches("jpg|jpeg|png|gif|JPG|JPEG|PNG|GIF"))) {
								error = true;
								getSession().error(getString("advEditPersonalizErrExt"));
							} else {
								// subo el archivo	
								personalizacion.setPicSmallLogo(fileUploadSmallLogo.getClientFileName());
								personalizacion.setContPicSmallLogo(fileUploadSmallLogo.getBytes());
								hayArchivo = true;
							}
						}

						if ((!error) && (fileUploadSymbol != null)) {
							// valido extensión y subo archivo
							if (!(fileUploadSymbol.getClientFileName().split("\\.")[1].matches("jpg|jpeg|png|gif|JPG|JPEG|PNG|GIF"))) {
								error = true;
								getSession().error(getString("advEditPersonalizErrExt"));
							} else {
								// subo el archivo
								personalizacion.setPicSimbolo(fileUploadSymbol.getClientFileName());
								personalizacion.setContPicSimbolo(fileUploadSymbol.getBytes());
								hayArchivo = true;
							}
						}
						
						if ((getTituloLogin() == null) || (getTituloLogin().compareTo("") == 0)) {
							setTituloLogin(getString("advEditPersonalizTitSitioDef"));
						}
						if ((getTituloSitio() == null) || (getTituloSitio().compareTo("") == 0)) {
							setTituloSitio(getString("advEditPersonalizTitSitioDef"));
						}
						
						personalizacion.setTituloLogin(getTituloLogin());
						personalizacion.setTituloSitio(getTituloSitio());

						/*if ((!error) && (hayArchivo)) {*/
						if ((!error) ) {
							Contexto.getInstance().getManagerBeanRemote().actualizarPersonalizacion(personalizacion);
							getSession().info(getString("advEditPersonalizExito"));
						}

					} catch (Exception e) {
						getSession().error(getString("advEditPersonalizErrorProc"));
					}
				}
			});

		} catch (Exception e) {
			appLogger.error(e);
		}
	}

	public byte[] getArchivoPicSmallLogo() {
		return archivoPicSmallLogo;
	}

	public void setArchivoPicSmallLogo(byte[] archivoPicSmallLogo) {
		this.archivoPicSmallLogo = archivoPicSmallLogo;
	}

	public String getNombreArchivoSmallLogo() {
		return nombreArchivoSmallLogo;
	}

	public void setNombreArchivoSmallLogo(String nombreArchivoSmallLogo) {
		this.nombreArchivoSmallLogo = nombreArchivoSmallLogo;
	}

	public byte[] getArchivoPicBigLogo() {
		return archivoPicBigLogo;
	}

	public void setArchivoPicBigLogo(byte[] archivoPicBigLogo) {
		this.archivoPicBigLogo = archivoPicBigLogo;
	}

	public String getNombreArchivoBigLogo() {
		return nombreArchivoBigLogo;
	}

	public void setNombreArchivoBigLogo(String nombreArchivoBigLogo) {
		this.nombreArchivoBigLogo = nombreArchivoBigLogo;
	}

	public byte[] getArchivoPicSymbol() {
		return archivoPicSymbol;
	}

	public void setArchivoPicSymbol(byte[] archivoPicSymbol) {
		this.archivoPicSymbol = archivoPicSymbol;
	}

	public String getNombreArchivoSymbol() {
		return nombreArchivoSymbol;
	}

	public void setNombreArchivoSymbol(String nombreArchivoSymbol) {
		this.nombreArchivoSymbol = nombreArchivoSymbol;
	}

	public String getTituloSitio() {
		return tituloSitio;
	}

	public void setTituloSitio(String tituloSitio) {
		this.tituloSitio = tituloSitio;
	}

	public String getTituloLogin() {
		return tituloLogin;
	}

	public void setTituloLogin(String tituloLogin) {
		this.tituloLogin = tituloLogin;
	}
	
	
}
