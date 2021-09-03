package net.lacnic.elections.domain.newservices;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.services.ElectionReportTable;

public class OrganizationReportTableDetail implements Serializable{
	private static final long serialVersionUID = 4933453990097788863L;
	private static final Logger appLogger = LogManager.getLogger("servicesAppLogger");

	List<ElectionReportTable> electionsReportTable=new ArrayList<>();
	List <UserVoterReportTableDetail> userVotersReportTable= new ArrayList<>();

	public OrganizationReportTableDetail() {
		
	}
	
	public OrganizationReportTableDetail(List<UserVoter> userVoters) {
		appLogger.info("PASE");
		for(int i=0;i<userVoters.size();i++) {
			appLogger.info(userVoters.get(i).getElection().getElectionId());
			this.userVotersReportTable.add(new UserVoterReportTableDetail(userVoters.get(i)));
			
			ElectionReportTable electionReportTable= new ElectionReportTable(userVoters.get(i).getElection());
			
			if(!electionsReportTable.contains(electionReportTable)) {
				electionsReportTable.add(new ElectionReportTable(userVoters.get(i).getElection()));
			}
		}
	}
	
	public List<ElectionReportTable> getElections() {
		return this.electionsReportTable;
	}
	

	public List<UserVoterReportTableDetail> getUserVoters(){
		return this.userVotersReportTable;
	}


}
