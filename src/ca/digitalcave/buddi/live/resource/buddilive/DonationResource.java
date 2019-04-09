package ca.digitalcave.buddi.live.resource.buddilive;

import java.io.IOException;
import java.util.Properties;

import org.apache.ibatis.session.SqlSession;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.xml.sax.SAXException;

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
		
			final Properties config = application.getConfigProperties();
			final Runnable emailRunnable = new Runnable() { 
				public void run() { 
					final String url = "smtp://" + config.getProperty("mail.smtp.host", "localhost") + ":" + config.getProperty("mail.smtp.port", "25");
					final Request request = new Request(Method.POST, url);
					final String fromEmail = config.getProperty("mail.smtp.from", "user@localhost");
					final SaxRepresentation entity = new SaxRepresentation() {
						@Override
						public void write(XmlWriter w) throws IOException {
							try {
								w.startDocument();
								w.startElement("email");
								w.startElement("head");
								w.dataElement("subject", "BuddiLive Donation");
								w.dataElement("from", fromEmail);
								w.dataElement("to", "buddilivedonation@digitalcave.ca");
								w.endElement("head");
								w.startElement("body");
								if (user == null){
									w.characters("A donation was sent by an unknown user.");
								}
								else {
									w.characters("A donation was sent by user ID " + user.getId() + (user.getEmail() != null ? " (email: " + user.getEmail() + ")" : "") + ".");
									if (validKey){
										w.characters("\nThe user has been upgraded to Premium access.");
									}
									else {
										w.characters("\nThe key was invalid, and the user has not been upgraded to Premium access.");
									}
								}
								w.endElement("body");
								w.endElement("email");
								w.endDocument();
							} catch (SAXException e) {
								throw new IOException(e);
							}
						}
					};
					entity.setCharacterSet(CharacterSet.ISO_8859_1);
					request.setEntity(entity);
					if ("true".equals(config.getProperty("mail.smtp.auth", "false"))) {
						final ChallengeResponse cr = new ChallengeResponse(ChallengeScheme.SMTP_PLAIN, config.getProperty("mail.smtp.username"), config.getProperty("mail.smtp.password"));
						request.setChallengeResponse(cr);
					}

					final Client client = new Client(getContext().createChildContext(), Protocol.SMTP);
					client.getContext().getParameters().set("startTls", config.getProperty("mail.smtp.starttls.enable", "false"));
					client.handle(request);
				}
			};
			final Thread emailThread = new Thread(emailRunnable, "Email");
			emailThread.setDaemon(false);
			emailThread.start();
			
			final Reference newRef = new Reference(getRootRef().toString() + "/doc/donation-thanks.html");
			redirectSeeOther(newRef);
			return new EmptyRepresentation();
		}
		finally {
			sqlSession.close();
		}
	}
}
