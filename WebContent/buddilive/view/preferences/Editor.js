Ext.define('BuddiLive.view.preferences.Editor', {
	"extend": "Ext.window.Window",
	"alias": "widget.preferenceseditor",
	"requires": [
		"BuddiLive.view.component.CurrenciesCombobox",
		"BuddiLive.view.component.LocalesCombobox",
		"BuddiLive.view.component.TimezonesCombobox"
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
						"xtype": "checkbox",
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
						"xtype": "textfield",
						"inputType": "password", 
						"allowBlank": false,
						"itemId": "password",
						"hidden": true,
						"fieldLabel": "Password"	//TODO i18n
					},
					{
						"xtype": "localescombobox",
						"itemId": "locale",
						"fieldLabel": "${translation("LOCALE")?json_string}",
						"value": d.locale
					},
					{
						"xtype": "timezonescombobox",
						"itemId": "timezone",
						"fieldLabel": "${translation("TIMEZONE")?json_string}",
						"value": d.timezone
					},
					{
						"xtype": "currenciescombobox",
						"itemId": "currency",
						"fieldLabel": "${translation("CURRENCY")?json_string}",
						"value": d.currency
					},
 					{
						"xtype": "combobox",
						"itemId": "dateFormat",
						"fieldLabel": "${translation("DATE_FORMAT")?json_string}",
						"editable": false,
						"value": d.dateFormat && d.dateFormat.length > 0 ? d.dateFormat : "",
						"forceSelection": true,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "${translation("USE_LOCALE_DEFAULTS")?json_string}", "value": ""},
								{"text": "${today?string("yyyy-MM-dd")}", "value": "yyyy-MM-dd"},
								{"text": "${today?string("MM/dd/yyyy")}", "value": "MM/dd/yyyy"},
								{"text": "${today?string("dd/MM/yyyy")}", "value": "dd/MM/yyyy"},
								{"text": "${today?string("MMM dd, yyyy")}", "value": "MMM dd, yyyy"},
								{"text": "${today?string("MMMM dd, yyyy")}", "value": "MMMM dd, yyyy"}
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