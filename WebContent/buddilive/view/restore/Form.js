Ext.define('BuddiLive.view.restore.Form', {
	"extend": "Ext.window.Window",
	"alias": "widget.restoreform",
	"requires": [
	],
	
	"initComponent": function(){
		this.title = "${translation("RESTORE")?json_string}",
		this.layout = "fit";
		this.modal = true;
		this.width = 400;
		this.items = [
			{
				"xtype": "form",
				"layout": "anchor",
				"bodyPadding": 5,
				"items": [
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_RESTORE_FILE")?json_string}",
						"type": "filefield",
						"fieldLabel": "${translation("RESTORE_FILE")?json_string}",
						"allowBlank": false,
						"name": "file"
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_DELETE_DATA")?json_string}",
						"type": "checkbox",
						"itemId": "deleteData",
						"fieldLabel": " ",
						"labelSeparator": "",
						"boxLabel": "${translation("DELETE_DATA")?json_string}"
					}
				]
			}
		];
		this.buttons = [
			{
				"text": "${translation("OK")?json_string}",
				"itemId": "ok",
				"disabled": true
			},
			{
				"text": "${translation("CANCEL")?json_string}",
				"itemId": "cancel"
			}
		];
	
		this.callParent(arguments);
	}
});