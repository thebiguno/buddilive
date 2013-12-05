Ext.define("BuddiLive.controller.scheduled.Editor", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"schedulededitor component": {
				"blur": this.updateButtons,
				"keyup": this.updateButtons,
				"afterrender": this.updateButtons
			},
			"schedulededitor button[itemId='ok']": {"click": this.ok},
			"schedulededitor button[itemId='cancel']": {"click": this.cancel}
		});
	},
	
	"updateButtons": function(component){
		var window = component.up("schedulededitor");
		var ok = window.down("button[itemId='ok']");
		var name = window.down("textfield[itemId='name']");
		var startDate = window.down("datefield[itemId='startDate']");
		var transaction = window.down("transactioneditor")
		
		ok.setDisabled(name.getValue().length == 0 || startDate.getValue() == null || !transaction.validate());
	},
	
	"cancel": function(component){
		component.up("schedulededitor").close();
	},
	
	"ok": function(component){
		var window = component.up("schedulededitor");
		var panel = window.initialConfig.panel;
		var selected = window.initialConfig.selected;

		var request = {"action": (selected ? "update" : "insert")};
		request.id = window.down("hidden[itemId='id']").getValue();
		request.name = window.down("textfield[itemId='name']").getValue();
		request.repeat = window.down("combobox[itemId='repeat']").getValue();
		request.start = Ext.Date.format(window.down("datefield[itemId='startDate']").getValue(), "Y-m-d");
		request.end = Ext.Date.format(window.down("datefield[itemId='endDate']").getValue(), "Y-m-d");
		request.transaction = window.down("transactioneditor").getTransaction();
		request.message = window.down("textarea[itemId='message']").getValue();
		
		var activeCard = window.down("panel[itemId='cardLayoutPanel']").getLayout().getActiveItem();
		request.scheduleDay = activeCard.getScheduleDay();
		request.scheduleWeek = activeCard.getScheduleWeek();
		request.scheduleMonth = activeCard.getScheduleMonth();

		var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": window});
		mask.show();
		
		var conn = new Ext.data.Connection();
		conn.request({
			"url": "data/scheduledtransactions",
			"headers": {
				"Accept": "application/json"
			},
			"method": "POST",
			"jsonData": request,
			"success": function(response){
				mask.hide();
				window.close();
				panel.getStore().load();
				panel.getSelectionModel().deselectAll()
			},
			"failure": function(response){
				mask.hide();
				BuddiLive.app.error(response);
			}
		});
	}
});