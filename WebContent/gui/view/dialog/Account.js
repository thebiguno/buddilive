Ext.define('BuddiLive.view.dialog.Account', {
	"extend": "Ext.window.Window",
	"alias": "widget.buddiaccount",
	"requires": [
		
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected

		this.title = (s ? "Edit Account" : "Add Account");
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
						"xtype": "hidden",
						"itemId": "id",
						"value": (s ? s.id : null)
					},
					{
						"xtype": "textfield",
						"itemId": "name",
						"value": (s ? s.name : null),
						"fieldLabel": "Name",
						"allowBlank": false,
						"emptyText": "TD Savings, Visa Gold, Cash in Wallet, etc"
					},
					{
						"xtype": "textfield",
						"itemId": "accountType",
						"value": (s ? s.accountType : null),
						"fieldLabel": "Account Type",
						"allowBlank": false,
						"emptyText": "Credit Card, Savings, etc"
					},
					{
						"xtype": "combobox",
						"itemId": "type",
						"value": (s ? s.type : "D"),
						"fieldLabel": "Type",
						"editable": false,
						"allowBlank": false,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "Debit", "value": "D"},
								{"text": "Credit", "value": "C"}
							]
						}),
						"queryMode": "local",
						"valueField": "value"
					},
					{
						"xtype": "numberfield",
						"itemId": "startBalance",
						"value": (s ? s.startBalance : null),
						"fieldLabel": "Starting Balance",
						"hideTrigger": true,
						"keyNavEnabled": false,
						"mouseWheelEnabled": false,
						"emptyText": "0.00"
					}
				
				]
			}
		];
		this.buttons = [
			{
				"text": "OK",
				"itemId": "ok",
				"disabled": true
			},
			{
				"text": "Cancel",
				"itemId": "cancel"
			}
		]
	
		this.callParent(arguments);
	}
});