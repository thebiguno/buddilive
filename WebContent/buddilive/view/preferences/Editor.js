Ext.define('BuddiLive.view.preferences.Editor', {
	"extend": "Ext.window.Window",
	"alias": "widget.preferenceseditor",
	"requires": [
		
	],
	
	"initComponent": function(){
		var d = this.initialConfig.data

		this.title = "${translation("PREFERENCES")?json_string}";
		this.layout = "fit";
		this.modal = true;
		this.width = 400;
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
						"boxLabel": "${translation("ENCRYPT_DATA")?json_string}"
					},
					{
						"xtype": "combobox",
						"itemId": "locale",
						"fieldLabel": "${translation("LANGUAGE")?json_string}",
						"editable": false,
						"value": d.locale,
						"forceSelection": true,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "${translation("USE_BROWSER_LOCALE_SETTINGS")?json_string}", "value": ""},
								{"text": "Deutsch", "value": "de"},
								{"text": "English", "value": "en"},
								{"text": "English (US)", "value": "en_US"},
								{"text": "Español", "value": "es"},
								{"text": "Español (Mexico)", "value": "es_MX"},
								{"text": "Francais", "value": "fr"},
								{"text": "Italiano", "value": "it"},
								{"text": "Nederlands", "value": "nl"},	//Dutch
								{"text": "Norsk", "value": "no"},	//Norwegian
								{"text": "Portugues", "value": "pt"},
								{"text": "Portugues (Brasil)", "value": "pt-BR"},
								{"text": "Svenska", "value": "sv"},	//Swedish
								{"text": "ελληνικά", "value": "el"},	//Greek
								{"text": "עִבְרִית", "value": "he"},	//Hebrew
								{"text": "ру́сский", "value": "ru"},	//Russian
								{"text": "српски", "value": "sr"}	//Serbian
							]
						}),
						"queryMode": "local",
						"valueField": "value"
					},
					{
						"xtype": "combobox",
						"itemId": "dateFormat",
						"fieldLabel": "${translation("DATE_FORMAT")?json_string}",
						"editable": false,
						"value": d.dateFormat,
						"forceSelection": true,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
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
						"xtype": "combobox",
						"itemId": "currencySymbol",
						"fieldLabel": "${translation("CURRENCY_FORMAT")?json_string}",
						"editable": true,
						"value": d.currencySymbol,
						"store": new Ext.data.Store({
							"fields": ["text"],
							"data": [
								{"text": "$"},
								{"text": "\u20ac"},		//Euro
								{"text": "\u00a3"},		//British Pounds
								{"text": "p."},			//Russian Ruble
								{"text": "\u00a5"},		//Yen
								{"text": "\u20a3"},		//French Franc
								{"text": "SFr"}, 		//Swiss Franc (?)
								{"text": "Rs"}, 		//Indian Rupees
								{"text": "Kr"}, 		//Norwegian
								{"text": "Bs"}, 		//Venezuela
								{"text": "S/."}, 		//Peru
								{"text": "\u20b1"},		//Peso
								{"text": "\u20aa"}, 	//Israel Sheqel 
								{"text": "Mex$"},		//Mexican Peso
								{"text": "R$"},			//Brazilian Real
								{"text": "Ch$"},		//Chilean Peso
								{"text": "C"},			//Costa Rican Colon
								{"text": "Arg$"},		//Argentinan Peso
								{"text": "Kc"}			//Something else; requested by a user
							]
						}),
						"valueField": "text"
					},
					{
						"xtype": "checkbox",
						"itemId": "currencySymbolAfterAmount",
						"fieldLabel": " ",
						"labelSeparator": "",
						"checked": d.currencySymbolAfterAmount,
						"boxLabel": "${translation("SHOW_CURRENCY_SYMBOL_AFTER_AMOUNT")?json_string}"
					},
					{
						"xtype": "checkbox",
						"itemId": "showDeleted",
						"fieldLabel": " ",
						"labelSeparator": "",
						"checked": d.showDeleted,
						"boxLabel": "${translation("SHOW_DELETED")?json_string}"
					},
					{
						"xtype": "checkbox",
						"itemId": "showCleared",
						"fieldLabel": " ",
						"labelSeparator": "",
						"checked": d.showCleared,
						"boxLabel": "${translation("SHOW_CLEARED")?json_string}"
					},
					{
						"xtype": "checkbox",
						"itemId": "showReconciled",
						"fieldLabel": " ",
						"labelSeparator": "",
						"checked": d.showReconciled,
						"boxLabel": "${translation("SHOW_RECONCILED")?json_string}"
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