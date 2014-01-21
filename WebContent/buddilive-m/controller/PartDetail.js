Ext.define("BuddiLive.controller.PartDetail", {
	"extend": "Ext.app.Controller",
	"config": {
		"refs": {
		},
		"control": {
			"partdetail button[itemId=back]": {
				"tap": function(button) {
					var formpanel = button.up('viewport').down('formpanel');
					formpanel.setRecord(null);
					var partlist = button.up('viewport').down('partlist');
					Ext.Viewport.animateActiveItem(partlist, {"type": 'slide', "direction": 'right'});
				}
			},
			"partdetail field": {
				"change": function(field) {
					var formpanel = field.up('formpanel');
					var values = formpanel.getValues();
					var record = formpanel.getRecord();
					if (record == null) return;
					record.set(values);
					Ext.Ajax.request({
						"url": "categories/" + record.data.category + "/parts/" + record.data.id,
						"method": "PUT",
						"jsonData": record.data,
						"success": function(response) {
							record.commit();
						},
						"failure": function(response) {
							record.reject();
						}
					});
				}
			},
		}
	}
});
