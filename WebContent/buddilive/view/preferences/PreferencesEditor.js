Ext.define('BuddiLive.view.preferences.PreferencesEditor', {
	"extend": "Ext.window.Window",
	"alias": "widget.preferenceseditor",
	"requires": [
		"BuddiLive.view.component.CurrenciesCombobox",
		"BuddiLive.view.component.LocalesCombobox"
	],
	
	"initComponent": function(){
		var d = this.initialConfig.data

		this.title = "${translation("PREFERENCES")?json_string}";
		this.layout = "fit";
		this.modal = true;
		this.width = 500;
		this.items = [
			{
				"xtype": "form",
				"layout": "form",
				"bodyPadding": 5,
				"items": [
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_ENCRYPT_DATA")?json_string}",
						"type": "checkbox",
						"itemId": "encrypt",
						"fieldLabel": " ",
						"labelSeparator": "",
						"checked": d.encrypt,
						"boxLabel": "${translation("ENCRYPT_DATA")?json_string}",
						"listeners": {
							"change": function(checkbox){
								checkbox.up("form").down("textfield[itemId='password']").setVisible(d.encrypt != checkbox.getValue());
								checkbox.up("form").down("textfield[itemId='password']").focus(true);
							}
						}
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_ENCRYPT_DATA_PASSWORD")?json_string}",
						"type": "textfield",
						"inputType": "password", 
						"allowBlank": false,
						"itemId": "password",
						"hidden": true,
						"fieldLabel": "${translation("PASSWORD")?json_string}"
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_STORE_EMAIL")?json_string}",
						"type": "checkbox",
						"itemId": "storeEmail",
						"fieldLabel": " ",
						"labelSeparator": "",
						"checked": d.storeEmail,
						"boxLabel": "${translation("STORE_EMAIL")?json_string}"
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_LOCALE")?json_string}",
						"type": "localescombobox",
						"itemId": "locale",
						"fieldLabel": "${translation("LOCALE")?json_string}",
						"value": d.locale
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_CURRENCY")?json_string}",
						"type": "currenciescombobox",
						"itemId": "currency",
						"fieldLabel": "${translation("CURRENCY")?json_string}",
						"value": d.currency
					},
 					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_DATE_FORMAT")?json_string}",
						"type": "combobox",
						"itemId": "dateFormat",
						"fieldLabel": "${translation("DATE_FORMAT")?json_string}",
						"editable": false,
						"value": d.dateFormat && d.dateFormat.length > 0 ? d.dateFormat : "",
						"forceSelection": true,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "${translation("USE_LOCALE_DEFAULTS")?json_string}", "value": ""},
								{"text": "${.now?string("yyyy-MM-dd")}", "value": "yyyy-MM-dd"},
								{"text": "${.now?string("MM/dd/yyyy")}", "value": "MM/dd/yyyy"},
								{"text": "${.now?string("dd/MM/yyyy")}", "value": "dd/MM/yyyy"},
								{"text": "${.now?string("MMM dd, yyyy")}", "value": "MMM dd, yyyy"},
								{"text": "${.now?string("MMMM dd, yyyy")}", "value": "MMMM dd, yyyy"}
							]
						}),
						"queryMode": "local",
						"valueField": "value"
 					},
					{
						"xtype": "checkbox",
						"itemId": "showDeleted",
						"fieldLabel": " ",
						"labelSeparator": "",
						"checked": d.showDeleted,
						"boxLabel": "${translation("SHOW_DELETED")?json_string}"
					}
				]
			}
		];
		this.buttons = [
			{
				"text": "${translation("OK")?json_string}",
				"itemId": "ok"
			},
			{
				"text": "${translation("CANCEL")?json_string}",
				"itemId": "cancel"
			}
		]
	
		this.callParent(arguments);
	}
});