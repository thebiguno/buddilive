Ext.define('BuddiLive.view.report.IncomeAndExpensesByCategory', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportincomeandexpensesbycategory",
	
	"initComponent": function(){
		this.title = "My Reports";	//TODO i18n
		this.closable = true;
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
	
		this.callParent(arguments);
	}
});