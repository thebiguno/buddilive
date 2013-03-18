Ext.define("BuddiLive.view.transaction.split.ToCombobox", {
	"extend": "BuddiLive.view.transaction.split.SourceCombobox",
	"alias": "widget.tocombobox",
	"requires": [
		"BuddiLive.store.transaction.split.ToComboboxStore"
	],
	"store": "transaction.split.ToComboboxStore"
});