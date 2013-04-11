Ext.define('BuddiLive.view.login.CreateAccount', {
	"extend": "Ext.window.Window",
	"alias": "widget.createaccount",
	"requires": [
		"BuddiLive.view.login.ClickLabel",
		"BuddiLive.view.login.PasswordField",
		"BuddiLive.view.component.LocalesCombobox"
	],
		
	"initComponent": function(){
		this.title = "Buddi Live Create Account";	//TODO i18n
		this.renderTo = "form";
		this.layout = "fit";
		this.modal = true;
		this.width = 500;
		this.items = [
			{
				"items": [
					{
						"xtype": "form",
						"layout": "form",
						"bodyPadding": 5,
						"items": [
							{
								"xtype": "hidden",
								"itemId": "id",
								"hidden": true
							},
							{
								"xtype": "textfield",
								"itemId": "identifier",
								"fieldLabel": "${translation("USERNAME")?json_string}",
								"allowBlank": false,
								"emptyText": "user@example.com",
								"vtype": "email"
							},
							{
								"xtype": "passwordfield",
								"itemId": "password",
								"fieldLabel": "${translation("PASSWORD")?json_string}",
								"allowBlank": false
							},
							{
								"xtype": "localescombobox",
								"itemId": "locale",
								"fieldLabel": "${translation("LOCALE")?json_string}",
								"value": d.locale
							},
							{
								"xtype": "panel",
								"border": false,
								"layout": "hbox",
								"items": [
									{
										"xtype": "checkbox",
										"itemId": "email",
										"fieldLabel": " ",
										"labelSeparator": "",
										"boxLabel": "${translation("STORE_EMAIL")?json_string}"
									},
									{
										"xtype": "clicklabel",
										"flex": 1,
										"text": "${translation("WHAT_IS_THIS")?json_string}",
										"listeners": {
											"click": function(){
												Ext.MessageBox.show({
													"title": "${translation("STORE_EMAIL")?json_string}",
													"msg": "${translation("CREATE_USER_WHAT_IS_REMEMBER_EMAIL")?json_string}",
													"buttons": Ext.MessageBox.OK
												});
											}
										}
									}
								]
							},
							{
								"xtype": "panel",
								"border": false,
								"layout": "hbox",
								"items": [
									{
										"xtype": "checkbox",
										"itemId": "encrypt",
										"fieldLabel": " ",
										"labelSeparator": "",
										"boxLabel": "${translation("ENCRYPT_DATA")?json_string}"
									},
									{
										"xtype": "clicklabel",
										"flex": 1,
										"text": "${translation("WHAT_IS_THIS")?json_string}",
										"listeners": {
											"click": function(){
												Ext.MessageBox.show({
													"title": "${translation("ENCRYPT_DATA")?json_string}",
													"msg": "${translation("CREATE_USER_WHAT_IS_ENCRYPT_DATA")?json_string}",
													"buttons": Ext.MessageBox.OK
												});
											}
										}
									}
								]
							},
							{
								"xtype": "panel",
								"border": false,
								"layout": "hbox",
								"items": [
									{
										"xtype": "checkbox",
										"itemId": "agree",
										"fieldLabel": " ",
										"labelSeparator": "",
										"allowBlank": false
									},
									{
										"xtype": "clicklabel",
										"flex": 1,
										"style": "padding-left: 4px; padding-top: 3px;",
										"html": "${translation("AGREE_TERMS_AND_CONDITIONS")?json_string}",
										"listeners": {
											"click": function(){
												Ext.MessageBox.show({
													"title": "${translation("CREATE_USER_TERMS_AND_CONDITIONS_TITLE")?json_string}",
													"msg": "${translation("CREATE_USER_TERMS_AND_CONDITIONS")?json_string}",
													"buttons": Ext.MessageBox.OK
												});
											}
										}
									}
								]
							},
						
						]
					}
				],
				"buttons": [
					{
						"text": "${translation("OK")?json_string}",
						"itemId": "ok"
					},
					{
						"text": "${translation("CANCEL")?json_string}",
						"itemId": "cancel"
					}
				]
			}
		]

		this.callParent(arguments);
	}
});