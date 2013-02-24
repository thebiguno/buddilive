package ca.digitalcave.buddi.web.security;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.security.ChallengeAuthenticator;

import ca.digitalcave.buddi.web.BuddiApplication;

public class BuddiAuthenticator extends ChallengeAuthenticator {

	private BuddiAuthenticator(BuddiApplication application, Context context, boolean optional) {
		super(context, optional, ChallengeScheme.CUSTOM, "Buddi Live", new BuddiVerifier(application));
	}	
	
	public BuddiAuthenticator(BuddiApplication application, Context context, boolean optional, Class<?> next) {
		this(application, context, optional);
		this.setNext(next);
	}
	public BuddiAuthenticator(BuddiApplication application, Context context, boolean optional, Restlet next) {
		this(application, context, optional);
		this.setNext(next);
	}
}
