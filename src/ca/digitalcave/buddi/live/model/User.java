package ca.digitalcave.buddi.live.model;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import javax.crypto.SecretKey;

import org.apache.commons.lang3.StringUtils;

import ca.digitalcave.moss.crypto.Crypto;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;
import ca.digitalcave.moss.restlet.model.AuthUser;

public class User extends AuthUser {
	private static final long serialVersionUID = 1L;
	
	private String plaintextIdentifier;	//Not persisted, injected by BuddiVerifier
	private String plaintextSecret;	//Not persisted, injected by BuddiVerifier
	private String encryptionKey;
//	private String decryptedEncryptionKey;	//Not persisted, injected by BuddiVerifier; deprecated.  Once all users are off of encryption version 1, we can delete this.
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

	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public void setSecretString(String secret) {
		setSecret(secret == null ? null : secret.toCharArray());
	}
	public String getSecretString() {
		return getSecret() == null ? null : new String(getSecret());
	}
	public String getPlaintextIdentifier() {
		return plaintextIdentifier;
	}
	public void setPlaintextIdentifier(String plaintextIdentifier) {
		this.plaintextIdentifier = plaintextIdentifier;
	}
	public String getPlaintextSecret() {
		return plaintextSecret;
	}
	public void setPlaintextSecret(String plaintextSecret) {
		this.plaintextSecret = plaintextSecret;
	}
	public String getEncryptionKey() {
		return encryptionKey;
	}
	public void setEncryptionKey(String encryptionKey) {
		this.encryptionKey = encryptionKey;
//		decryptedEncryptionKey = null;
		decryptedSecretKey = null;
	}
//	public String getDecryptedEncryptionKey() throws CryptoException {
//		if (decryptedEncryptionKey == null && isEncrypted()) {
//			decryptedEncryptionKey = Crypto.decrypt(plaintextSecret, encryptionKey);
//		}
//		return decryptedEncryptionKey;
//	}
	public SecretKey getDecryptedSecretKey() throws CryptoException {
		if (decryptedSecretKey == null && isEncrypted()){
			decryptedSecretKey = Crypto.recoverSecretKey(Crypto.decrypt(plaintextSecret, encryptionKey));
		}
		return decryptedSecretKey;
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
		final String normalizedOverride = normalizeSupportedDateFormat(overrideDateFormat);
		if (StringUtils.isNotBlank(normalizedOverride)) return normalizedOverride;

		if (locale != null) {
			final DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			if (format instanceof SimpleDateFormat) {
				final String normalizedLocale = normalizeSupportedDateFormat(((SimpleDateFormat) format).toPattern());
				if (StringUtils.isNotBlank(normalizedLocale)) return normalizedLocale;
			}
		}

		return "yyyy-MM-dd";
	}
	public String getOverrideDateFormat() {
		return overrideDateFormat;
	}
	public void setOverrideDateFormat(String overrideDateFormat) {
		this.overrideDateFormat = normalizeSupportedDateFormat(overrideDateFormat);
	}

	public static String normalizeSupportedDateFormat(String rawFormat) {
		final String value = StringUtils.trimToEmpty(rawFormat);
		if (StringUtils.isBlank(value)) return null;

		final String pattern = value
				.replace('Y', 'y')
				.replace('D', 'd')
				.replace('M', 'm')
				.trim()
				.toLowerCase(Locale.ROOT);

		if (pattern.matches("^y{1,4}[-./]m{1,4}[-./]d{1,4}$")) return "yyyy-MM-dd";
		if (pattern.matches("^m{1,4}[-./]d{1,4}[-./]y{1,4}$")) return "MM/dd/yyyy";
		if (pattern.matches("^d{1,4}[.\\-/]m{1,4}[.\\-/]y{1,4}$")) {
			return pattern.indexOf('.') >= 0 ? "dd.MM.yyyy" : "dd/MM/yyyy";
		}

		return null;
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
