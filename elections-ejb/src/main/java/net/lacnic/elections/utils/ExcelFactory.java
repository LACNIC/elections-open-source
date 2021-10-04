package net.lacnic.elections.utils;

public class ExcelFactory {
private  final String XLSXType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
private final String XLSType="application/vnd.ms-excel";

	public IExcelUtils getExcelUtil(String excelType) {
		switch(excelType) {
		case  XLSXType:
			return new ExcelUtilsXLSX();
		case XLSType:
			return new ExcelUtilsXLS();
		}
		return null;
	}
}
