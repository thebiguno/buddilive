Ext.define("BuddiLive.view.report.picker.Interval", {
	"extend": "Ext.window.Window",
	"alias": "widget.reportpickerinterval",
	"requires": [
	],
	
	"initComponent": function(){
		var me = this;
		var s = this.initialConfig.selected

		this.title = "${translation("INTERVAL_PICKER")?json_string}";
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
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_INTERVAL_PICKER")?json_string}",
						"type": "combobox",
						"itemId": "interval",
						"value": "PLUGIN_FILTER_THIS_MONTH",
						"fieldLabel": "${translation("INTERVAL")?json_string}",
						"forceSelection": true,
						"editable": false,
						"allowBlank": false,
						"store": new Ext.data.Store({
							"fields": ["text", "value"],
							"data": [
								{"text": "${translation("PLUGIN_FILTER_THIS_WEEK")?json_string}", "value": "PLUGIN_FILTER_THIS_WEEK"},
								{"text": "${translation("PLUGIN_FILTER_LAST_WEEK")?json_string}", "value": "PLUGIN_FILTER_LAST_WEEK"},
								{"text": "${translation("PLUGIN_FILTER_THIS_SEMI_MONTH")?json_string}", "value": "PLUGIN_FILTER_THIS_SEMI_MONTH"},
								{"text": "${translation("PLUGIN_FILTER_LAST_SEMI_MONTH")?json_string}", "value": "PLUGIN_FILTER_LAST_SEMI_MONTH"},
								{"text": "${translation("PLUGIN_FILTER_THIS_MONTH")?json_string}", "value": "PLUGIN_FILTER_THIS_MONTH"},
								{"text": "${translation("PLUGIN_FILTER_LAST_MONTH")?json_string}", "value": "PLUGIN_FILTER_LAST_MONTH"},
								{"text": "${translation("PLUGIN_FILTER_THIS_QUARTER")?json_string}", "value": "PLUGIN_FILTER_THIS_QUARTER"},
								{"text": "${translation("PLUGIN_FILTER_LAST_QUARTER")?json_string}", "value": "PLUGIN_FILTER_LAST_QUARTER"},
								{"text": "${translation("PLUGIN_FILTER_THIS_YEAR")?json_string}", "value": "PLUGIN_FILTER_THIS_YEAR"},
								{"text": "${translation("PLUGIN_FILTER_THIS_YEAR_TO_DATE")?json_string}", "value": "PLUGIN_FILTER_THIS_YEAR_TO_DATE"},
								{"text": "${translation("PLUGIN_FILTER_LAST_YEAR")?json_string}", "value": "PLUGIN_FILTER_LAST_YEAR"},
								{"text": "${translation("PLUGIN_FILTER_ALL_TIME")?json_string}", "value": "PLUGIN_FILTER_ALL_TIME"},
								{"text": "${translation("PLUGIN_FILTER_OTHER")?json_string}", "value": "PLUGIN_FILTER_OTHER"}
							]
						}),
						"queryMode": "local",
						"valueField": "value"
					}
				]
			}
		];
		this.buttons = [
			{
				"text": "${translation("OK")?json_string}",
				"itemId": "ok",
				"listeners": {
					"click": function(){
						me.initialConfig.callback(me.down("combobox[itemId='interval']").getValue());
						me.close();
					}
				}
			},
			{
				"text": "${translation("CANCEL")?json_string}",
				"itemId": "cancel",
				"listeners": {
					"click": function(){
						me.close();
					}
				}
			}
		]
	
		this.callParent(arguments);
	}
});