package net.lacnic.elections.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.lacnic.elections.domain.pre.CandidateTextImprovementInstruction;
import net.lacnic.elections.ejb.commons.ElectionsParametersEJB;

class OpenAiHttpCompatibilityTest {

    private HttpServer server;
    private ElectionsParametersEJB originalParameters;
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();

    @BeforeEach
    void setUp() throws Exception {
        originalParameters = EJBFactory.getInstance().getElectionsParametersEJB();
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        ElectionsParametersEJB parameters = mock(ElectionsParametersEJB.class);
        when(parameters.getParameter(Constants.AI_TEXT_IMPROVEMENT_ENABLED)).thenReturn("true");
        when(parameters.getParameter(Constants.OPENAI_URL)).thenReturn(
                "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/chat/completions");
        when(parameters.getParameter(Constants.OPENAI_API_KEY)).thenReturn("local-test-key");
        when(parameters.getParameter(Constants.OPENAI_MODEL)).thenReturn("local-test-model");
        EJBFactory.getInstance().setElectionsParametersEJB(parameters);
    }

    @AfterEach
    void tearDown() {
        if (server != null) server.stop(0);
        EJBFactory.getInstance().setElectionsParametersEJB(originalParameters);
    }

    @Test
    void applicationClientReadsGzipJsonAndSendsUtf8Payload() throws Exception {
        serve(200, "{\"choices\":[{\"message\":{\"content\":\"{\\\"respuesta\\\":\\\"Biografía corregida\\\"}\"}}]}", true);
        OpenAiClient.TextImprovementResult result = improve();

        assertNotNull(result);
        assertEquals("Biografía corregida", result.getText());
        JsonNode payload = new ObjectMapper().readTree(requestBody.get());
        assertEquals("local-test-model", payload.path("model").asText());
        assertTrue(payload.path("messages").get(1).path("content").asText().contains("Biografía de prueba"));
        assertEquals("Bearer local-test-key", authorization.get());
    }

    @Test
    void applicationClientHandlesHttpFailureWithoutReturningCandidateText() throws Exception {
        serve(503, "{\"error\":{\"message\":\"local test unavailable\"}}", false);
        assertNull(improve());
        assertNotNull(requestBody.get());
    }

    private OpenAiClient.TextImprovementResult improve() {
        return OpenAiClient.improveCandidateText("Corrige ortografía", "Biografía de prueba",
                CandidateTextImprovementInstruction.SPELLING_REVIEW);
    }

    private void serve(int status, String body, boolean gzip) throws Exception {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        if (gzip) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (GZIPOutputStream output = new GZIPOutputStream(buffer)) { output.write(bytes); }
            bytes = buffer.toByteArray();
        }
        byte[] response = bytes;
        server.createContext("/v1/chat/completions", exchange -> {
            try (exchange) {
                requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                if (gzip) exchange.getResponseHeaders().set("Content-Encoding", "gzip");
                exchange.sendResponseHeaders(status, response.length);
                exchange.getResponseBody().write(response);
            }
        });
        server.start();
    }
}
