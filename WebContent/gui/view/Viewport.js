Ext.define("BuddiLive.view.Viewport", {
	"extend": "Ext.container.Viewport",
	
	"requires": [
		"BuddiLive.view.TabPanel",
	],
	
	"initComponent": function() {
		this.layout = "fit";
		this.height = "100%";
		this.width = "100%";
		this.items = {
			"xtype": "budditabpanel"
		};

		this.callParent();
	}
});