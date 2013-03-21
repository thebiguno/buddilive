Ext.define('BuddiLive.view.login.Panel', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.loginpanel",
	"requires": [
		"BuddiLive.view.login.CreateAccount"
	],
		
	"initComponent": function(){
		var panel = this;
		
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
				"margin": "10 10 30 10",
				"defaults": {
					"anchor": "100%",
					"padding": 10,
					"listeners": {
						"specialkey": function(field, event) {
							if (event.getKey() == event.ENTER) {
								var login = panel.down("button[itemId='login']");
								login.fireEvent("click", login);
							}
						}
					}
				},
				"items": [
					{
						"xtype": "textfield",
						"allowBlank": false,
						"itemId": "identifier",
						"fieldLabel": "Username",	//TODO i18n
						"listeners": {
							"afterrender": function(component){
								component.focus();
							}
						}
					},
					{
						"xtype": "textfield",
						"inputType": "password", 
						"allowBlank": false,
						"itemId": "credentials",
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
						"itemId": "createAccount",
						"text": "Create New Account"	//TODO i18n
					},
					"->",
					{
						"xtype": "button",
						"itemId": "login",
						"text": "Login"		//TODO i18n
					}
				]
			}
		];

		this.callParent(arguments);
	}
});