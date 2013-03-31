Ext.define("BuddiLive.view.scheduled.panel.EveryDay", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledpaneleveryday",
	"requires": [
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected;
		this.itemId = "SCHEDULE_FREQUENCY_EVERY_DAY";
		this.border = false;
		this.callParent(arguments);
		this.padding = 0;
		this.height = 0;
	},
	
	"getScheduleDay": function(){
		return 0;
	},
	"getScheduleWeek": function(){
		return 0;
	},
	"getScheduleMonth": function(){
		return 0;
	}
});