package net.lacnic.elections.ejb.commons;

import java.util.List;

import javax.ejb.Remote;

import net.lacnic.elections.domain.Parameter;


@Remote
public interface ElectionsParametersEJB {

	List<Parameter> getParametersAll();
	
	String getParameter(String key);

	boolean isProd();

	boolean addParameter(String key, String value);

	void editParameter(Parameter p);

	void deleteParameter(String key);

	String getDataSiteKey();

}
