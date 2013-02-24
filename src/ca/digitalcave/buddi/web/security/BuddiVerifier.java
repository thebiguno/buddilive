package ca.digitalcave.buddi.web.security;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.engine.util.Base64;
import org.restlet.security.Verifier;

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.util.CryptoUtil;

public class BuddiVerifier implements Verifier {
	
	public final static String COOKIE_NAME = "buddi";
	
	private BuddiApplication application;
	
	public BuddiVerifier(BuddiApplication application) {
		this.application = application;
	}
	
	protected BuddiApplication getApplication() {
		return application;
	}
	
	public int verify(Request request, Response response) {
		final Cookie cookieUser = request.getCookies().getFirst(COOKIE_NAME);
		if (cookieUser != null){
			try {
				final JSONObject token = new JSONObject(new String(CryptoUtil.decrypt(Base64.decode(cookieUser.getValue()))));
				
				if (token.has("clientIp")){
					if (!token.get("clientIp").equals(request.getClientInfo().getAddress())){
						throw new Exception("Stored IP address does not match client IP; denying login");
					}
				}
				
				Date expiry = null;
				try {
					expiry = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(token.getString("expiry"));
				}
				catch (Throwable e){}
				
				if (expiry == null || expiry.after(new Date())) {
					request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.CUSTOM, token.getString("identifier"), token.getString("password")));
				}
			}
			catch (Throwable t){}
		}
		
		if (request.getChallengeResponse() == null) {
			return RESULT_MISSING;
		} else {
			//if (true) return RESULT_INVALID;
			final String identifier = request.getChallengeResponse().getIdentifier();
			final char[] secret = request.getChallengeResponse().getSecret();
			
			DirContext ctx = null;
			try {
				final Hashtable<String, String> properties = new Hashtable<String, String>();
				properties.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
				properties.put(Context.PROVIDER_URL, "ldaps://rsgmail.richer.ca:636");
				properties.put(Context.SECURITY_AUTHENTICATION, "simple");
				properties.put(Context.SECURITY_PRINCIPAL, "uid=diradmin, cn=users, dc=rsgmail, dc=richer, dc=ca");
				properties.put(Context.SECURITY_CREDENTIALS, "flyby2?pares");
				ctx = new InitialDirContext(properties);
				final SearchControls searchControls = new SearchControls();
				searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				searchControls.setReturningAttributes(new String[] { "cn", "givenName", "sn", "businessCategory", "telephoneNumber", "o", "mail" });
				final String base = "dc=rsgmail,dc=richer,dc=ca";
				final String filter = String.format("(|(uid=%s)(mail=%s))", identifier, identifier);
				final NamingEnumeration<SearchResult> search = ctx.search(base, filter, searchControls);
				final SearchResult searchResult = search.nextElement();
				final String dn = searchResult.getNameInNamespace();
				final Attributes atts = searchResult.getAttributes();
				
				// test the supplied password!
				@SuppressWarnings("unchecked")
				final Hashtable<String, String> environment = (Hashtable<String, String>) ctx.getEnvironment().clone();
				environment.put(Context.SECURITY_PRINCIPAL, dn);
				environment.put(Context.SECURITY_CREDENTIALS, new String(secret));
				new InitialDirContext(environment);
				
				final BuddiUser user = new BuddiUser(identifier, secret);
				request.getClientInfo().setUser(user);
				user.setCommonName(((String) atts.get("cn").get()).trim());
				user.setFirstName(((String) atts.get("givenname").get()).trim());
				user.setLastName(((String) atts.get("sn").get()).trim());
				user.setEmail(((String) atts.get("mail").get()).trim());
				
				if (dn.endsWith("cn=users,dc=rsgmail,dc=richer,dc=ca")) {
					user.setClient("RSG");
				} else {
					try { user.setClient(((String) atts.get("businessCategory").get()).trim()); } catch (Exception e) {}
				}
				try { user.setTelephone(((String) atts.get("telephoneNumber").get()).trim()); } catch (Exception e) {}
				try { user.setCompanyName(((String) atts.get("o").get()).trim()); } catch (Exception e) {}
				
				if (StringUtils.isBlank(user.getClient())) return RESULT_INVALID;
				
				return RESULT_VALID;
			} catch (Throwable e) {
				e.printStackTrace();
				return RESULT_INVALID;
			} finally {
				try { ctx.close(); } catch (Throwable t) {};
			}
		}
	}
}
