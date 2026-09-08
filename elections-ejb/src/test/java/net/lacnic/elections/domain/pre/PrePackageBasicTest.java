package net.lacnic.elections.domain.pre;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lacnic.DomainModelTestUtil;

 class PrePackageBasicTest extends TestCase {
	private static final Class<?>[] PACKAGE_CLASSES = new Class[] {
			net.lacnic.elections.domain.pre.AuditorCandidateDecision.class,
			net.lacnic.elections.domain.pre.CandidateEvaluationStatus.class,
			net.lacnic.elections.domain.pre.CandidatePepDeclaration.class,
			net.lacnic.elections.domain.pre.WorkOrganizationType.class,
			net.lacnic.elections.domain.pre.ElectionPresetConfigurations.class,
			net.lacnic.elections.domain.pre.TaskDependencyLevel.class,
			net.lacnic.elections.domain.pre.ElectionCalendarKey.class,
			net.lacnic.elections.domain.pre.ElectionCalendar.class,
			net.lacnic.elections.domain.pre.SyncRun.class,
			net.lacnic.elections.domain.pre.SyncAudit.class,
			net.lacnic.elections.domain.pre.CandidateElectionTaskStatus.class,
			net.lacnic.elections.domain.pre.CandidateElectionTaskProgress.class,
			net.lacnic.elections.domain.pre.CandidateWorkOrganization.class,
			net.lacnic.elections.domain.pre.SupportStatus.class,
			net.lacnic.elections.domain.pre.CandidateTrainingCredentialsRequestResult.class,
			net.lacnic.elections.domain.pre.CandidateDeclarationsDefinition.class,
			net.lacnic.elections.domain.pre.SupportNomination.class,
			net.lacnic.elections.domain.pre.CandidateDeclarationOptionDefinition.class,
			net.lacnic.elections.domain.pre.CandidateTextImprovementResponse.class,
			net.lacnic.elections.domain.pre.OrganizationCategoryConverter.class,
			net.lacnic.elections.domain.pre.CandidateTextImprovementInstruction.class,
			net.lacnic.elections.domain.pre.CandidateDeclarationInputType.class,
			net.lacnic.elections.domain.pre.Organization.class,
			net.lacnic.elections.domain.pre.CandidateDeclarationDefinition.class,
			net.lacnic.elections.domain.pre.CandidateStatus.class,
			net.lacnic.elections.domain.pre.NominationStatus.class,
			net.lacnic.elections.domain.pre.CandidateDeclarationCode.class,
			net.lacnic.elections.domain.pre.CandidateQuestionStatus.class,
			net.lacnic.elections.domain.pre.CandidateCountryLink.class,
			net.lacnic.elections.domain.pre.Nomination.class,
			net.lacnic.elections.domain.pre.CandidateQuestionOwner.class,
			net.lacnic.elections.domain.pre.LanguageCodeConverter.class,
			net.lacnic.elections.domain.pre.CandidateCampusCourseStatus.class,
			net.lacnic.elections.domain.pre.CandidateQuestion.class,
			net.lacnic.elections.domain.pre.AuditorCandidateDecisionStatus.class,
			net.lacnic.elections.domain.pre.ElectionTaskKey.class
	};

	 static Test suite() {
		return new TestSuite(PrePackageBasicTest.class);
	}

	@org.junit.jupiter.api.Test

	 void testPrePackageClassesBasicContract() {
		for (Class<?> c : PACKAGE_CLASSES) {
			DomainModelTestUtil.assertBasicBeanContract(c);
		}
	}
}
