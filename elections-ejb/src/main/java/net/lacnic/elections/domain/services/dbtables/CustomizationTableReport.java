package net.lacnic.elections.domain.services.dbtables;

import java.io.Serializable;
import java.util.Base64;

import net.lacnic.elections.domain.Customization;


public class CustomizationTableReport implements Serializable {

	private static final long serialVersionUID = 6467734290258269435L;

	private Long customizationId;
	private String picSmallLogo;
	private String picBigLogo;
	private String picSymbol;
	private String contPicSmallLogo;
	private String contPicBigLogo;
	private String contPicSymbol;
	private String siteTitle;
	private String loginTitle;
	private Boolean showHome;
	private String homeHtml;


	public CustomizationTableReport() { }

	public CustomizationTableReport(Customization customization) {
		this.customizationId = customization.getCustomizationId();
		this.picSmallLogo = customization.getPicSmallLogo();
		this.picBigLogo = customization.getPicBigLogo();
		this.picSymbol = customization.getPicSymbol();
		this.contPicSmallLogo = customization.getContPicSmallLogo() != null ? Base64.getEncoder().encodeToString(customization.getContPicSmallLogo()) : "";
		this.contPicBigLogo = customization.getContPicBigLogo() != null ? Base64.getEncoder().encodeToString(customization.getContPicBigLogo()) : "";
		this.contPicSymbol = customization.getContPicSymbol() != null ? Base64.getEncoder().encodeToString(customization.getContPicSymbol()) : "";
		this.siteTitle = customization.getSiteTitle();
		this.loginTitle = customization.getLoginTitle();
		this.showHome = customization.isShowHome();
		this.homeHtml = customization.getHomeHtml();
	}


	public Long getCustomizationId() {
		return customizationId;
	}

	public void setCustomizationId(Long customizationId) {
		this.customizationId = customizationId;
	}

	public String getPicSmallLogo() {
		return picSmallLogo;
	}

	public void setPicSmallLogo(String picSmallLogo) {
		this.picSmallLogo = picSmallLogo;
	}

	public String getPicBigLogo() {
		return picBigLogo;
	}

	public void setPicBigLogo(String picBigLogo) {
		this.picBigLogo = picBigLogo;
	}

	public String getPicSymbol() {
		return picSymbol;
	}

	public void setPicSymbol(String picSymbol) {
		this.picSymbol = picSymbol;
	}

	public String getContPicSmallLogo() {
		return contPicSmallLogo;
	}

	public void setContPicSmallLogo(String contPicSmallLogo) {
		this.contPicSmallLogo = contPicSmallLogo;
	}

	public String getContPicBigLogo() {
		return contPicBigLogo;
	}

	public void setContPicBigLogo(String contPicBigLogo) {
		this.contPicBigLogo = contPicBigLogo;
	}

	public String getContPicSymbol() {
		return contPicSymbol;
	}

	public void setContPicSymbol(String contPicSymbol) {
		this.contPicSymbol = contPicSymbol;
	}

	public String getSiteTitle() {
		return siteTitle;
	}

	public void setSiteTitle(String siteTitle) {
		this.siteTitle = siteTitle;
	}

	public String getLoginTitle() {
		return loginTitle;
	}

	public void setLoginTitle(String loginTitle) {
		this.loginTitle = loginTitle;
	}

	public Boolean isShowHome() {
		return showHome;
	}

	public void setShowHome(Boolean showHome) {
		this.showHome = showHome;
	}

	public String getHomeHtml() {
		return homeHtml;
	}

	public void setHomeHtml(String homeHtml) {
		this.homeHtml = homeHtml;
	}

}
