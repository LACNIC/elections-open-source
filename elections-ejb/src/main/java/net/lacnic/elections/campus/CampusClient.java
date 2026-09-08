package net.lacnic.elections.campus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import net.lacnic.elections.domain.Candidate;
import net.lacnic.elections.domain.Election;
import net.lacnic.elections.domain.pre.CandidateCampusCourseStatus;
import net.lacnic.elections.utils.Constants;
import net.lacnic.elections.utils.ElectionsProperties;
import net.lacnic.elections.utils.EJBFactory;

public class CampusClient {

	private static String restFormat = "&moodlewsrestformat=json";
	private static final String DEFAULT_PLACEHOLDER_TEXT = "N/A";
	private static final String DEFAULT_CITY = "Montevideo";
	private static final String DEFAULT_COUNTRY = "UY";
	private static final String DEFAULT_AUTH_METHOD = "email";
	private static final String DEFAULT_LANGUAGE = "es";
	private static final String DEFAULT_TIMEZONE = "-3.0";
	private static final String DEFAULT_MAIL_FORMAT = "1";
	private static final long DEFAULT_CAMPUS_COURSE_MIN_ID = 0L;
	private static final String JSON_FIELD_USERS = "users";
	private static final String JSON_FIELD_CONTENT = "content";

	private static final Logger logger = LoggerFactory.getLogger(CampusClient.class);

	private CampusClient() {
		throw new UnsupportedOperationException("Utility class should not be instantiated");
	}

	public static String getCampusUrl() {
		if (!isCampusIntegrationEnabled()) {
			return null;
		}
		return getCampusConfiguration(Constants.CAMPUS_URL);
	}

	/**
	 * Campus is an external integration and must not be used by installations
	 * running with the local application authentication mode.
	 */
	public static boolean isCampusIntegrationEnabled() {
		String authMethod = ElectionsProperties.get(Constants.WS_AUTH_METHOD);
		if (Constants.WS_AUTH_TYPE_APP.equalsIgnoreCase(authMethod != null ? authMethod.trim() : null)) {
			return false;
		}
		String campusUrl = getCampusConfigurationWithoutLogging(Constants.CAMPUS_URL);
		String campusToken = getCampusConfigurationWithoutLogging(Constants.CAMPUS_TOKEN);
		return hasText(campusUrl) && hasText(campusToken);
	}

	public static boolean isCampusTrainingEnabled(Election election) {
		return isCampusIntegrationEnabled() && hasAnyCampusCourse(election);
	}

	private static String getCampusToken() {
		return getCampusConfiguration(Constants.CAMPUS_TOKEN);
	}

	private static String getCampusConfiguration(String key) {
		try {
			return EJBFactory.getInstance().getElectionsParametersEJB().getParameter(key);
		} catch (Exception e) {
			logger.error("Error obtaining Campus configuration parameter {}", key, e);
			return null;
		}
	}

	private static String getCampusConfigurationWithoutLogging(String key) {
		try {
			return EJBFactory.getInstance().getElectionsParametersEJB().getParameter(key);
		} catch (Exception e) {
			return null;
		}
	}

	public static Candidate enrollCandidate(Candidate candidate, String roleId, String courseId) {
		if (!isCampusIntegrationEnabled()) {
			return candidate;
		}
		long campusId = getCampusId(candidate);
		if (campusId > 0L) {
			enrollUser(campusId, roleId, courseId);
		}
		return candidate;
	}

	public static boolean enrollUser(long userId, String roleId, String courseId) {
		if (!isCampusIntegrationEnabled()) {
			return false;
		}
		try {
			if (userId <= 0L || !hasText(roleId) || !hasText(courseId)) {
				logger.warn("Skipping campus enrollment because userId, roleId or courseId is invalid. userId={}, roleId={}, courseId={}", userId, roleId, courseId);
				return false;
			}

			String functionName = "enrol_manual_enrol_users";
			String urlParameters = "enrolments[0][roleid]=" + roleId.trim() + "&enrolments[0][userid]=" + userId + "&enrolments[0][courseid]=" + courseId.trim();
			String response = sendRequest(urlParameters, functionName);
			if (!hasText(response)) {
				return false;
			}
			return true;
		} catch (Exception e) {
			logger.error("Error enrolling user in course", e);
			return false;
		}
	}

	private static long getCampusId(Candidate candidate) {
		if (candidate == null) {
			return 0L;
		}
		String candidateEmail = normalizeEmail(candidate.getMail());
		if (!hasText(candidateEmail)) {
			return 0L;
		}

		long campusId = getCampusUserIdFromEmail(candidateEmail);

		if (campusId == 0) {
			campusId = createUser(candidate);
		}

		return campusId;
	}

	public static long getOrCreateCampusUserId(Candidate candidate) {
		if (!isCampusIntegrationEnabled()) {
			return 0L;
		}
		try {
			if (candidate == null || !hasText(candidate.getMail())) {
				return 0L;
			}
			return getCampusId(candidate);
		} catch (Exception e) {
			logger.error("Error resolving or creating campus user for candidate {}", candidate != null ? candidate.getCandidateId() : null, e);
			return 0L;
		}
	}

	private static long createUser(Candidate candidate) {
		if (candidate == null) {
			return 0L;
		}
		String normalizedEmail = normalizeEmail(candidate.getMail());
		if (!hasText(normalizedEmail)) {
			return 0L;
		}

		String functionName = "core_user_create_users";
		String[] splitName = splitFullName(candidate.getName());
		String firstName = removeSpecialCharacters(splitName[0]);
		String lastName = removeSpecialCharacters(splitName[1]);

		String urlParameters = "users[0][username]=" + URLEncoder.encode(normalizedEmail, StandardCharsets.UTF_8)
				+ "&users[0][firstname]=" + URLEncoder.encode(firstName, StandardCharsets.UTF_8)
				+ "&users[0][lastname]=" + URLEncoder.encode(lastName, StandardCharsets.UTF_8)
				+ "&users[0][email]=" + URLEncoder.encode(normalizedEmail, StandardCharsets.UTF_8)
				+ "&users[0][auth]=" + URLEncoder.encode(DEFAULT_AUTH_METHOD, StandardCharsets.UTF_8)
				+ "&users[0][lang]=" + URLEncoder.encode(DEFAULT_LANGUAGE, StandardCharsets.UTF_8)
				+ "&users[0][timezone]=" + URLEncoder.encode(DEFAULT_TIMEZONE, StandardCharsets.UTF_8)
				+ "&users[0][mailformat]=" + URLEncoder.encode(DEFAULT_MAIL_FORMAT, StandardCharsets.UTF_8)
				+ "&users[0][city]=" + URLEncoder.encode(DEFAULT_CITY, StandardCharsets.UTF_8)
				+ "&users[0][country]=" + URLEncoder.encode(DEFAULT_COUNTRY, StandardCharsets.UTF_8) + "&users[0][createpassword]=1";

		String response = sendRequest(urlParameters, functionName);
		if (!hasText(response)) {
			return 0L;
		}

		try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
			JsonArray array = jsonReader.readArray();
			if (array == null || array.isEmpty()) {
				return 0L;
			}
			JsonObject obj = array.getJsonObject(0);
			if (obj == null || !obj.containsKey("id")) {
				return 0L;
			}
			JsonNumber id = obj.getJsonNumber("id");
			if (id == null) {
				return 0L;
			}

			logger.info("Id: {}", id.longValueExact());
			return id.longValueExact();
		}
	}

	private static String[] splitFullName(String fullName) {
		String normalized = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
		if (normalized.isEmpty()) {
			return new String[] { "N/A", "N/A" };
		}

		String[] parts = normalized.split(" ");
		int partCount = parts.length;

		if (partCount == 1) {
			return new String[] { parts[0], parts[0] };
		}
		if (partCount == 2) {
			return new String[] { parts[0], parts[1] };
		}
		if (partCount == 3) {
			return new String[] { parts[0] + " " + parts[1], parts[2] };
		}

		StringBuilder firstName = new StringBuilder();
		for (int i = 0; i < partCount - 2; i++) {
			if (i > 0) {
				firstName.append(' ');
			}
			firstName.append(parts[i]);
		}

		String lastName = parts[partCount - 2] + " " + parts[partCount - 1];
		return new String[] { firstName.toString(), lastName };
	}

	public static Candidate updateCandidateCampusProgress(Election election, Candidate candidate, Date startDate) {
		if (!isCampusIntegrationEnabled()) {
			return candidate;
		}
		try {
			if (election == null || !hasAnyCampusCourse(election) || candidate == null || !hasText(candidate.getMail()) || startDate == null) {
				return candidate;
			}

			long campusUserId = getCampusUserIdFromEmail(candidate.getMail());
			if (campusUserId <= 0L) {
				return candidate;
			}
			Date lastAccessDate = getLastAccessDate(campusUserId);

			if (lastAccessDate.after(startDate)) {
				logger.info("Campus access status: {}", GradeStatus.COMPLETED);
				candidate.setCampusCourseStatus(CandidateCampusCourseStatus.COMPLETED);
				return candidate;
			} else {
				logger.info("Campus access status: {}", GradeStatus.NEVER_ENTERED);
				if (shouldDowngradeToPending(candidate)) {
					candidate.setCampusCourseStatus(CandidateCampusCourseStatus.PENDING);
				}
				return candidate;
			}
		} catch (Exception e) {
			logger.error("Error getting user course grade status", e);
			return candidate;
		}
	}

	public static Candidate updateCandidateCampusCalification(Election election, Candidate candidate) {
		if (!isCampusIntegrationEnabled()) {
			return candidate;
		}
		try {
			if (election == null || candidate == null || candidate.getCampusCourseSelected() == null || candidate.getMail() == null || candidate.getMail().trim().isEmpty()) {
				return candidate;
			}

			long campusUserId = getCampusUserIdFromEmail(candidate.getMail());
			if (campusUserId <= 0L) {
				return candidate;
			}

			String functionName = "gradereport_user_get_grades_table";
			String urlParameters = "courseid=" + candidate.getCampusCourseSelected() + "&userid=" + campusUserId;
			String response = sendRequest(urlParameters, functionName);
			if (response == null || response.trim().isEmpty()) {
				return candidate;
			}

			try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
				JsonObject responseObject = jsonReader.readObject();
				JsonArray tables = responseObject.getJsonArray("tables");
				if (tables == null || tables.isEmpty()) {
					return candidate;
				}

				JsonObject latestTable = tables.getJsonObject(tables.size() - 1);
				JsonArray tableData = latestTable.getJsonArray("tabledata");
				String courseTotalGradeValue = resolveCourseTotalGradeValue(tableData);
				if (isValidGradeValue(courseTotalGradeValue)) {
					candidate.setCampusCourseCalification(courseTotalGradeValue);
				}
				return candidate;
			}
		} catch (Exception e) {
			logger.error("Error getting user course calification", e);
			return candidate;
		}
	}

	public static List<Course> getCourses() {
		if (!isCampusIntegrationEnabled()) {
			return new ArrayList<>();
		}
		JsonArray array = getCoursesFromCampus();
		if (array == null) {
			return null;
		}

		long courseMinId = resolveCampusCourseMinId();
		List<Course> courses = new ArrayList<>();
		for (JsonValue jsonValue : array) {
			JsonObject courseObject = (JsonObject) jsonValue;
			Long courseId = courseObject.getJsonNumber("id").longValue();
			if (courseId <= courseMinId) {
				continue;
			}
			courses.add(new Course(courseId, courseObject.getString("fullname")));
		}
		return courses;
	}

	private static long resolveCampusCourseMinId() {
		String value = getCampusConfiguration(Constants.CAMPUS_COURSE_MIN_ID);
		if (!hasText(value)) {
			return DEFAULT_CAMPUS_COURSE_MIN_ID;
		}
		try {
			long parsedValue = Long.parseLong(value.trim());
			if (parsedValue < 0L) {
				logger.warn("Parameter {} must be zero or positive. Received value: {}", Constants.CAMPUS_COURSE_MIN_ID, value);
				return DEFAULT_CAMPUS_COURSE_MIN_ID;
			}
			return parsedValue;
		} catch (NumberFormatException e) {
			logger.warn("Parameter {} is not a valid long. Received value: {}", Constants.CAMPUS_COURSE_MIN_ID, value, e);
			return DEFAULT_CAMPUS_COURSE_MIN_ID;
		}
	}

	private static JsonArray getCoursesFromCampus() {
		try {
			String response = sendRequest("core_course_get_courses");

			try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
				return jsonReader.readArray();
			}
		} catch (Exception e) {
			return null;
		}
	}

	public static long getCampusUserIdFromEmail(String email) {
		if (!isCampusIntegrationEnabled()) {
			return 0L;
		}
		String normalizedEmail = normalizeEmail(email);
		if (!hasText(normalizedEmail)) {
			return 0L;
		}
		String functionName = "core_user_get_users";
		String urlParameters = "criteria[0][key]=email&criteria[0][value]=" + URLEncoder.encode(normalizedEmail, StandardCharsets.UTF_8);
		String response = sendRequest(urlParameters, functionName);
		if (!hasText(response)) {
			return 0L;
		}

		try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
			JsonObject obj = jsonReader.readObject();
			if (obj == null || !obj.containsKey(JSON_FIELD_USERS)) {
				return 0L;
			}
			JsonArray results = obj.getJsonArray(JSON_FIELD_USERS);

			if (results == null || results.isEmpty()) {
				return 0L;
			}

			JsonObject result = results.getJsonObject(0);
			if (result == null || !result.containsKey("id")) {
				return 0L;
			}
			JsonNumber id = result.getJsonNumber("id");
			if (id == null) {
				return 0L;
			}

			logger.info("Campus UserId: {}", id.longValueExact());
			return id.longValueExact();
		}
	}

	public static List<Long> getEnrolledUserIds(String courseId) {
		List<Long> enrolledUserIds = new ArrayList<>();
		if (!isCampusIntegrationEnabled()) {
			return enrolledUserIds;
		}

		if (courseId == null || "0".equalsIgnoreCase(courseId)) {
			return enrolledUserIds;
		}

		try {
			String response = sendRequest("courseid=" + courseId, "core_enrol_get_enrolled_users");

			try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
				JsonArray array = jsonReader.readArray();
				for (JsonValue jsonValue : array) {
					JsonObject userObject = (JsonObject) jsonValue;
					enrolledUserIds.add(userObject.getJsonNumber("id").longValue());
				}
			}
		} catch (Exception e) {
			logger.error("Error checking user enrollment in course {}", courseId, e);
		}
		return enrolledUserIds;
	}

	private static Date getLastAccessDate(long campusUserId) {
		if (campusUserId <= 0L) {
			return new Date(0);
		}
		String functionName = "core_user_get_users";
		String urlParameters = "criteria[0][key]=id&criteria[0][value]=" + campusUserId;
		String response = sendRequest(urlParameters, functionName);
		if (!hasText(response)) {
			return new Date(0);
		}

		try (JsonReader jsonReader = Json.createReader(new StringReader(response))) {
			JsonObject obj = jsonReader.readObject();
			if (obj == null || !obj.containsKey(JSON_FIELD_USERS)) {
				return new Date(0);
			}
			JsonArray results = obj.getJsonArray(JSON_FIELD_USERS);

			if (results == null || results.isEmpty()) {
				return new Date(0);
			}

			JsonObject result = results.getJsonObject(0);
			if (result == null || !result.containsKey("lastaccess")) {
				return new Date(0);
			}
			JsonNumber id = result.getJsonNumber("lastaccess");
			if (id == null) {
				return new Date(0);
			}
			return new Date(id.longValueExact() * 1000);
		}
	}

	private static String sendRequest(String functionName) {
		return sendRequest("", functionName);
	}

	private static boolean isValidGradeValue(String gradeValue) {
		return gradeValue != null && !gradeValue.trim().isEmpty() && !"-".equals(gradeValue.trim());
	}

	private static String resolveCourseTotalGradeValue(JsonArray tableData) {
		if (tableData == null || tableData.isEmpty()) {
			return null;
		}
		for (int i = 0; i < tableData.size(); i++) {
			JsonObject row = tableData.getJsonObject(i);
			if (!isCourseTotalRow(row)) {
				continue;
			}
			String gradeContent = extractCellContent(row, "grade");
			if (isValidGradeValue(gradeContent)) {
				return gradeContent;
			}
		}
		return null;
	}

	private static boolean isCourseTotalRow(JsonObject row) {
		if (row == null || !row.containsKey("itemname")) {
			return false;
		}

		JsonObject itemName = row.getJsonObject("itemname");
		if (itemName == null) {
			return false;
		}

		String cssClass = itemName.getString("class", "");
		if (hasText(cssClass) && cssClass.toLowerCase().contains("baggt")) {
			return true;
		}

		String content = itemName.containsKey(JSON_FIELD_CONTENT) ? extractGradeValue(itemName.get(JSON_FIELD_CONTENT)) : null;
		if (content == null) {
			return false;
		}
		String normalized = content.toLowerCase();
		return normalized.contains("total del curso") || normalized.contains("course total") || normalized.contains("total do curso");
	}

	private static String extractCellContent(JsonObject row, String key) {
		if (row == null || key == null || !row.containsKey(key)) {
			return null;
		}
		JsonObject cell = row.getJsonObject(key);
		if (cell == null || !cell.containsKey(JSON_FIELD_CONTENT)) {
			return null;
		}
		return extractGradeValue(cell.get(JSON_FIELD_CONTENT));
	}

	private static String extractGradeValue(JsonValue content) {
		if (content == null) {
			return null;
		}

		String rawContent;
		if (content.getValueType() == JsonValue.ValueType.STRING) {
			rawContent = ((JsonString) content).getString();
		} else {
			rawContent = content.toString();
		}

		Matcher matcher = Pattern.compile("(?is)<div[^>]*>\\s*([^<]+?)\\s*</div>").matcher(rawContent);
		while (matcher.find()) {
			String candidateValue = matcher.group(1);
			if (candidateValue == null) {
				continue;
			}

			String normalizedValue = candidateValue.replace("&nbsp;", " ").replace("&#160;", " ").trim();
			if (!normalizedValue.isEmpty()) {
				return normalizedValue;
			}
		}

		String withoutTags = rawContent.replaceAll("(?is)<[^>]*>", " ").replace("&nbsp;", " ").replace("&#160;", " ").trim().replaceAll("\\s+", " ");
		return withoutTags.isEmpty() ? null : withoutTags;
	}

	private static String sendRequest(String urlParameters, String functionName) {
		if (!isCampusIntegrationEnabled()) {
			return null;
		}
		HttpURLConnection con = null;
		try {
			String campusDomainName = getCampusUrl();
			String campusToken = getCampusToken();
			if (!hasText(campusDomainName) || !hasText(campusToken) || !hasText(functionName)) {
				logger.warn("Skipping campus request due to invalid configuration. domainSet={}, tokenSet={}, functionName={}",
						hasText(campusDomainName), hasText(campusToken), functionName);
				return null;
			}

			logger.info("Parameters: {}", urlParameters);
			String serverurl = campusDomainName + "/webservice/rest/server.php" + "?wstoken=" + campusToken + "&wsfunction=" + functionName + restFormat;

			con = (HttpURLConnection) new URL(serverurl).openConnection();
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			con.setRequestProperty("Content-Language", "en-US");
			con.setDoOutput(true);
			con.setUseCaches(false);
			con.setDoInput(true);

			try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
				if (urlParameters != null && !urlParameters.isEmpty()) {
					wr.write(urlParameters.getBytes(StandardCharsets.UTF_8));
				}
				wr.flush();
			}

			int responseCode = con.getResponseCode();
			boolean success = responseCode >= 200 && responseCode < 300;
			InputStream responseStream = success ? con.getInputStream() : con.getErrorStream();
			if (responseStream == null) {
				logger.warn("Campus response stream is empty. functionName={}, responseCode={}", functionName, responseCode);
				return null;
			}

			StringBuilder response = new StringBuilder();
			try (BufferedReader rd = new BufferedReader(new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
				String line;
				while ((line = rd.readLine()) != null) {
					response.append(line).append('\r');
				}
			}

			if (!success) {
				logger.warn("Campus request returned non-success status. functionName={}, responseCode={}, response={}", functionName, responseCode, response);
			}

			if (response.toString().contains("exception")) {
				logger.warn("Response from {} contains an exception: {}", functionName, response);
			}
			return response.toString();
		} catch (Exception e) {
			logger.error("Error sending request to function {}", functionName, e);
			return null;
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	private static String removeSpecialCharacters(String in) {
		if (in == null || in.trim().isEmpty()) {
			return DEFAULT_PLACEHOLDER_TEXT;
		}
		String sanitized = in.replaceAll("[^a-zA-Z0-9 ]", "").trim().replaceAll("\\s+", " ");
		return sanitized.isEmpty() ? DEFAULT_PLACEHOLDER_TEXT : sanitized;
	}

	private static String normalizeEmail(String email) {
		if (email == null) {
			return null;
		}
		String normalized = email.trim().toLowerCase();
		return normalized.isEmpty() ? null : normalized;
	}

	private static boolean shouldDowngradeToPending(Candidate candidate) {
		if (candidate == null || candidate.getCampusCourseStatus() == null) {
			return true;
		}

		CandidateCampusCourseStatus currentStatus = candidate.getCampusCourseStatus();
		return currentStatus == CandidateCampusCourseStatus.PENDING;
	}

	private static boolean hasAnyCampusCourse(Election election) {
		if (election == null) {
			return false;
		}
		return election.getCampusCourse() != null
				|| election.getCampusCourseEnglish() != null
				|| election.getCampusCoursePortuguese() != null;
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

}
