Ext.define("BuddiLive.controller.transaction.split.Editor", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"spliteditor button[itemId='addSplit']": {"click": this.addSplit},
			"spliteditor button[itemId='removeSplit']": {"click": this.removeSplit}
		});
	},
	
	"addSplit": function(component){
		var transactionEditor = component.up("transactioneditor");
		transactionEditor.add({"xtype": "spliteditor"});
		this.updateButtons(transactionEditor);
	},
	
	"removeSplit": function(component){
		var splitLine = component.up("spliteditor");
		var transactionEditor = component.up("transactioneditor");
		if (Ext.ComponentQuery.query("spliteditor", transactionEditor).length > 1){
			transactionEditor.remove(splitLine);
		}
		this.updateButtons(transactionEditor);
	},
	
	"updateButtons": function(transactionEditor){
		var splitEditors = Ext.ComponentQuery.query("spliteditor", transactionEditor);
		var visible = splitEditors.length > 1;
		for (var i = 0; i < splitEditors.length; i++){
			splitEditors[i].down("button[itemId='removeSplit']").setVisible(visible);
			splitEditors[i].down("button[itemId='addSplit']").setVisible(i == splitEditors.length - 1);
		}
	}
});