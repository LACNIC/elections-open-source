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
import net.lacnic.elections.domain.services.dbtables.ActivityTableReport;
import net.lacnic.elections.domain.services.dbtables.AuditorTableReport;
import net.lacnic.elections.domain.services.dbtables.CandidateTableReport;
import net.lacnic.elections.domain.services.dbtables.CommissionerTableReport;
import net.lacnic.elections.domain.services.dbtables.CustomizationTableReport;
import net.lacnic.elections.domain.services.dbtables.ElectionTableReport;
import net.lacnic.elections.domain.services.dbtables.EmailTableReport;
import net.lacnic.elections.domain.services.dbtables.UserAdminTableReport;
import net.lacnic.elections.domain.services.dbtables.UserVoterTableReport;
import net.lacnic.elections.domain.services.dbtables.VoteTableReport;
import net.lacnic.elections.domain.services.detail.ElectionDetailReport;
import net.lacnic.elections.domain.services.detail.ElectionParticipationDetailReport;
import net.lacnic.elections.domain.services.detail.OrganizationVoterDetailReport;
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

	public List<TablesReportDataLongId> getActivitiesBasicData(int pageSize, int offset);

	public ActivityTableReport getActivityTableReport(Long activityId);

	public List<TablesReportDataLongId> getAuditorsBasicData(int pageSize, int offset);

	public AuditorTableReport getAuditorTableReport(Long auditorId);

	public List<TablesReportDataLongId> getCandidatesBasicData(int pageSize, int offset);

	public CandidateTableReport getCandidateTableReport(Long candidateId);

	public List<TablesReportDataLongId> getCommissionersBasicData(int pageSize, int offset);

	public CommissionerTableReport getCommissionerTableReport(Long commissionerId);

	public List<TablesReportDataLongId> getCustomizationsBasicData();

	public CustomizationTableReport getCustomizationTableReport(Long customizationId);

	public List<TablesReportDataLongId> getElectionsBasicData(int pageSize, int offset);

	public ElectionTableReport getElectionTableReport(Long electionId);

	public List<TablesReportDataLongId> getElectionEmailTemplatesBasicData(int pageSize, int offset);

	public ElectionEmailTemplate getElectionEmailTemplateTableReport(Long electionEmailTemplateId);

	public List<TablesReportDataLongId> getEmailsBasicData(int pageSize, int offset);

	public EmailTableReport getEmailTableReport(Long emailId);

	public List<TablesReportDataLongId> getEmailsHistoryBasicData(int pageSize, int offset);

	public EmailTableReport getEmailHistoryTableReport(Long emailHistoryId);

	public List<TablesReportDataLongId> getIpAccessesBasicData(int pageSize, int offset);

	public IpAccess getIpAccessTableReport(Long ipAccessId);

	public List<TablesReportDataLongId> getJointElectionsBasicData(int pageSize, int offset);

	public JointElection getJointElectionTableReport(Long jointElectionId);

	public List<TableReportDataStringId> getParametersBasicData();

	public Parameter getParameterReport(String key);

	public List<TableReportDataStringId> getUserAdminBasicData(int pageSize, int offset);

	public UserAdminTableReport getUserAdminReportTable(String userAdminId);

	public List<TablesReportDataLongId> getUserVotersBasicData(int pageSize, int offset);

	public UserVoterTableReport getUserVoterReportTable(Long userVoterId);

	public List<TablesReportDataLongId> getVotesBasicData(int pageSize, int offset);

	public VoteTableReport getVoteTableReport(Long voteId);

	public List<ElectionDetailReport> getElectionsDetailReport();

	public ElectionDetailReport getElectionDetailReport(Long electionId);

	public List<ElectionParticipationDetailReport> getElectionsParticipationsByEmail(String email);

	public List<OrganizationVoterDetailReport> getElectionsParticipationsByOrgId(String orgID);

}
