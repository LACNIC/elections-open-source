package net.lacnic.elections.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;


@Entity
public class Customization implements Serializable {

	private static final long serialVersionUID = 3371727214644352916L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customization_seq")
	@SequenceGenerator(name = "customization_seq", sequenceName = "customization_seq", allocationSize = 1)
	@Column(name = "customization_id")
	private long customizationId;

	@Column(name = "pic_small_logo", nullable = false, length = 255)
	private String picSmallLogo;

	@Column(name = "pic_big_logo", nullable = false, length = 255)
	private String picBigLogo;

	@Column(name = "pic_symbol",nullable = false, length = 255)
	private String picSymbol;

	@Column(name = "cont_pic_small_logo", nullable = true)
	private byte[] contPicSmallLogo;

	@Column(name = "cont_pic_big_logo", nullable = true)
	private byte[] contPicBigLogo;

	@Column(name = "cont_pic_symbol", nullable = true)
	private byte[] contPicSymbol;

	@Column(name = "site_title", nullable = false, length = 255)
	private String siteTitle;

	@Column(name = "login_title", nullable = false, length = 255)
	private String loginTitle;

	@Column(name = "show_home", nullable = false)
	private boolean showHome;

	@Column(name = "home_html", nullable = true, columnDefinition = "TEXT")
	private String homeHtml;


	public Customization() { }


	public long getCustomizationId() {
		return customizationId;
	}

	public void setCustomizationId(long customizationId) {
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

	public byte[] getContPicSmallLogo() {
		return contPicSmallLogo;
	}

	public void setContPicSmallLogo(byte[] contPicSmallLogo) {
		this.contPicSmallLogo = contPicSmallLogo;
	}

	public byte[] getContPicBigLogo() {
		return contPicBigLogo;
	}

	public void setContPicBigLogo(byte[] contPicBigLogo) {
		this.contPicBigLogo = contPicBigLogo;
	}

	public byte[] getContPicSymbol() {
		return contPicSymbol;
	}

	public void setContPicSymbol(byte[] contPicSymbol) {
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

	public boolean isShowHome() {
		return showHome;
	}

	public void setShowHome(boolean showHome) {
		this.showHome = showHome;
	}

	public String getHomeHtml() {
		return homeHtml;
	}

	public void setHomeHtml(String homeHtml) {
		this.homeHtml = homeHtml;
	}

}
