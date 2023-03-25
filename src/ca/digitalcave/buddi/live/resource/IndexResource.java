package ca.digitalcave.buddi.live.resource;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Sources;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.db.util.DataUpdater;
import ca.digitalcave.buddi.live.db.util.DatabaseException;
import ca.digitalcave.buddi.live.model.Account;
import ca.digitalcave.buddi.live.model.User;
import ca.digitalcave.buddi.live.util.LocaleUtil;
import ca.digitalcave.moss.crypto.Crypto.CryptoException;

public class IndexResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.TEXT_HTML));
	}
	
	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final HashMap<String, Object> dataModel = new HashMap<String, Object>();
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		try {
			final User user = (User) getRequest().getClientInfo().getUser();
			if (user != null){
				//Check user's encryption level, and upgrade as needed
				final int encryptionVersion = sqlSession.getMapper(Users.class).selectEncryptionVersion(user);

				if (encryptionVersion == 1){
					//Upgrade from 1 to latest
					DataUpdater.upgradeEncryptionFrom1(user, sqlSession);
				}

				final List<Account> accounts = sqlSession.getMapper(Sources.class).selectAccounts(user);
				if (accounts.size() == 0) dataModel.put("newUser", "true");

				//Update the user's last login date
				sqlSession.getMapper(Users.class).updateUserLoginTime(user);

				sqlSession.commit();
			}
		} catch (CryptoException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} catch (DatabaseException e) {
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		} finally {
			sqlSession.close();
		}
		
		final User user = (User) getClientInfo().getUser();
		if (getClientInfo().getUser() != null && (!user.isTwoFactorRequired() || user.getTwoFactorBackupCodes().size() > 0)) {
			dataModel.put("user", getClientInfo().getUser());
		}
		dataModel.put("requestAttributes", getRequestAttributes());
		dataModel.put("systemProperties", ((BuddiApplication) getApplication()).getSystemProperties());
		dataModel.put("translation", LocaleUtil.getTranslation(getRequest()));

		return new TemplateRepresentation("/index.html", ((BuddiApplication) getApplication()).getFreemarkerConfiguration(), dataModel, variant.getMediaType());
	}
}
