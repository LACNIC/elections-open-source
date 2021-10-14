package net.lacnic.elections.ws.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JacksonObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = -775912127480187728L;

	public static JacksonObjectMapper get() {
		JacksonObjectMapper mapper = new JacksonObjectMapper();
		mapper.setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS);
		return mapper;
	}

}
