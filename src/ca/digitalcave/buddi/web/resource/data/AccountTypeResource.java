package ca.digitalcave.buddi.web.resource.data;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.AccountTypeMap;
import ca.digitalcave.buddi.web.model.AccountType;
import ca.digitalcave.buddi.web.security.BuddiUser;

public class AccountTypeResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}
	
	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
		final BuddiUser user = (BuddiUser) getRequest().getClientInfo().getUser();
		try {
			final List<AccountType> accountTypes = sqlSession.getMapper(AccountTypeMap.class).selectAccountTypes(user.getIdentifier());
			final JSONObject result = new JSONObject();
			for (AccountType accountType : accountTypes) {
				result.accumulate("data", accountType.toJson());
			}
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
		return null;
	}
}
