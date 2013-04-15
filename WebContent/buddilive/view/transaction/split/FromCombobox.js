Ext.define("BuddiLive.view.transaction.split.FromCombobox", {
	"extend": "BuddiLive.view.transaction.split.SourceCombobox",
	"alias": "widget.fromcombobox",
	"requires": [
		"BuddiLive.store.transaction.split.FromComboboxStore"
	],
	"store": "transaction.split.FromComboboxStore",
	
	"initComponent": function(){
		var combo = this;

		this.callParent(arguments);

		this.addListener("select", function(){
			
			//TODO Don't let them select two categories
			//TODO Ensure one of the items is the currently selected source
		});
	}
});