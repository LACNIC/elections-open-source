package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.exception.CensusValidationException;

public class CensusOrgIdIdentityValidator implements IValidator<String> {

	private static final long serialVersionUID = 3649749305374341549L;

	private final long electionId;
	private final boolean editingMode;
	private final IModel<String> mailModel;
	private final IModel<Long> userVoterIdModel;

	public CensusOrgIdIdentityValidator(long electionId, boolean editingMode, IModel<String> mailModel, IModel<Long> userVoterIdModel) {
		this.electionId = electionId;
		this.editingMode = editingMode;
		this.mailModel = mailModel;
		this.userVoterIdModel = userVoterIdModel;
	}

	@Override
	public void validate(IValidatable<String> validatable) {
		String orgId = validatable.getValue();
		boolean validateByOrgId = orgId != null && !orgId.trim().isEmpty();
		if (validateByOrgId) {
			UserVoter userVoter = new UserVoter();
			userVoter.setMail(mailModel.getObject());
			userVoter.setOrgID(orgId);
			if (editingMode && userVoterIdModel != null && userVoterIdModel.getObject() != null) {
				userVoter.setUserVoterId(userVoterIdModel.getObject());
				Election election = new Election(electionId);
				userVoter.setElection(election);
			}
			try {
				if (editingMode) {
					AppContext.getInstance().getManagerBeanRemote().validateUserVoterCanBeEdited(userVoter);
				} else {
					AppContext.getInstance().getManagerBeanRemote().validateUserVoterCanBeAdded(electionId, userVoter);
				}
			} catch (CensusValidationException cve) {
				String messageKey = cve.getMessage();
				if (messageKey != null && !messageKey.isBlank()) {
					validatable.error(new ValidationError().addKey(messageKey));
				} else {
					validatable.error(new ValidationError().addKey("censusManagementErrBif"));
				}
			}
		}
	}
}
