package ca.digitalcave.buddi.live.security;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.routing.Filter;

public class AddressFilter extends Filter {

	public AddressFilter(Context context) {
		super(context);
	}

	public AddressFilter(Context context, Restlet next) {
		super(context, next);
	}
	
	@Override
	protected int beforeHandle(Request request, Response response) {
		final Form form = (Form) request.getAttributes().get("org.restlet.http.headers");
		final String[] values = form.getValuesArray("X-Forwarded-For");
		if (values != null) {
			for (String address : values) {
				request.getClientInfo().setAddress(address);
			}
		}
		
		return CONTINUE;
	}
}
