Ext.define("BuddiLive.view.transaction.split.FromCombobox", {
	"extend": "BuddiLive.view.transaction.split.SourceCombobox",
	"alias": "widget.fromcombobox",
	"requires": [
		"BuddiLive.store.transaction.split.FromComboboxStore"
	],
	"store": "transaction.split.FromComboboxStore"
});