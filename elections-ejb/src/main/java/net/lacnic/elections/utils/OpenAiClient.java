package net.lacnic.elections.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.lacnic.elections.domain.pre.CandidateTextImprovementInstruction;

public final class OpenAiClient {

	private static final Logger appLogger = LoggerFactory.getLogger("ejbAppLogger");
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final String DEFAULT_MODEL = "gpt-4o-mini";
	private static final String STRICT_JSON_MESSAGE = "Responde solo con JSON valido. No uses markdown, bloques de codigo ni texto adicional.";
	private static final String TEXT_START_DELIMITER = "<<<INICIO_TEXTO>>>";
	private static final String TEXT_END_DELIMITER = "<<<FIN_TEXTO>>>";
	private static final String QUESTION_START_DELIMITER = "<<<INICIO_PREGUNTA>>>";
	private static final String QUESTION_END_DELIMITER = "<<<FIN_PREGUNTA>>>";
	private static final String CHAT_COMPLETIONS_PATH = "/v1/chat/completions";
	private static final String JSON_FIELD_CONTENT = "content";
	private static final String JSON_FIELD_JSON_SCHEMA = "json_schema";
	private static final String JSON_FIELD_MESSAGE = "message";
	private static final String JSON_FIELD_RESPUESTA = "respuesta";
	private static final String JSON_FIELD_ROLE = "role";
	private static final String JSON_FIELD_TYPE = "type";
	private static final String SAFE_LOG_VALUE = "-";
	private static final int OPENAI_CONNECT_TIMEOUT_SECONDS = 10;
	private static final int OPENAI_RESPONSE_TIMEOUT_SECONDS = 60;
	private static final int OPENAI_CONNECTION_REQUEST_TIMEOUT_SECONDS = 10;
	private static final int MAX_LOG_TEXT_LENGTH = 200;

	private OpenAiClient() {
		throw new IllegalStateException("Utility class");
	}

	public static TextImprovementResult improveCandidateText(
			String basePrompt,
			String originalText,
			CandidateTextImprovementInstruction instruction) {
		return improveCandidateText(basePrompt, originalText, instruction, null);
	}

	public static TextImprovementResult improveCandidateText(
			String basePrompt,
			String originalText,
			CandidateTextImprovementInstruction instruction,
			String questionText) {
		if (!isTextImprovementEnabled()) {
			return null;
		}
		if (StringUtils.isBlank(basePrompt) || StringUtils.isBlank(originalText) || instruction == null) {
			return null;
		}

		String openAiUrl = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.OPENAI_URL);
		String apiKey = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.OPENAI_API_KEY);
		String configuredModel = EJBFactory.getInstance().getElectionsParametersEJB().getParameter(Constants.OPENAI_MODEL);
		String model = StringUtils.defaultIfBlank(configuredModel, DEFAULT_MODEL);
		if (StringUtils.isBlank(openAiUrl) || StringUtils.isBlank(apiKey)) {
			appLogger.warn("OpenAI configuration is incomplete. urlConfigured={}, apiKeyConfigured={}",
					StringUtils.isNotBlank(openAiUrl),
					StringUtils.isNotBlank(apiKey));
			return null;
		}
		if (!isChatCompletionsUrl(openAiUrl)) {
			appLogger.error(
					"Unsupported OpenAI URL for text improvement. expectedPath={}, configuredUrl={}",
					CHAT_COMPLETIONS_PATH,
					StringUtils.abbreviate(StringUtils.trimToEmpty(openAiUrl), MAX_LOG_TEXT_LENGTH));
			return null;
		}

		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(Timeout.ofSeconds(OPENAI_CONNECT_TIMEOUT_SECONDS))
				.setResponseTimeout(Timeout.ofSeconds(OPENAI_RESPONSE_TIMEOUT_SECONDS))
				.setConnectionRequestTimeout(Timeout.ofSeconds(OPENAI_CONNECTION_REQUEST_TIMEOUT_SECONDS))
				.build();
		try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build()) {
			String systemPrompt = buildSystemPrompt(basePrompt, instruction);
			String userPrompt = buildUserPrompt(originalText, instruction, questionText);
			String requestPayload = buildPayload(model, systemPrompt, userPrompt);
			appLogger.info("Calling OpenAI. url={}, model={}", openAiUrl, model);
			HttpPost request = new HttpPost(openAiUrl);
			request.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
			request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
			request.setEntity(new StringEntity(requestPayload, ContentType.APPLICATION_JSON));

			try (CloseableHttpResponse response = httpClient.execute(request)) {
				String responseBody = readResponseBody(response);
				int statusCode = response.getCode();
				String requestId = resolveResponseHeader(response, "x-request-id");
				if (statusCode < 200 || statusCode >= 300) {
					appLogger.error(
							"OpenAI request failed. statusCode={}, requestId={}, summary={}",
							statusCode,
							requestId,
							buildSafeResponseSummary(responseBody));
					return null;
				}

				String content = extractContentFromResponse(responseBody);
				if (StringUtils.isBlank(content)) {
					appLogger.error(
							"OpenAI response did not contain content. statusCode={}, requestId={}, summary={}",
							statusCode,
							requestId,
							buildSafeResponseSummary(responseBody));
					return null;
				}

				TextImprovementResult improvementResult = parseImprovementResult(content);
				if (improvementResult == null || StringUtils.isBlank(improvementResult.getText())) {
					return null;
				}
				return improvementResult;
			}
		} catch (Exception e) {
			appLogger.error("Error calling OpenAI text improvement service", e);
			return null;
		}
	}

	public static boolean isTextImprovementEnabled() {
		try {
			String configuredValue = EJBFactory.getInstance().getElectionsParametersEJB()
					.getParameter(Constants.AI_TEXT_IMPROVEMENT_ENABLED);
			return isTextImprovementEnabled(configuredValue);
		} catch (Exception e) {
			return false;
		}
	}

	static boolean isTextImprovementEnabled(String configuredValue) {
		return StringUtils.isBlank(configuredValue) || Boolean.parseBoolean(configuredValue.trim());
	}

	private static String buildPayload(String model, String systemPrompt, String userPrompt) throws Exception {
		return buildChatCompletionsPayload(model, systemPrompt, userPrompt);
	}

	private static String buildChatCompletionsPayload(String model, String systemPrompt, String userPrompt) throws Exception {
		ObjectNode root = OBJECT_MAPPER.createObjectNode();
		root.put("model", model);
		root.put("temperature", 0.2);
		root.set("response_format", buildChatCompletionsResponseFormat());

		ArrayNode messages = OBJECT_MAPPER.createArrayNode();
		ObjectNode systemMessage = OBJECT_MAPPER.createObjectNode();
		systemMessage.put(JSON_FIELD_ROLE, "system");
		systemMessage.put(JSON_FIELD_CONTENT, systemPrompt + "\n\n" + STRICT_JSON_MESSAGE);
		messages.add(systemMessage);

		ObjectNode userMessage = OBJECT_MAPPER.createObjectNode();
		userMessage.put(JSON_FIELD_ROLE, "user");
		userMessage.put(JSON_FIELD_CONTENT, userPrompt);
		messages.add(userMessage);

		root.set("messages", messages);
		return OBJECT_MAPPER.writeValueAsString(root);
	}

	private static ObjectNode buildChatCompletionsResponseFormat() {
		ObjectNode responseFormat = OBJECT_MAPPER.createObjectNode();
		responseFormat.put(JSON_FIELD_TYPE, JSON_FIELD_JSON_SCHEMA);
		ObjectNode jsonSchema = OBJECT_MAPPER.createObjectNode();
		jsonSchema.put("name", "candidate_text_improvement_result");
		jsonSchema.set("schema", buildImprovementResultSchema());
		jsonSchema.put("strict", true);
		responseFormat.set(JSON_FIELD_JSON_SCHEMA, jsonSchema);
		return responseFormat;
	}

	private static ObjectNode buildImprovementResultSchema() {
		return buildSingleResponseSchema();
	}

	private static ObjectNode buildSingleResponseSchema() {
		ObjectNode schema = OBJECT_MAPPER.createObjectNode();
		schema.put(JSON_FIELD_TYPE, "object");
		schema.put("additionalProperties", false);

		ObjectNode properties = OBJECT_MAPPER.createObjectNode();
		properties.set(JSON_FIELD_RESPUESTA, buildStringSchema());
		schema.set("properties", properties);
		schema.set("required", buildRequiredArray(JSON_FIELD_RESPUESTA));
		return schema;
	}

	private static ObjectNode buildStringSchema() {
		ObjectNode schema = OBJECT_MAPPER.createObjectNode();
		schema.put(JSON_FIELD_TYPE, "string");
		return schema;
	}

	private static ArrayNode buildRequiredArray(String... keys) {
		ArrayNode required = OBJECT_MAPPER.createArrayNode();
		for (String key : keys) {
			required.add(key);
		}
		return required;
	}

	static String buildSystemPrompt(
			String basePrompt,
			CandidateTextImprovementInstruction instruction) {
		StringBuilder promptBuilder = new StringBuilder();
		promptBuilder.append(basePrompt.trim()).append('\n');
		appendMandatoryRules(promptBuilder, instruction);
		promptBuilder.append("Formato de salida obligatorio: ");
		promptBuilder.append("{\"respuesta\":\"string\"}\n");
		return promptBuilder.toString();
	}

	static String buildUserPrompt(
			String originalText,
			CandidateTextImprovementInstruction instruction,
			String questionText) {
		StringBuilder promptBuilder = new StringBuilder();
		if (instruction == CandidateTextImprovementInstruction.WRITING_STYLE_REVIEW && StringUtils.isNotBlank(questionText)) {
			promptBuilder.append("Contexto adicional (pregunta asociada):\n");
			promptBuilder.append(QUESTION_START_DELIMITER).append('\n');
			promptBuilder.append(questionText.trim()).append('\n');
			promptBuilder.append(QUESTION_END_DELIMITER).append('\n');
			promptBuilder.append('\n');
		}
		if (instruction != null && instruction.isTranslation()) {
			String targetLanguage = StringUtils.defaultIfBlank(
					StringUtils.defaultIfBlank(instruction.getTargetLanguageLabel(), instruction.getTargetLanguage()),
					"es");
			promptBuilder.append("Idioma de destino: ").append(targetLanguage).append('\n');
			promptBuilder.append('\n');
		}
		promptBuilder.append("Texto de entrada (contenido literal):\n");
		promptBuilder.append(TEXT_START_DELIMITER).append('\n');
		promptBuilder.append(originalText).append('\n');
		promptBuilder.append(TEXT_END_DELIMITER);
		return promptBuilder.toString();
	}

	private static void appendMandatoryRules(StringBuilder promptBuilder, CandidateTextImprovementInstruction instruction) {
		if (instruction == CandidateTextImprovementInstruction.SPELLING_REVIEW) {
			promptBuilder.append("Reglas obligatorias: corrige tildes, puntuacion, mayusculas y errores ortograficos. ");
			promptBuilder.append("Mantiene nombres propios, siglas, terminos tecnicos y partes en ingles sin cambios innecesarios. ");
			promptBuilder.append("Si no hay errores, devuelve exactamente el texto original en respuesta. ");
			promptBuilder.append('\n');
			return;
		}
		if (instruction != null && instruction.isTranslation()) {
			promptBuilder.append("Reglas obligatorias: traduce al idioma destino y conserva el significado original. ");
			promptBuilder.append("No agregues contenido nuevo ni elimines información útil del texto original. ");
			promptBuilder.append("Respeta nombres propios, fechas, siglas, términos técnicos y acrónimos. ");
			promptBuilder.append("Mantén estructura, tono y formato en lo posible. ");
			promptBuilder.append("Si una parte es ambigua o no traducible de forma confiable, mantenla en texto original sin explicación. ");
			promptBuilder.append("Si el texto ya está en el idioma destino, devuelve exactamente el texto original en respuesta. ");
			promptBuilder.append('\n');
			return;
		}
		promptBuilder.append("Reglas obligatorias: mejora claridad, cohesion y fluidez sin agregar informacion nueva. ");
		promptBuilder.append("Respeta nombres propios, siglas, terminos tecnicos y partes en ingles. ");
		promptBuilder.append("Si no hay mejoras claras para aplicar, devuelve exactamente el texto original en respuesta. ");
		promptBuilder.append("Si hay una pregunta de contexto, usala solo para interpretar la respuesta y no la incluyas en la salida.\n");
	}

	private static String readResponseBody(CloseableHttpResponse response) throws Exception {
		HttpEntity responseEntity = response.getEntity();
		if (responseEntity == null) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent(), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				result.append(line);
			}
		}
		return result.toString();
	}

	static String extractContentFromResponse(String responseBody) throws Exception {
		JsonNode root = OBJECT_MAPPER.readTree(responseBody);
		JsonNode chatCompletionContent = root.path("choices").path(0).path(JSON_FIELD_MESSAGE).path(JSON_FIELD_CONTENT);
		if (chatCompletionContent.isTextual()) {
			return chatCompletionContent.asText();
		}
		return null;
	}

	static TextImprovementResult parseImprovementResult(String content) {
		try {
			JsonNode root = OBJECT_MAPPER.readTree(StringUtils.trimToEmpty(content));
			String improvedText = StringUtils.trimToNull(root.path(JSON_FIELD_RESPUESTA).asText(null));
			if (improvedText == null) {
				return null;
			}
			improvedText = StringUtils.trimToNull(decodeSafeHtmlTextEntities(improvedText));
			if (improvedText == null) {
				return null;
			}
			return new TextImprovementResult(improvedText);
		} catch (Exception e) {
			appLogger.error("Error parsing OpenAI improvement response. summary={}", buildSafeContentSummary(content), e);
			return null;
		}
	}

	private static String decodeSafeHtmlTextEntities(String value) {
		if (StringUtils.isEmpty(value)) {
			return value;
		}

		String decoded = value;
		decoded = StringUtils.replace(decoded, "&#38;", "&");
		decoded = StringUtils.replace(decoded, "&#x26;", "&");
		decoded = StringUtils.replace(decoded, "&#X26;", "&");
		decoded = StringUtils.replaceIgnoreCase(decoded, "&amp;", "&");
		decoded = StringUtils.replaceIgnoreCase(decoded, "&nbsp;", " ");
		decoded = StringUtils.replace(decoded, "&#160;", " ");
		decoded = StringUtils.replace(decoded, "&#xA0;", " ");
		decoded = StringUtils.replace(decoded, "&#xa0;", " ");
		decoded = StringUtils.replaceIgnoreCase(decoded, "&quot;", "\"");
		decoded = StringUtils.replace(decoded, "&#34;", "\"");
		decoded = StringUtils.replace(decoded, "&#x22;", "\"");
		decoded = StringUtils.replace(decoded, "&#X22;", "\"");
		decoded = StringUtils.replaceIgnoreCase(decoded, "&apos;", "'");
		decoded = StringUtils.replace(decoded, "&#39;", "'");
		decoded = StringUtils.replace(decoded, "&#x27;", "'");
		decoded = StringUtils.replace(decoded, "&#X27;", "'");

		return decoded;
	}

	static boolean isChatCompletionsUrl(String openAiUrl) {
		if (StringUtils.isBlank(openAiUrl)) {
			return false;
		}
		try {
			URI uri = URI.create(openAiUrl.trim());
			String path = StringUtils.removeEnd(StringUtils.defaultString(uri.getPath()), "/");
			return CHAT_COMPLETIONS_PATH.equals(path);
		} catch (Exception ex) {
			return false;
		}
	}

	private static String resolveResponseHeader(CloseableHttpResponse response, String headerName) {
		if (response == null || StringUtils.isBlank(headerName) || response.getFirstHeader(headerName) == null) {
			return SAFE_LOG_VALUE;
		}
		return StringUtils.defaultIfBlank(response.getFirstHeader(headerName).getValue(), SAFE_LOG_VALUE);
	}

	private static String buildSafeResponseSummary(String responseBody) {
		if (StringUtils.isBlank(responseBody)) {
			return "empty_response_body";
		}

		try {
			JsonNode root = OBJECT_MAPPER.readTree(responseBody);
			JsonNode errorNode = root.path("error");
			if (!errorNode.isMissingNode() && !errorNode.isNull()) {
				String type = StringUtils.defaultIfBlank(StringUtils.trimToNull(errorNode.path(JSON_FIELD_TYPE).asText(null)), SAFE_LOG_VALUE);
				String code = StringUtils.defaultIfBlank(StringUtils.trimToNull(errorNode.path("code").asText(null)), SAFE_LOG_VALUE);
				String message = StringUtils.defaultIfBlank(StringUtils.trimToNull(errorNode.path(JSON_FIELD_MESSAGE).asText(null)), SAFE_LOG_VALUE);
				return "error_type=" + type + ", error_code=" + code + ", error_message=" + StringUtils.abbreviate(message, MAX_LOG_TEXT_LENGTH);
			}

			List<String> keys = new ArrayList<>();
			root.fieldNames().forEachRemaining(keys::add);
			String keySummary = keys.isEmpty() ? SAFE_LOG_VALUE : StringUtils.join(keys, ',');
			return "json_keys=" + StringUtils.abbreviate(keySummary, MAX_LOG_TEXT_LENGTH) + ", body_length=" + responseBody.length();
		} catch (Exception ex) {
			return "non_json_response body_length=" + responseBody.length();
		}
	}

	private static String buildSafeContentSummary(String content) {
		if (StringUtils.isBlank(content)) {
			return "empty_content";
		}
		String trimmed = StringUtils.trimToEmpty(content);
		return "content_length="
				+ content.length()
				+ ", starts_with_fence="
				+ trimmed.startsWith("```")
				+ ", starts_with_brace="
				+ trimmed.startsWith("{");
	}

	public static final class TextImprovementResult {
		private final String text;

		public TextImprovementResult(String text) {
			this.text = text;
		}

		public String getText() {
			return text;
		}
	}
}
