package net.lacnic.elections.adminweb.validators;

import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

public class CensusExcelFileValidator implements IValidator<List<FileUpload>> {

	private static final long serialVersionUID = 1357298806077644144L;
	private static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	public CensusExcelFileValidator(long electionId, CheckBox regenerateLinksCheckbox) {
		// Kept signature to avoid touching call sites.
	}

	@Override
	public void validate(IValidatable<List<FileUpload>> validatable) {
		FileUpload upload = getFirstUpload(validatable.getValue());
		if (upload == null) {
			validatable.error(new ValidationError().addKey("censusManagementErrNoFile"));
			return;
		}
		if (!isExcel(upload)) {
			validatable.error(new ValidationError().addKey("censusManagementUploadUnknownFileType"));
		}
	}

	private FileUpload getFirstUpload(List<FileUpload> uploads) {
		if (uploads == null || uploads.isEmpty()) {
			return null;
		}
		return uploads.get(0);
	}

	private boolean isExcel(FileUpload upload) {
		String fileName = upload.getClientFileName();
		boolean hasXlsxName = fileName != null && fileName.toLowerCase(Locale.ROOT).endsWith(".xlsx");
		boolean hasXlsxContentType = CONTENT_TYPE_XLSX.equals(upload.getContentType());
		return hasXlsxName || hasXlsxContentType;
	}
}
