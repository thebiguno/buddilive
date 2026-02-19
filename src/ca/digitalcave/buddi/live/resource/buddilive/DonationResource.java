package ca.digitalcave.buddi.live.resource.buddilive;

import org.apache.ibatis.session.SqlSession;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import ca.digitalcave.buddi.live.BuddiApplication;
import ca.digitalcave.buddi.live.db.Users;
import ca.digitalcave.buddi.live.model.User;

public class DonationResource extends ServerResource {

	@Override
	protected void doInit() throws ResourceException {
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}

	@Override
	protected Representation get(Variant variant) throws ResourceException {
		final BuddiApplication application = (BuddiApplication) getApplication();
		final SqlSession sqlSession = application.getSqlSessionFactory().openSession(true);
		final User user = (User) getRequest().getClientInfo().getUser();
		//Yep, you have just found my super secure method of determining if someone has actually sent the donation.  Now you, yes YOU,
		// can upgrade yourself to premium access without paying a cent!  There is nothing better in life than ripping off the little guy!
		final boolean validKey = "e0b994b8-e939-49d8-b243-cdcd0ec7fa03".equals(getQueryValue("key"));
		try {
			if (validKey && user != null){
				sqlSession.getMapper(Users.class).updateUserPremium(user, "Y");
			}
		
			final StringBuilder body = new StringBuilder();
			if (user == null){
				body.append("A donation was sent by an unknown user.");
			}
			else {
				body.append("A donation was sent by user ID " + user.getId() + (user.getEmail() != null ? " (email: " + user.getEmail() + ")" : "") + ".");
				if (validKey){
					body.append("\nThe user has been upgraded to Premium access.");
				}
				else {
					body.append("\nThe key was invalid, and the user has not been upgraded to Premium access.");
				}
			}

			application.getAuthenticationHelper().sendEmail("buddilivedonation@digitalcave.ca", "BuddiLive Donation", body.toString());
			
			final Reference newRef = new Reference(getRootRef().toString() + "/doc/donation-thanks.html");
			redirectSeeOther(newRef);
			return new EmptyRepresentation();
		}
		finally {
			sqlSession.close();
		}
	}
}
