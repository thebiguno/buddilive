package ca.digitalcave.buddi.web.resource.data;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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
import ca.digitalcave.buddi.web.model.Source;
import ca.digitalcave.buddi.web.security.BuddiUser;
import ca.digitalcave.buddi.web.util.FormatUtil;

public class SourcesDataResource extends ServerResource {

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
			final List<Source> sources = sqlSession.getMapper(Sources.class).selectSource(user.getId(), (Long) null);
			final JSONArray result = new JSONArray();
			for (Source source : sources) {
				final JSONObject item = new JSONObject();
				item.put("id", source.getId());
				item.put("userId", source.getUserId());
				item.put("uuid", source.getUuid());
				item.put("name", source.getName());
				item.put("startDate", FormatUtil.formatDateTime((Date) source.getStartDate()));
				item.put("deleted", source.isDeleted());
				item.put("type", source.getType());
				item.put("created", FormatUtil.formatDateTime((Date) source.getCreated()));
				item.put("modified", FormatUtil.formatDateTime((Date) source.getModified()));
				item.put("startBalance", source.getStartBalance());
				item.put("periodType", source.getPeriodType());
				item.put("parent", source.getParent());
				result.put(item);
			}
			return new JsonRepresentation(result);
		}
		catch (JSONException e){
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
		}
		finally {
			sqlSession.close();
		}
	}

	@Override
	protected Representation post(Representation entity, Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession();
		final BuddiUser user = (BuddiUser) getRequest().getClientInfo().getUser();
		try {
			final JSONArray request = new JSONArray(entity.getText());
			for (int i = 0; i < request.length(); i++) {
				final JSONObject source = request.getJSONObject(i);

				final Source value = new Source();
				value.setUserId(user.getId());
				value.setUuid(source.has("uuid") ? source.getString("uuid") : UUID.randomUUID().toString());
				value.setName(source.getString("name"));
				value.setStartDate(source.has("startDate") ? FormatUtil.parseDate(source.getString("startDate")) : FormatUtil.parseDate("1900-01-01"));
				value.setDeleted(source.has("delete") ? source.getBoolean("deleted") : false);
				value.setType(source.getString("type"));
				value.setStartBalance(source.has("startBalance") ? source.getLong("startBalance") : null);
				value.setPeriodType(source.has("periodType") ? source.getString("periodType") : null);
				value.setParent(source.has("parent") ? source.getInt("parent") : null);

				final Integer count = sqlSession.getMapper(Sources.class).insertSource(value);
				if (count == 1){
					sqlSession.commit();
				}
				else {
					throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
				}
			}
			
			final JSONObject result = new JSONObject();
			result.put("success", true);
			return new JsonRepresentation(result);
		}
		catch (IOException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		catch (JSONException e){
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		}
		finally {
			sqlSession.close();
		}
	}
}
