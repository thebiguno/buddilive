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
						"listeners": {
							"select": function(combo){
								combo.up("form").down("selfdocumentingfield[childItemId='startDate']").setVisible(combo.getValue() == "PLUGIN_FILTER_OTHER");
								combo.up("form").down("selfdocumentingfield[childItemId='endDate']").setVisible(combo.getValue() == "PLUGIN_FILTER_OTHER");
							}
						},
						"queryMode": "local",
						"valueField": "value"
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_START_DATE")?json_string}",
						"type": "datefield",
						"itemId": "startDate",
						"allowBlank": false,
						"fieldLabel": "${translation("START_DATE")?json_string}",
						"msgTarget": "none",
						"hidden": true,
						"value": new Date(),
						"maxValue": new Date(),
						"listeners": {
							"change": function(field){
								field.up("form").down("datefield[itemId='endDate']").setMinValue(field.getValue());
							}
						}
					},
					{
						"xtype": "selfdocumentingfield",
						"messageBody": "${translation("HELP_END_DATE")?json_string}",
						"type": "datefield",
						"itemId": "endDate",
						"allowBlank": false,
						"fieldLabel": "${translation("END_DATE")?json_string}",
						"msgTarget": "none",
						"hidden": true,
						"value": new Date(),
						"minValue": new Date(),
						"listeners": {
							"change": function(field){
								field.up("form").down("datefield[itemId='startDate']").setMaxValue(field.getValue());
							}
						}
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
						var interval = me.down("combobox[itemId='interval']").getValue();
						var query = "interval=" + interval;
						if (interval == "PLUGIN_FILTER_OTHER"){
							var startValid = me.down("datefield[itemId='startDate']").validate();
							var endValid = me.down("datefield[itemId='endDate']").validate();
							if (!startValid || !endValid) return;
							
							query += ("&startDate=" + Ext.Date.format(me.down("datefield[itemId='startDate']").getValue(), "Y-m-d"));
							query += ("&endDate=" + Ext.Date.format(me.down("datefield[itemId='endDate']").getValue(), "Y-m-d"));
						}
						me.initialConfig.callback(query);
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