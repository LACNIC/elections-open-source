package net.lacnic.siselecciones.admin.web.panel.avanzadas;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.ContextImage;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;

import net.lacnic.siselecciones.admin.app.Contexto;
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

						if ((!error) && (hayArchivo)) {
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

}
