package net.lacnic.siselecciones.dao;

import javax.persistence.EntityManager;

public class DaoFactoryElecciones {

	private DaoFactoryElecciones() {
	}

	public static UsuarioPadronDao createUsuarioPadronDao(EntityManager em) {
		return new UsuarioPadronDao(em);
	}

	public static VotoDao createVotoDao(EntityManager em) {
		return new VotoDao(em);
	}

	public static EleccionDao createEleccionDao(EntityManager em) {
		return new EleccionDao(em);
	}

	public static AccesosIpsDao createAccesoIpsDao(EntityManager em) {
		return new AccesosIpsDao(em);
	}

	public static CandidatoDao createCandidatoDao(EntityManager em) {
		return new CandidatoDao(em);
	}

	public static ActividadDao createActividadDao(EntityManager em) {
		return new ActividadDao(em);
	}

	public static TemplateEleccionDao createTemplateEleccionesDao(EntityManager em) {
		return new TemplateEleccionDao(em);
	}

	public static EmailDao createEmailDao(EntityManager em) {
		return new EmailDao(em);
	}

	public static ComisionadoDao createComisionadoDao(EntityManager em) {
		return new ComisionadoDao(em);
	}

	public static AuditorDao createAuditorDao(EntityManager em) {
		return new AuditorDao(em);
	}

	public static ReportDao createReportDao(EntityManager em) {
		return new ReportDao(em);
	}

	public static UsuarioAdminDao createUsuarioAdminDao(EntityManager em) {
		return new UsuarioAdminDao(em);
	}

	public static ParametroDao createParametroDao(EntityManager em) {
		return new ParametroDao(em);
	}
	
	public static PersonalizacionDao createPersonalizacionDao(EntityManager em) {
		return new PersonalizacionDao(em);
	}
}