package net.lacnic.elections.utils;

import java.util.List;

import net.lacnic.elections.domain.UserVoter;


public interface IExcelUtils {

	public List<UserVoter> processCensusExcel(byte[] content) throws Exception;
}
