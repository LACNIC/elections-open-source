package net.lacnic.elections.adminweb.validators;

import org.apache.wicket.model.IModel;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.ElectionType;
import net.lacnic.elections.domain.UserVoter;
import net.lacnic.elections.exception.CensusValidationException;

public class CensusEmailIdentityValidator implements IValidator<String> {

	private static final long serialVersionUID = 1835001516280296890L;

	private final long electionId;
	private final ElectionType electionType;
	private final boolean editingMode;
	private final FormComponent<String> orgIdComponent;
	private final IModel<String> orgIdModel;
	private final IModel<Long> userVoterIdModel;

	public CensusEmailIdentityValidator(long electionId, ElectionType electionType, boolean editingMode, FormComponent<String> orgIdComponent, IModel<String> orgIdModel, IModel<Long> userVoterIdModel) {
		this.electionId = electionId;
		this.electionType = electionType;
		this.editingMode = editingMode;
		this.orgIdComponent = orgIdComponent;
		this.orgIdModel = orgIdModel;
		this.userVoterIdModel = userVoterIdModel;
	}

	@Override
	public void validate(IValidatable<String> validatable) {
		String mail = validatable.getValue();
		String orgId = getCurrentOrgIdValue();
		boolean validateByEmail = !hasOrgId(orgId) && mail != null && !mail.trim().isEmpty();
		if (validateByEmail) {
			UserVoter userVoter = new UserVoter();
				userVoter.setMail(mail);
					userVoter.setOrgID(null);
					if (editingMode && userVoterIdModel != null && userVoterIdModel.getObject() != null) {
						userVoter.setUserVoterId(userVoterIdModel.getObject());
						Election election = new Election(electionId);
						election.setElectionType(electionType);
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

	private boolean hasOrgId(String orgId) {
		return orgId != null && !orgId.trim().isEmpty();
	}

	private String getCurrentOrgIdValue() {
		String orgId = null;
		if (orgIdComponent != null) {
			orgId = orgIdComponent.getInput();
		}
		if (!hasOrgId(orgId) && orgIdModel != null) {
			orgId = orgIdModel.getObject();
		}
		return orgId;
	}
}
