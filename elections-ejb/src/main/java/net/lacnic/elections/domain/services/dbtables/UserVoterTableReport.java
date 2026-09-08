package net.lacnic.elections.domain.services.dbtables;

import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.domain.services.detail.UserVoterDetailReport;


public class UserVoterTableReport extends UserVoterDetailReport {

	private static final long serialVersionUID = 4933453990097788863L;

	public UserVoterTableReport() { }

	public UserVoterTableReport(UserVoter userVoter) {
		super(userVoter);
	}
}
