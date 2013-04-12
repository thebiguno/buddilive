Ext.define("BuddiLive.controller.restore.Form", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"restoreform filefield": {
				"change": this.updateButtons
			},
			"restoreform button[itemId='ok']": {"click": this.ok},
			"restoreform button[itemId='cancel']": {"click": this.cancel}
		});
	},
	
	"cancel": function(component){
		component.up("restoreform").close();
	},
	
	"ok": function(component){
		var window = component.up("restoreform");
		var form = window.down("form").getForm();
		var submit = function(deleteData){
			var mask = new Ext.LoadMask({"msg": "${translation("PROCESSING")?json_string}", "target": window});
			mask.show();
			form.submit({
				"url": "data/restore?deleteData=" + deleteData,
				"success": function(form, action){
					Ext.MessageBox.show({
						"title": "${translation("RESTORE_SUCCESS_TITLE")?json_string}",
						"msg": "${translation("RESTORE_SUCCESS_MESSAGE")?json_string}",
						"buttons": Ext.Msg.OK,
						"fn": function(){
							location.reload();
						}
					});
					mask.hide();
					window.close();
				},
				"failure": function(form, action){
					mask.hide();
					BuddiLive.app.error();
				}
			});
		};
		
		var deleteData = window.down("checkbox[itemId='deleteData']").getValue();
		if (deleteData){
			Ext.MessageBox.show({
				"title": "${translation("DELETE_DATA")?json_string}",
				"msg": "${translation("CONFIRM_DELETE_DATA")?json_string}",
				"buttons": Ext.MessageBox.YESNO,
				"fn": function(buttonId){
					if (buttonId != "yes") return;
					
					submit(true);
				}
			});
		}
		else {
			submit(false);
		}
	},
	
	"updateButtons": function(component){
		var window = component.up("restoreform");
		var form = window.down("form").getForm();
		var ok = window.down("button[itemId='ok']");
		
		ok.setDisabled(!form.isValid());
	}
});