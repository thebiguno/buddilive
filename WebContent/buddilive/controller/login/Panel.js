Ext.define("BuddiLive.controller.login.Panel", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"loginpanel button[itemId='createAccount']": {"click": this.createAccount},
			"loginpanel button[itemId='login']": {"click": this.login}
		});
	},
	
	"createAccount": function(component){
		Ext.widget("createaccount").show();
	},
	
	"login": function(button){
		var window = button.up("window");
		var form = window.down("form");
		if (form.getForm().isValid() == false){
			Ext.MessageBox.show({
				"title": "${translation("CREATE_USER_NOT_VALID_TITLE")?json_string}",
				"msg": "${translation("CREATE_USER_NOT_VALID")?json_string}",
				"buttons": Ext.MessageBox.OK
			});
			return;
		}
		if (form.down("checkbox[itemId='agree']").getValue() == false){
			Ext.MessageBox.show({
				"title": "${translation("CREATE_USER_AGREEMENT_REQUIRED_TITLE")?json_string}",
				"msg": "${translation("CREATE_USER_AGREEMENT_REQUIRED")?json_string}",
				"buttons": Ext.MessageBox.OK
			});
			return;
		}
		var request = {"action": "insert"};
		request.identifier = form.down("textfield[itemId='identifier']").getValue();
		request.credentials = form.down("textfield[itemId='password']").getValue();
		request.email = form.down("textfield[itemId='email']").getValue();
		request.locale = form.down("textfield[itemId='locale']").getValue();
		var conn = new Ext.data.Connection();
		conn.request({
			"url": "data/users",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				window.close();
			},
			"failure": function(response){
				var title = (response.statusText ? response.statusText : "Error");	//TODO i18n
				var message = "Unknown error";	//TODO i18n
				var json = Ext.decode(response.responseText, true);
				if (json == null){
					message = error.responseText;
				}
				else {
					message = json.msg;
				}
				Ext.MessageBox.show({
					"title": title,
					"msg": message,
					"buttons": Ext.MessageBox.OK
				});
			}
		});
	}
});