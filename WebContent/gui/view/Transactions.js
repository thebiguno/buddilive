Ext.define('BuddiLive.view.Transactions', {
	"extend": "Ext.grid.Panel",
	"alias": "widget.budditransactions",
	
	"initComponent": function(){
		this.closable = true;
		this.columns = [
			"Foo", "Bar"
		];
		this.callParent(arguments);
	}
});