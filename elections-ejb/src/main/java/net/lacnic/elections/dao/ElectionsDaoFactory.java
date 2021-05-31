package net.lacnic.elections.dao;

import javax.persistence.EntityManager;


public class ElectionsDaoFactory {

	private ElectionsDaoFactory() { }


	public static UserVoterDao createUsuarioPadronDao(EntityManager em) {
		return new UserVoterDao(em);
	}

	public static VoteDao createVotoDao(EntityManager em) {
		return new VoteDao(em);
	}

	public static EleccionDao createEleccionDao(EntityManager em) {
		return new EleccionDao(em);
	}

	public static IpAccessDao createIpAccessDao(EntityManager em) {
		return new IpAccessDao(em);
	}

	public static CandidateDao createCandidateDao(EntityManager em) {
		return new CandidateDao(em);
	}

	public static ActivityDao createActivityDao(EntityManager em) {
		return new ActivityDao(em);
	}

	public static ElectionEmailTemplateDao createElectionEmailTemplateDao(EntityManager em) {
		return new ElectionEmailTemplateDao(em);
	}

	public static EmailDao createEmailDao(EntityManager em) {
		return new EmailDao(em);
	}

	public static CommissionerDao createCommissionerDao(EntityManager em) {
		return new CommissionerDao(em);
	}

	public static AuditorDao createAuditorDao(EntityManager em) {
		return new AuditorDao(em);
	}

	public static ReportDao createReportDao(EntityManager em) {
		return new ReportDao(em);
	}

	public static UserAdminDao createUsuarioAdminDao(EntityManager em) {
		return new UserAdminDao(em);
	}

	public static ParameterDao createParameterDao(EntityManager em) {
		return new ParameterDao(em);
	}

	public static CustomizationDao createCustomizationDao(EntityManager em) {
		return new CustomizationDao(em);
	}

}
