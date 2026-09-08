package net.lacnic.elections.ws.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class JacksonConfigurationProviderTest {

	private static class SamplePayload {
		private String nullableField;

		public String getNullableField() {
			return nullableField;
		}
	}

	@Test
	void getContextReturnsConfiguredJacksonMapper() throws Exception {
		JacksonConfigurationProvider provider = new JacksonConfigurationProvider();

		ObjectMapper mapper = provider.getContext(SamplePayload.class);

		assertNotNull(mapper);
		assertInstanceOf(JacksonObjectMapper.class, mapper);
		assertEquals("{\"nullableField\":null}", mapper.writeValueAsString(new SamplePayload()));
	}

	@Test
	void getContextReturnsNewMapperEachTime() {
		JacksonConfigurationProvider provider = new JacksonConfigurationProvider();

		ObjectMapper mapperA = provider.getContext(String.class);
		ObjectMapper mapperB = provider.getContext(Integer.class);

		assertNotSame(mapperA, mapperB);
	}
}
