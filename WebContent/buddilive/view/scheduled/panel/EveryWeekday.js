Ext.define("BuddiLive.view.scheduled.panel.EveryWeekday", {
	"extend": "Ext.panel.Panel",
	"alias": "widget.scheduledpaneleveryweekday",
	"requires": [
	],
	
	"initComponent": function(){
		var s = this.initialConfig.selected;
		this.itemId = "SCHEDULE_FREQUENCY_EVERY_WEEKDAY";
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