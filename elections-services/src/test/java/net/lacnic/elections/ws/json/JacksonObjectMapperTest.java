package net.lacnic.elections.ws.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

class JacksonObjectMapperTest {

	private static class SamplePayload {
		private String nullableField;

		public String getNullableField() {
			return nullableField;
		}
	}

	@Test
	void getReturnsMapperConfiguredToIncludeNullValues() throws Exception {
		JacksonObjectMapper mapper = JacksonObjectMapper.get();

		assertNotNull(mapper);
		assertEquals("{\"nullableField\":null}", mapper.writeValueAsString(new SamplePayload()));
	}

	@Test
	void getReturnsNewMapperInstanceOnEachCall() {
		JacksonObjectMapper mapperA = JacksonObjectMapper.get();
		JacksonObjectMapper mapperB = JacksonObjectMapper.get();

		assertNotSame(mapperA, mapperB);
	}
}
