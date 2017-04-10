Ext.define('BuddiLive.view.login.CreateAccount', {
	"extend": "Ext.window.Window",
	"alias": "widget.createaccount",
	"requires": [
		"BuddiLive.view.login.ClickLabel",
		"BuddiLive.view.login.PasswordField",
		"BuddiLive.view.component.SelfDocumentingField",
		"BuddiLive.view.component.CurrenciesCombobox",
		"BuddiLive.view.component.LocalesCombobox"
	],
		
	"initComponent": function(){
		this.title = "${translation("CREATE_ACCOUNT")?json_string}";
		this.renderTo = "form";
		this.layout = "fit";
		this.modal = true;
		this.width = 500;
		this.items = [
			{
				"items": [
					{
						"xtype": "form",
						"layout": "anchor",
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
								"xtype": "selfdocumentingfield",
								"messageBody": "${translation("CREATE_USER_AGREEMENT_REQUIRED")?json_string}",
								"type": "checkbox",
								"boxLabel": "${translation("AGREE_TERMS_AND_CONDITIONS")?json_string}",
								"itemId": "agree",
								"fieldLabel": " ",
								"labelSeparator": "",
								"allowBlank": false
							}
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