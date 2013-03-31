Ext.define("BuddiLive.view.scheduled.panel.EveryXDays", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledpaneleveryxdays",
	"requires": [
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected;
		this.itemId = "SCHEDULE_FREQUENCY_EVERY_X_DAYS";
		this.border = false;
		this.layout = "form";
		this.padding = 0;
		this.items = [
			{
				"xtype": "selfdocumentingfield",
				"messageBody": "${translation("HELP_REPEATING_EVERY_X_DAYS")?json_string}",
				"type": "panel",
				"border": false,
				"layout": "hbox",
				"fieldLabel": "${translation("REPEATING_EVERY_X_DAYS")?json_string}",
				"items": [
					{
						"xtype": "numberfield",
						"value": (s ? s.scheduleDay : 7),
						"allowBlank": false,
						"hideTrigger": true,
						"keyNavEnabled": false,
						"mouseWheelEnabled": false,
						"minValue": 1,
						"flex": 1
					},
					{
						"xtype": "displayfield",
						"value": "${translation("DAYS")?json_string}",
						"margin": "0 0 0 10",
						"width": 50
					}
				]
			}
		];
	
		this.callParent(arguments);
	},
	
	"getScheduleDay": function(){
		return this.down("numberfield").getValue();
	},
	"getScheduleWeek": function(){
		return 0;
	},
	"getScheduleMonth": function(){
		return 0;
	}
});