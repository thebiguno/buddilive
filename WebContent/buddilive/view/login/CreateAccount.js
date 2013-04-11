Ext.define('BuddiLive.view.login.CreateAccount', {
	"extend": "Ext.window.Window",
	"alias": "widget.createaccount",
	"requires": [
		"BuddiLive.view.login.ClickLabel",
		"BuddiLive.view.login.PasswordField",
		"BuddiLive.view.component.SelfDocumentingField",
		"BuddiLive.view.component.CurrenciesCombobox",
		"BuddiLive.view.component.LocalesCombobox",
		"BuddiLive.view.component.TimezonesCombobox"
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
								"xtype": "selfdocumentingfield",
								"messageBody": "${translation("HELP_LOCALE")?json_string}",
								"type": "localescombobox",
								"itemId": "locale",
								"fieldLabel": "${translation("LOCALE")?json_string}",
								"value": "en_US"
							},
							{
								"xtype": "selfdocumentingfield",
								"messageBody": "${translation("HELP_TIMEZONE")?json_string}",
								"type": "timezonescombobox",
								"itemId": "timezone",
								"fieldLabel": "${translation("TIMEZONE")?json_string}",
								"value": "America/Boise"
							},
							{
								"xtype": "selfdocumentingfield",
								"messageBody": "${translation("HELP_CURRENCY")?json_string}",
								"type": "currenciescombobox",
								"itemId": "currency",
								"fieldLabel": "${translation("CURRENCY")?json_string}",
								"value": "USD"
							},
							{
								"xtype": "selfdocumentingfield",
								"messageBody": "${translation("HELP_STORE_EMAIL")?json_string}",
								"type": "checkbox",
								"itemId": "email",
								"fieldLabel": " ",
								"labelSeparator": "",
								"boxLabel": "${translation("STORE_EMAIL")?json_string}"
							},
							{
								"xtype": "selfdocumentingfield",
								"messageBody": "${translation("HELP_ENCRYPT_DATA")?json_string}",
								"type": "checkbox",
								"itemId": "encrypt",
								"fieldLabel": " ",
								"labelSeparator": "",
								"boxLabel": "${translation("ENCRYPT_DATA")?json_string}"
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