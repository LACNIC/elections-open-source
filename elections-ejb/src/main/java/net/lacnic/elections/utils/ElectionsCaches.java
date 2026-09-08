package net.lacnic.elections.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.lacnic.elections.data.AsyncProcessingError;
import net.lacnic.elections.data.AsyncProcessingProgress;
import net.lacnic.elections.data.AsyncProcessingType;
import net.lacnic.elections.data.HealthCheck;
import net.lacnic.elections.domain.services.publicelection.PublicElectionsSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionCoreSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionOfficialResultSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionPhotoSnapshot;
import net.lacnic.elections.domain.services.publicelection.PublicElectionRollSnapshot;

public final class ElectionsCaches {

	private static final Map<Long, PublicElectionCoreSnapshot> publicElectionCoreSnapshotByElectionId = new ConcurrentHashMap<>();
	private static final Map<Long, PublicElectionRollSnapshot> publicElectionRollSnapshotByElectionId = new ConcurrentHashMap<>();
	private static final Map<Long, PublicElectionPhotoSnapshot> publicElectionPhotoSnapshotByElectionId = new ConcurrentHashMap<>();
	private static final Map<Long, PublicElectionOfficialResultSnapshot> publicElectionOfficialResultSnapshotByElectionId = new ConcurrentHashMap<>();
	private static volatile PublicElectionsSnapshot publicElectionsSnapshot;
	private static volatile HealthCheck healthCheck;
	private static final Map<String, String> parametersByKey = new ConcurrentHashMap<>();
	private static final Map<Long, Boolean> processingByElectionId = new ConcurrentHashMap<>();
	private static final Map<Long, AsyncProcessingType> processingTypeByElectionId = new ConcurrentHashMap<>();
	private static final Map<Long, AsyncProcessingError> censusProcessingErrorsByElectionId = new ConcurrentHashMap<>();
	private static final Map<Long, AsyncProcessingError> organizationsProcessingErrorsByElectionId = new ConcurrentHashMap<>();
	private static final Map<Long, AsyncProcessingProgress> censusProcessingProgressByElectionId = new ConcurrentHashMap<>();
	private static final Map<Long, AsyncProcessingProgress> organizationsProcessingProgressByElectionId = new ConcurrentHashMap<>();
	private static final Map<String, Integer> aiTextImprovementAttemptsByScopeKey = new ConcurrentHashMap<>();
	private static final Map<String, Integer> campusProgressCheckAttemptsByTrackingKey = new ConcurrentHashMap<>();
	private static final Map<String, Integer> publicFailedAccessAttemptsByIp = new ConcurrentHashMap<>();
	private static final Map<String, Integer> loginCaptchaAttemptsByIp = new ConcurrentHashMap<>();

	private ElectionsCaches() {
		throw new IllegalStateException("Utility class");
	}

	public static void clearPublicElectionSnapshotCache() {
		publicElectionCoreSnapshotByElectionId.clear();
		publicElectionRollSnapshotByElectionId.clear();
		publicElectionPhotoSnapshotByElectionId.clear();
		publicElectionOfficialResultSnapshotByElectionId.clear();
		publicElectionsSnapshot = null;
	}

	public static PublicElectionsSnapshot getPublicElectionsSnapshot() {
		return publicElectionsSnapshot;
	}

	public static void putPublicElectionsSnapshot(PublicElectionsSnapshot data) {
		publicElectionsSnapshot = data;
	}

	public static HealthCheck getHealthCheck() {
		return healthCheck;
	}

	public static void putHealthCheck(HealthCheck data) {
		healthCheck = data;
	}

	public static void clearHealthCheckCache() {
		healthCheck = null;
	}

	public static PublicElectionCoreSnapshot getPublicElectionCoreSnapshot(Long electionId) {
		return publicElectionCoreSnapshotByElectionId.get(electionId);
	}

	public static void putPublicElectionCoreSnapshot(Long electionId, PublicElectionCoreSnapshot data) {
		if (electionId == null || data == null) {
			return;
		}
		publicElectionCoreSnapshotByElectionId.put(electionId, data);
	}

	public static PublicElectionRollSnapshot getPublicElectionRollSnapshot(Long electionId) {
		return publicElectionRollSnapshotByElectionId.get(electionId);
	}

	public static void putPublicElectionRollSnapshot(Long electionId, PublicElectionRollSnapshot data) {
		if (electionId == null || data == null) {
			return;
		}
		publicElectionRollSnapshotByElectionId.put(electionId, data);
	}

	public static PublicElectionPhotoSnapshot getPublicElectionPhotoSnapshot(Long electionId) {
		return publicElectionPhotoSnapshotByElectionId.get(electionId);
	}

	public static void putPublicElectionPhotoSnapshot(Long electionId, PublicElectionPhotoSnapshot data) {
		if (electionId == null || data == null) {
			return;
		}
		publicElectionPhotoSnapshotByElectionId.put(electionId, data);
	}

	public static PublicElectionOfficialResultSnapshot getPublicElectionOfficialResultSnapshot(Long electionId) {
		return publicElectionOfficialResultSnapshotByElectionId.get(electionId);
	}

	public static void putPublicElectionOfficialResultSnapshot(Long electionId, PublicElectionOfficialResultSnapshot data) {
		if (electionId == null || data == null) {
			return;
		}
		publicElectionOfficialResultSnapshotByElectionId.put(electionId, data);
	}

	public static Map<String, String> getParametersCache() {
		return parametersByKey;
	}

	public static void putParameter(String key, String value) {
		if (key == null || value == null) {
			return;
		}
		parametersByKey.put(key, value);
	}

	public static void replaceParametersCache(Map<String, String> values) {
		parametersByKey.clear();
		if (values != null) {
			parametersByKey.putAll(values);
		}
	}

	public static void clearParametersCache() {
		parametersByKey.clear();
	}

	public static int incrementAiTextImprovementAttempt(long candidateId) {
		if (candidateId <= 0L) {
			return 0;
		}
		return incrementAiTextImprovementAttempt("CANDIDATE:" + candidateId);
	}

	public static int incrementAiTextImprovementAttempt(String scopeKey) {
		if (scopeKey == null || scopeKey.trim().isEmpty()) {
			return 0;
		}
		String normalizedScopeKey = scopeKey.trim().toUpperCase(Locale.ROOT);
		return aiTextImprovementAttemptsByScopeKey.merge(normalizedScopeKey, 1, Integer::sum);
	}

	public static void clearAiTextImprovementRateLimitCache() {
		aiTextImprovementAttemptsByScopeKey.clear();
	}

	public static int incrementCampusProgressCheckAttempt(String trackingKey) {
		if (trackingKey == null || trackingKey.trim().isEmpty()) {
			return 0;
		}
		String normalizedTrackingKey = trackingKey.trim().toUpperCase(Locale.ROOT);
		return campusProgressCheckAttemptsByTrackingKey.merge(normalizedTrackingKey, 1, Integer::sum);
	}

	public static void clearCampusProgressCheckRateLimitCache() {
		campusProgressCheckAttemptsByTrackingKey.clear();
	}

	public static void clearCampusProgressCheckRateLimitCacheByButton(String buttonKey) {
		if (buttonKey == null || buttonKey.trim().isEmpty()) {
			return;
		}
		String normalizedButtonKey = buttonKey.trim().toUpperCase(Locale.ROOT);
		String trackingKeyPrefix = "CAMPUS_PROGRESS_CHECK|" + normalizedButtonKey + "|";
		campusProgressCheckAttemptsByTrackingKey.keySet().removeIf(key -> key != null && key.startsWith(trackingKeyPrefix));
	}

	public static int incrementPublicFailedAccessAttempt(String clientIp) {
		if (clientIp == null || clientIp.trim().isEmpty()) {
			return 0;
		}
		String normalizedClientIp = clientIp.trim();
		return publicFailedAccessAttemptsByIp.merge(normalizedClientIp, 1, Integer::sum);
	}

	public static int getPublicFailedAccessAttempts(String clientIp) {
		if (clientIp == null || clientIp.trim().isEmpty()) {
			return 0;
		}
		String normalizedClientIp = clientIp.trim();
		return publicFailedAccessAttemptsByIp.getOrDefault(normalizedClientIp, 0);
	}

	public static void clearPublicFailedAccessRateLimitCache() {
		publicFailedAccessAttemptsByIp.clear();
	}

	public static int incrementLoginCaptchaAttempt(String clientIp) {
		if (clientIp == null || clientIp.trim().isEmpty()) {
			return 0;
		}
		String normalizedClientIp = clientIp.trim().toLowerCase(Locale.ROOT);
		return loginCaptchaAttemptsByIp.merge(normalizedClientIp, 1, Integer::sum);
	}

	public static int getLoginCaptchaAttempts(String clientIp) {
		if (clientIp == null || clientIp.trim().isEmpty()) {
			return 0;
		}
		String normalizedClientIp = clientIp.trim().toLowerCase(Locale.ROOT);
		return loginCaptchaAttemptsByIp.getOrDefault(normalizedClientIp, 0);
	}

	public static void clearLoginCaptchaRateLimitCache() {
		loginCaptchaAttemptsByIp.clear();
	}

	public static HashMap<String, String> copyParametersCache() {
		return new HashMap<>(parametersByKey);
	}

	public static boolean startOrganizationsProcessing(Long electionId) {
		if (electionId == null) {
			return false;
		}
		clearOrganizationsProcessingError(electionId);
		boolean started = startElectionProcessing(electionId, AsyncProcessingType.ORGANIZATIONS);
		if (started) {
			censusProcessingProgressByElectionId.remove(electionId);
			markOrganizationsProcessingPreparing(electionId);
		}
		return started;
	}

	public static void finishOrganizationsProcessing(Long electionId) {
		finishElectionProcessing(electionId);
	}

	public static boolean isOrganizationsProcessing(Long electionId) {
		if (electionId == null) {
			return false;
		}
		return processingByElectionId.containsKey(electionId);
	}

	public static boolean startCensusProcessing(Long electionId) {
		if (electionId == null) {
			return false;
		}
		clearCensusProcessingError(electionId);
		boolean started = startElectionProcessing(electionId, AsyncProcessingType.CENSUS);
		if (started) {
			organizationsProcessingProgressByElectionId.remove(electionId);
			markCensusProcessingPreparing(electionId);
		}
		return started;
	}

	public static void finishCensusProcessing(Long electionId) {
		finishElectionProcessing(electionId);
	}

	public static void switchElectionProcessingType(Long electionId, AsyncProcessingType processingType) {
		if (electionId == null) {
			return;
		}
		if (!processingByElectionId.containsKey(electionId) || processingType == null) {
			return;
		}
		processingTypeByElectionId.put(electionId, processingType);
		if (processingType == AsyncProcessingType.CENSUS) {
			if (!censusProcessingProgressByElectionId.containsKey(electionId)) {
				censusProcessingProgressByElectionId.put(electionId, new AsyncProcessingProgress(0, 0, 0, 0, 0, 0, 0, 0, System.currentTimeMillis(), AsyncProcessingProgress.PHASE_PREPARING));
			}
			organizationsProcessingProgressByElectionId.remove(electionId);
		}
		if (processingType == AsyncProcessingType.ORGANIZATIONS) {
			if (!organizationsProcessingProgressByElectionId.containsKey(electionId)) {
				organizationsProcessingProgressByElectionId.put(electionId, new AsyncProcessingProgress(0, 0, 0, 0, 0, 0, 0, 0, System.currentTimeMillis(), AsyncProcessingProgress.PHASE_PREPARING));
			}
			censusProcessingProgressByElectionId.remove(electionId);
		}
	}

	public static boolean isCensusProcessing(Long electionId) {
		if (electionId == null) {
			return false;
		}
		return processingByElectionId.containsKey(electionId);
	}

	public static boolean isElectionProcessing(Long electionId) {
		if (electionId == null) {
			return false;
		}
		return processingByElectionId.containsKey(electionId);
	}

	public static AsyncProcessingType getElectionProcessingType(Long electionId) {
		if (electionId == null) {
			return null;
		}
		return processingTypeByElectionId.get(electionId);
	}

	public static void setCensusProcessingError(Long electionId, AsyncProcessingError error) {
		if (electionId == null) {
			return;
		}
		if (error == null || error.getMessageKey() == null || error.getMessageKey().trim().isEmpty()) {
			censusProcessingErrorsByElectionId.remove(electionId);
			return;
		}
		censusProcessingErrorsByElectionId.put(electionId, error);
	}

	public static AsyncProcessingError getCensusProcessingError(Long electionId) {
		if (electionId == null) {
			return null;
		}
		return censusProcessingErrorsByElectionId.get(electionId);
	}

	public static void clearCensusProcessingError(Long electionId) {
		if (electionId == null) {
			return;
		}
		censusProcessingErrorsByElectionId.remove(electionId);
	}

	public static void updateCensusProcessingProgress(Long electionId, int processedRows, int totalRows, int createdRows, int updatedRows, int deletedRows) {
		updateCensusProcessingProgress(electionId, processedRows, totalRows, createdRows, updatedRows, deletedRows, 0, 0, 0);
	}

	public static void markCensusProcessingPreparing(Long electionId) {
		markCensusProcessingPreparing(electionId, null);
	}

	public static void markCensusProcessingPreparing(Long electionId, String operationKey) {
		updateCensusProcessingProgress(electionId, 0, 0, 0, 0, 0, 0, 0, 0, AsyncProcessingProgress.PHASE_PREPARING, operationKey);
	}

	public static void updateCensusProcessingProgress(Long electionId, int processedRows, int totalRows, int createdRows, int updatedRows, int deletedRows, int totalCreatedRows, int totalUpdatedRows,
			int totalDeletedRows) {
		updateCensusProcessingProgress(electionId, processedRows, totalRows, createdRows, updatedRows, deletedRows, totalCreatedRows, totalUpdatedRows, totalDeletedRows, AsyncProcessingProgress.PHASE_PLANNED);
	}

	public static void updateCensusProcessingProgress(Long electionId, int processedRows, int totalRows, int createdRows, int updatedRows, int deletedRows, int totalCreatedRows, int totalUpdatedRows,
			int totalDeletedRows, String phase) {
		updateCensusProcessingProgress(electionId, processedRows, totalRows, createdRows, updatedRows, deletedRows, totalCreatedRows, totalUpdatedRows, totalDeletedRows, phase, null);
	}

	public static void updateCensusProcessingProgress(Long electionId, int processedRows, int totalRows, int createdRows, int updatedRows, int deletedRows, int totalCreatedRows, int totalUpdatedRows,
			int totalDeletedRows, String phase, String operationKey) {
		if (electionId == null) {
			return;
		}
		censusProcessingProgressByElectionId.put(electionId,
				new AsyncProcessingProgress(processedRows, totalRows, createdRows, updatedRows, deletedRows, totalCreatedRows, totalUpdatedRows, totalDeletedRows, System.currentTimeMillis(), phase, operationKey));
	}

	public static AsyncProcessingProgress getCensusProcessingProgress(Long electionId) {
		if (electionId == null) {
			return null;
		}
		AsyncProcessingProgress progress = censusProcessingProgressByElectionId.get(electionId);
		if (progress == null) {
			return null;
		}
		return new AsyncProcessingProgress(progress);
	}

	public static void setOrganizationsProcessingError(Long electionId, AsyncProcessingError error) {
		if (electionId == null) {
			return;
		}
		if (error == null || error.getMessageKey() == null || error.getMessageKey().trim().isEmpty()) {
			organizationsProcessingErrorsByElectionId.remove(electionId);
			return;
		}
		organizationsProcessingErrorsByElectionId.put(electionId, error);
	}

	public static AsyncProcessingError getOrganizationsProcessingError(Long electionId) {
		if (electionId == null) {
			return null;
		}
		return organizationsProcessingErrorsByElectionId.get(electionId);
	}

	public static void clearOrganizationsProcessingError(Long electionId) {
		if (electionId == null) {
			return;
		}
		organizationsProcessingErrorsByElectionId.remove(electionId);
	}

	public static void updateOrganizationsProcessingProgress(Long electionId, int processedRows, int totalRows, int createdRows, int updatedRows, int deletedRows) {
		updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, createdRows, updatedRows, deletedRows, 0, 0, 0);
	}

	public static void markOrganizationsProcessingPreparing(Long electionId) {
		markOrganizationsProcessingPreparing(electionId, null);
	}

	public static void markOrganizationsProcessingPreparing(Long electionId, String operationKey) {
		updateOrganizationsProcessingProgress(electionId, 0, 0, 0, 0, 0, 0, 0, 0, AsyncProcessingProgress.PHASE_PREPARING, operationKey);
	}

	public static void updateOrganizationsProcessingProgress(Long electionId, int processedRows, int totalRows, int createdRows, int updatedRows, int deletedRows, int totalCreatedRows, int totalUpdatedRows,
			int totalDeletedRows) {
		updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, createdRows, updatedRows, deletedRows, totalCreatedRows, totalUpdatedRows, totalDeletedRows, AsyncProcessingProgress.PHASE_PLANNED);
	}

	public static void updateOrganizationsProcessingProgress(Long electionId, int processedRows, int totalRows, int createdRows, int updatedRows, int deletedRows, int totalCreatedRows, int totalUpdatedRows,
			int totalDeletedRows, String phase) {
		updateOrganizationsProcessingProgress(electionId, processedRows, totalRows, createdRows, updatedRows, deletedRows, totalCreatedRows, totalUpdatedRows, totalDeletedRows, phase, null);
	}

	public static void updateOrganizationsProcessingProgress(Long electionId, int processedRows, int totalRows, int createdRows, int updatedRows, int deletedRows, int totalCreatedRows, int totalUpdatedRows,
			int totalDeletedRows, String phase, String operationKey) {
		if (electionId == null) {
			return;
		}
		organizationsProcessingProgressByElectionId.put(electionId,
				new AsyncProcessingProgress(processedRows, totalRows, createdRows, updatedRows, deletedRows, totalCreatedRows, totalUpdatedRows, totalDeletedRows, System.currentTimeMillis(), phase, operationKey));
	}

	public static AsyncProcessingProgress getOrganizationsProcessingProgress(Long electionId) {
		if (electionId == null) {
			return null;
		}
		AsyncProcessingProgress progress = organizationsProcessingProgressByElectionId.get(electionId);
		if (progress == null) {
			return null;
		}
		return new AsyncProcessingProgress(progress);
	}

	private static boolean startElectionProcessing(Long electionId, AsyncProcessingType processingType) {
		boolean started = processingByElectionId.putIfAbsent(electionId, Boolean.TRUE) == null;
		if (!started) {
			return false;
		}
		processingTypeByElectionId.put(electionId, processingType);
		if (processingType == AsyncProcessingType.CENSUS) {
			markCensusProcessingPreparing(electionId);
		}
		if (processingType == AsyncProcessingType.ORGANIZATIONS) {
			markOrganizationsProcessingPreparing(electionId);
		}
		return true;
	}

	private static void finishElectionProcessing(Long electionId) {
		if (electionId == null) {
			return;
		}
		processingByElectionId.remove(electionId);
		processingTypeByElectionId.remove(electionId);
		censusProcessingProgressByElectionId.remove(electionId);
		organizationsProcessingProgressByElectionId.remove(electionId);
	}
}
