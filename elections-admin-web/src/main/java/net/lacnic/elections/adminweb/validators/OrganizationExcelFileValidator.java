package net.lacnic.elections.adminweb.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import net.lacnic.elections.adminweb.app.AppContext;
import net.lacnic.elections.exception.CensusValidationException;

public class OrganizationExcelFileValidator implements IValidator<List<FileUpload>> {

	private static final long serialVersionUID = -572989907646530786L;
	private static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static final String KEY_ORGANIZATIONS_MANAGEMENT_VALIDATION_EXCEL_READ_ERROR = "organizationsManagementValidationExcelReadError";

	public enum UploadType {
		DEBTORS,
		DELETE,
		UPSERT
	}

	private final UploadType uploadType;

	public OrganizationExcelFileValidator(UploadType uploadType) {
		this.uploadType = uploadType;
	}

	@Override
	public void validate(IValidatable<List<FileUpload>> validatable) {
		FileUpload upload = getFirstUpload(validatable.getValue());
		if (upload == null) {
			return;
		}
		List<ValidationError> errors = validateUpload(upload);
		for (ValidationError error : errors) {
			validatable.error(error);
		}
	}

	private List<ValidationError> validateUpload(FileUpload upload) {
		List<ValidationError> errors = new ArrayList<>();
		if (!isXlsx(upload)) {
			errors.add(error("organizationsManagementValidationExcelUnknownFileType"));
			return errors;
		}
		try {
			byte[] content = upload.getBytes();
			String contentType = upload.getContentType();
			switch (uploadType) {
			case DEBTORS:
				AppContext.getInstance().getManagerBeanRemote().validateOrganizationsDebtorsExcel(contentType, content);
				break;
			case DELETE:
				AppContext.getInstance().getManagerBeanRemote().validateOrganizationsDeleteExcel(contentType, content);
				break;
			case UPSERT:
				// Detailed validation for upsert runs on submit so the UI can expose the downloadable error report.
				break;
			default:
				errors.add(error(KEY_ORGANIZATIONS_MANAGEMENT_VALIDATION_EXCEL_READ_ERROR));
				break;
			}
		} catch (CensusValidationException cve) {
			errors.add(toValidationError(cve));
		} catch (Exception e) {
			errors.add(error(KEY_ORGANIZATIONS_MANAGEMENT_VALIDATION_EXCEL_READ_ERROR));
		}
		return errors;
	}

	private ValidationError toValidationError(CensusValidationException cve) {
		if (cve == null || cve.getMessage() == null || cve.getMessage().isBlank()) {
			return error(KEY_ORGANIZATIONS_MANAGEMENT_VALIDATION_EXCEL_READ_ERROR);
		}
		if ("organizationsManagementUploadNullRequiredFields".equals(cve.getMessage())) {
			ValidationError error = new ValidationError().addKey("organizationsManagementValidationExcelOrgIdEmptyAtRow");
			Integer row = cve.getErrorRow();
			error.setVariable("row", row == null ? "" : row + 1);
			return error;
		}
		return new ValidationError().addKey(cve.getMessage());
	}

	private ValidationError error(String key) {
		return new ValidationError().addKey(key);
	}

	private FileUpload getFirstUpload(List<FileUpload> uploads) {
		if (uploads == null || uploads.isEmpty()) {
			return null;
		}
		return uploads.get(0);
	}

	private boolean isXlsx(FileUpload upload) {
		String fileName = upload.getClientFileName();
		boolean hasXlsxName = fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx");
		boolean hasXlsxContentType = CONTENT_TYPE_XLSX.equals(upload.getContentType());
		return hasXlsxName || hasXlsxContentType;
	}
}
