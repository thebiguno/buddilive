Ext.define('BuddiLive.view.login.Panel', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.loginpanel",
	"requires": [
		"BuddiLive.view.login.CreateAccount"
	],
		
	"initComponent": function(){
		this.title = "Buddi Live Login";	//TODO i18n
		this.renderTo = "form";
		this.layout = "fit";
		this.items = [
			{
				"xtype": "label",
				"padding": 10,
				"text": "Welcome to Buddi Live.  Please log in, or sign up for an account."
			},
			{
				"xtype": "panel",
				"layout": "form",
				"border": false,
				"margin": 10,
				"defaults": {
					"anchor": "100%",
					"padding": 10,
					"listeners": {
						"specialkey": function(field, event) {
							if (event.getKey() == event.ENTER) {
								panel.down("form").getForm().submit();
							}
						}
					}
				},
				"items": [
					{
						"xtype": "textfield",
						"name": "user",
						"allowBlank": false,
						"itemId": "username",
						"fieldLabel": "Username"	//TODO i18n
					},
					{
						"xtype": "textfield",
						"name": "password",
						"inputType": "password", 
						"allowBlank": false,
						"itemId": "password",
						"fieldLabel": "Password"	//TODO i18n
					}
				]
			}
		];
		this.dockedItems = [
			{
				"xtype": "toolbar",
				"dock": "bottom",
				"items": [
					{
						"xtype": "button",
						"text": "Create New Account",	//TODO i18n
						"itemId": "createAccount"
					},
					"->",
					{
						"xtype": "button",
						"formbind": true,
						"text": "Login"		//TODO i18n
					}
				]
			}
		];

		this.callParent(arguments);
	}
});