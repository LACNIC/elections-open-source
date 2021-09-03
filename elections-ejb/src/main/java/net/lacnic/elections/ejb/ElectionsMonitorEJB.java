package net.lacnic.elections.ejb;

import java.util.List;

import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.data.Participation;
import net.lacnic.elections.data.TableReportDataStringId;
import net.lacnic.elections.data.TablesReportDataLongId;
import net.lacnic.elections.domain.ElectionEmailTemplate;
import net.lacnic.elections.domain.ElectionLight;
import net.lacnic.elections.domain.IpAccess;
import net.lacnic.elections.domain.JointElection;
import net.lacnic.elections.domain.Parameter;
import net.lacnic.elections.domain.newservices.ElectionDetailReportTable;
import net.lacnic.elections.domain.newservices.OrganizationReportTableDetail;
import net.lacnic.elections.domain.services.ActivityReportTable;
import net.lacnic.elections.domain.services.AuditorReportTable;
import net.lacnic.elections.domain.services.CandidateReportTable;
import net.lacnic.elections.domain.services.CommissionerReportTable;
import net.lacnic.elections.domain.services.CustomizationReportTable;
import net.lacnic.elections.domain.services.ElectionReportTable;
import net.lacnic.elections.domain.services.EmailReportTable;
import net.lacnic.elections.domain.services.UserAdminReportTable;
import net.lacnic.elections.domain.services.UserVoterReportTable;
import net.lacnic.elections.domain.services.VoteReportTable;
import net.ripe.ipresource.IpResourceSet;


public interface ElectionsMonitorEJB {

	public HealthCheck getHealthCheckData();

	public HealthCheck updateHealthCheckData();

	public List<Participation> getOrganizationParticipations(String org);

	public List<ElectionLight> getElectionsLightAllOrderStartDateDesc();

	public String getWsAuthToken();

	public String getWsAuthMethod();

	public String getWsLacnicAuthUrl();

	public IpResourceSet getWsAuthorizedIps();

	public List<TablesReportDataLongId> getActivitiesBasicData();

	public ActivityReportTable getActivityTableReport(Long activityId);

	public List<TablesReportDataLongId> getAuditorsBasicData();

	public AuditorReportTable getAuditorTableReport(Long auditorId);

	public List<TablesReportDataLongId> getCandidatesBasicData();

	public CandidateReportTable getCandidateTableReport(Long candidateId);

	public List<TablesReportDataLongId> getCommissionersBasicData();

	public CommissionerReportTable getCommissionerTableReport(Long commissionerId);

	public List<TablesReportDataLongId> getCustomizationsBasicData();

	public CustomizationReportTable getCustomizationTableReport(Long customizationId);

	public List<TablesReportDataLongId> getElectionsBasicData();

	public ElectionReportTable getElectionTableReport(Long electionId);

	public List<TablesReportDataLongId> getElectionEmailTemplatesBasicData();

	public ElectionEmailTemplate getElectionEmailTemplateTableReport(Long electionEmailTemplateId);

	public List<TablesReportDataLongId> getEmailsBasicData();

	public EmailReportTable getEmailTableReport(Long emailId);

	public List<TablesReportDataLongId> getEmailsHistoryBasicData();

	public EmailReportTable getEmailHistoryTableReport(Long emailHistoryId);

	public List<TablesReportDataLongId> getIpAccessesBasicData();

	public IpAccess getIpAccessTableReport(Long ipAccessId);

	public List<TablesReportDataLongId> getJointElectionsBasicData();

	public JointElection getJointElectionTableReport(Long jointElectionId);

	public List<TableReportDataStringId> getParametersBasicData();

	public Parameter getParameterReport(String key);

	public List<TableReportDataStringId> getUserAdminBasicData();

	public UserAdminReportTable getUserAdminReportTable(String userAdminId);

	public List<TablesReportDataLongId> getUserVotersBasicData();

	public UserVoterReportTable getUserVoterReportTable(Long userVoterId);

	public List<TablesReportDataLongId> getVotesBasicData();

	public VoteReportTable getVoteTableReport(Long voteId);
	
	public ElectionDetailReportTable getElectionDetailTableReport(Long electionId);

	public List <ElectionDetailReportTable> getElectionsDetailsTableReport();

	public List <ElectionReportTable> getElectionsByEmail(String email);

	public OrganizationReportTableDetail getOrganizationDetailsById(String orgID);
	

	
	
}
