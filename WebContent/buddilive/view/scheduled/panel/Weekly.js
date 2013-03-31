Ext.define("BuddiLive.view.scheduled.panel.Weekly", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledpanelweekly",
	"requires": [
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected;
		this.itemId = "SCHEDULE_FREQUENCY_WEEKLY";
		this.border = false;
		this.layout = "form";
		this.padding = 0;
		this.items = [
			{
				"xtype": "selfdocumentingfield",
				"messageBody": "${translation("HELP_REPEATING_WEEKLY")?json_string}",
				"type": "combobox",
				"fieldLabel": "${translation("REPEATING_WEEKLY")?json_string}",
				"value": (s ? s.scheduleDay : 0),
				"displayField": "text",
				"valueField": "value",
				"allowBlank": false,
				"forceSelection": true,
				"store": new Ext.data.Store({
					"fields": ["text", "value"],
					"data": [
						{"text": "${translation("SCHEDULE_DAY_SUNDAY")?json_string}", "value": 0},
						{"text": "${translation("SCHEDULE_DAY_MONDAY")?json_string}", "value": 1},
						{"text": "${translation("SCHEDULE_DAY_TUESDAY")?json_string}", "value": 2},
						{"text": "${translation("SCHEDULE_DAY_WEDNESDAY")?json_string}", "value": 3},
						{"text": "${translation("SCHEDULE_DAY_THURSDAY")?json_string}", "value": 4},
						{"text": "${translation("SCHEDULE_DAY_FRIDAY")?json_string}", "value": 5},
						{"text": "${translation("SCHEDULE_DAY_SATURDAY")?json_string}", "value": 6}
					]
				}),
				"queryMode": "local",
				"valueField": "value"
			}
		];
	
		this.callParent(arguments);
	},
	
	"getScheduleDay": function(){
		return this.down("combobox").getValue();
	},
	"getScheduleWeek": function(){
		return 0;
	},
	"getScheduleMonth": function(){
		return 0;
	}
});