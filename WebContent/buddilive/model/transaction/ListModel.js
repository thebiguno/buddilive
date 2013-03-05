Ext.define('BuddiLive.model.transaction.ListModel', {
	"extend": "Ext.data.Model",
	"fields": ["date", "description", "number", "deleted", "amount", "from", "to", "splits"]
});