package net.lacnic.elections.ws.services.util;

import net.lacnic.elections.ws.app.AppContext;


public class PagingUtil {

	public static final Integer MAX_PAGE_SIZE = AppContext.getInstance().getMonitorBeanRemote().getWsMaxPageSize();


	public static final boolean validatePagingParameters(Integer pageSize, Integer offset) {
		return !(pageSize == null || offset == null || offset < 0 || pageSize <= 0 || pageSize > MAX_PAGE_SIZE);
	}

	public static final String getPagingInfoResponse(String scheme, String serverName, int serverPort, String servicePath) {
		StringBuilder message = new StringBuilder();
		String server = serverName;
		if(serverPort != 80 && serverPort != 443) {
			server += ":" + serverPort;
		}
		message.append("Parámetros de paginado incorrectos. Debe incluir tamaño de página (máximo " + MAX_PAGE_SIZE + ") y offset (en páginas), en ese orden. Ejemplos:\n");
		message.append("Primera página de 10 elementos: " + scheme + "://" + server + "/elections-ws" + servicePath + "/10/0\n");
		message.append("Segunda página de 10 elementos: " + scheme + "://" + server + "/elections-ws" + servicePath + "/10/1\n");

		return message.toString();
	}

}
