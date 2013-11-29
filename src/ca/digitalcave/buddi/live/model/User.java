package ca.digitalcave.buddi.live.model;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;

import org.apache.commons.lang.StringUtils;

public class User extends org.restlet.security.User {
	private Integer id;
	private String plaintextIdentifier;	//Not hashed, injected by BuddiVerifier
	private String encryptionKey;
	private String decryptedEncryptionKey;	//Not persisted, injected by BuddiVerifier; deprecated.  Once all users are off of encryption version 1, we can delete this.
	private SecretKey decryptedSecretKey;	//Not persisted, injected by BuddiVerifier
	private String uuid;
	private Boolean premium = false;
	private Locale locale;
	private Currency currency;
	private String overrideDateFormat;
	private Boolean showCleared;
	private Boolean showReconciled;
	private Boolean showDeleted;
	private Date created;
	private Date modified;

	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getPlaintextIdentifier() {
		return plaintextIdentifier;
	}
	public void setPlaintextIdentifier(String plaintextIdentifier) {
		this.plaintextIdentifier = plaintextIdentifier;
	}
	public void setSecretString(String secret) {
		setSecret(secret == null ? null : secret.toCharArray());
	}
	public String getSecretString() {
		return getSecret() == null ? null : new String(getSecret());
	}
	public String getEncryptionKey() {
		return encryptionKey;
	}
	public void setEncryptionKey(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}
	public String getDecryptedEncryptionKey() {
		return decryptedEncryptionKey;
	}
	public void setDecryptedEncryptionKey(String decryptedEncryptionKey) {
		this.decryptedEncryptionKey = decryptedEncryptionKey;
	}
	public SecretKey getDecryptedSecretKey() {
		return decryptedSecretKey;
	}
	public void setDecryptedSecretKey(SecretKey decryptedSecretKey) {
		this.decryptedSecretKey = decryptedSecretKey;
	}
	public boolean isEncrypted(){
		return encryptionKey != null;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	
	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}
	
	public boolean isPremium() {
		return premium;
	}
	public void setPremium(boolean premium) {
		this.premium = premium;
	}
	public Locale getLocale() {
		return locale;
	}
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	public String getExtDateFormat(){
		//Auto converts from Java format to EXT JS (PHP) format.
		//TODO This may need tweaking for accuracy and performance.
		return getDateFormat()
				.replaceAll("yyyy", "Y")
				.replaceAll("yy", "y")
				.replaceAll("ddd", "D")
				.replaceAll("dd?", "d")
				//.replaceAll("([^M]?)M([^M]?)", "$1n$2")		//Single M should be replaced with non-leading zero month
				.replaceAll("MMMM", "F")
				.replaceAll("MMM", "M")
				.replaceAll("MM?", "m");
	}
	public String getDateFormat() {
		if (StringUtils.isBlank(overrideDateFormat)) {
			if (locale != null) {
				final DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
				if (format instanceof SimpleDateFormat) return ((SimpleDateFormat) format).toLocalizedPattern();
			} 
		}
		else {
			try {
				if (new SimpleDateFormat(overrideDateFormat) != null);
				return overrideDateFormat;
			}
			catch (IllegalArgumentException e){}
		}
		
		return "yyyy-MM-dd";
	}
	public String getOverrideDateFormat() {
		return overrideDateFormat;
	}
	public void setOverrideDateFormat(String overrideDateFormat) {
		this.overrideDateFormat = overrideDateFormat;
	}
	public Currency getCurrency() {
		return currency;
	}
	public void setCurrency(Currency currency) {
		this.currency = currency;
	}
	public String getCurrencySymbol(){
		return currency.getSymbol(locale);
	}
	public boolean isShowCleared() {
		return showCleared;
	}
	public void setShowCleared(boolean showCleared) {
		this.showCleared = showCleared;
	}
	public boolean isShowDeleted() {
		return showDeleted;
	}
	public void setShowDeleted(boolean showDeleted) {
		this.showDeleted = showDeleted;
	}
	public boolean isShowReconciled() {
		return showReconciled;
	}
	public void setShowReconciled(boolean showReconciled) {
		this.showReconciled = showReconciled;
	}
	public String getDecimalSeparator(){
		return ((DecimalFormat) NumberFormat.getInstance(getLocale())).getDecimalFormatSymbols().getDecimalSeparator() + "";
	}
	public String getThousandSeparator(){
		return ((DecimalFormat) NumberFormat.getInstance(getLocale())).getDecimalFormatSymbols().getGroupingSeparator() + "";
	}
}
