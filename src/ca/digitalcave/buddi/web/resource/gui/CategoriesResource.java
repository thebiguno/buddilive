package ca.digitalcave.buddi.web.resource.gui;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.web.BuddiApplication;
import ca.digitalcave.buddi.web.db.Sources;
import ca.digitalcave.buddi.web.db.util.ConstraintsChecker;
import ca.digitalcave.buddi.web.db.util.DatabaseException;
import ca.digitalcave.buddi.web.model.Category;
import ca.digitalcave.buddi.web.model.CategoryPeriod;
import ca.digitalcave.buddi.web.model.CategoryPeriod.CategoryPeriods;
import ca.digitalcave.buddi.web.model.User;
import ca.digitalcave.buddi.web.util.FormatUtil;

public class CategoriesResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
//			final List<Category> categories = Category.getHierarchy(sqlSession.getMapper(Sources.class).selectCategories(user, ));
			CategoryPeriod cp = new CategoryPeriod(CategoryPeriods.valueOf(getQuery().getFirstValue("periodType")), new Date());
			final List<Category> categories = Category.getHierarchy(sqlSession.getMapper(Sources.class).selectCategories(user, cp));
			
			final JSONArray data = new JSONArray();
			for (Category c : categories) {
				data.put(getJsonObject(c));
			}
			
			final JSONObject result = new JSONObject();
			result.put("children", data);
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}
	
	private JSONObject getJsonObject(Category category) throws JSONException {
		final JSONObject result = new JSONObject();
		result.put("id", category.getId());
		result.put("name", category.getName());
		result.put("periodType", category.getPeriodType());
		result.put("type", category.getType());
		result.put("parent", category.getParent());
		result.put("deleted", category.isDeleted());
		final StringBuilder sb = new StringBuilder();
		if (category.isDeleted()) sb.append(" text-decoration: line-through;");
		if (!category.isIncome()) sb.append(" color: " + FormatUtil.HTML_RED + ";");
		result.put("style", sb.toString());
		sb.setLength(0);
		result.put("icon", "img/folder-open-table.png");
		if (category.getChildren() != null){
			result.put("expanded", true);
			for (Category child : category.getChildren()) {
				result.accumulate("children", getJsonObject(child));
			}
		}
		else {
			result.put("leaf", true);
		}
		return result;
	}

	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		final User user = (User) getRequest().getClientInfo().getUser();
		try {
			final JSONObject request = new JSONObject(entity.getText());
			final String action = request.optString("action");
			
			final Category category = new Category(request);
			
			if ("insert".equals(action)){
				ConstraintsChecker.checkInsertCategory(category, user, sqlSession);
				int count = sqlSession.getMapper(Sources.class).insertCategory(user, category);
				if (count != 1) throw new DatabaseException(String.format("Insert failed; expected 1 row, returned %s", count));
			} 
			else if ("delete".equals(action) || "undelete".equals(action)){
				category.setDeleted("delete".equals(action));
				int count = sqlSession.getMapper(Sources.class).updateSourceDeleted(user, category);
				if (count != 1) throw new DatabaseException(String.format("Delete / undelete failed; expected 1 row, returned %s", count));
			}
			else if ("update".equals(action)){
				ConstraintsChecker.checkUpdateCategory(category, user, sqlSession);
				int count = sqlSession.getMapper(Sources.class).updateCategory(user, category);
				if (count != 1) throw new DatabaseException(String.format("Update failed; expected 1 row, returned %s", count));
			}
			else {
				throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "An action parameter must be specified.");
			}
			
			sqlSession.commit();
			final JSONObject result = new JSONObject();
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (DatabaseException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (IOException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
