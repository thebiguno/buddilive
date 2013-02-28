Ext.define('BuddiLive.view.Transactions', {
	"extend": "Ext.grid.Panel",
	"alias": "widget.budditransactions",
	
	"initComponent": function(){
		this.closable = true;
		this.title = "Transactions";
		this.layout = "fit";
		this.store = Ext.create("BuddiLive.store.Transactions");
		this.columns = [
			{
				"text": "Name",
				"dataIndex": "name",
				"flex": 1
			}
		];
		this.callParent(arguments);
	}
});