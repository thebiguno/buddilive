Ext.define('BuddiLive.view.report.PieTotalsByCategory', {
	"extend": "Ext.panel.Panel",
	"alias": "widget.reportpietotalsbycategory",
	
	"requires": [
		"BuddiLive.store.report.PieTotalsByCategoryStore"
	],
	
	"initComponent": function(){
		this.title = "${translation("REPORT_PIE_INCOME_BY_CATEGORY")?json_string}",
		this.closable = true;
		this.dockedItems = BuddiLive.app.viewport.getDockedItems();
		this.layout = "fit";
		
		this.items = [
			{
				"xtype": "chart",
				"store": Ext.create("BuddiLive.store.report.PieTotalsByCategoryStore", {"interval": this.initialConfig.interval, "type": this.initialConfig.type}),
				"series": [{
					"type": "pie",
					"angleField": "amount",
					"showInLegend": true,
					"colorSet": ["#bf3030", "#bf8630", "#a3bf30", "#4dbf30", "#30bf69", "#30bfbf", "#3069bf", "#4c30bf", "#a330bf", "#bf3086"],
					"tips": {
						"trackMouse": true,
						"width": 500,
						"height": 28,
						"renderer": function(item) {
							this.setTitle(item.get("label") + ": " + item.get("formattedAmount") + " (" + item.get("percent") + "%)");
						}
					},
					"highlight": {
						"segment": {
							"margin": 20
						}
					},
					"label": {
						"field": "label",
						"display": "rotate",
						"contrast": true,
						"renderer": function(value, storeItem, item) {
							return value + ": " + item.get("formattedAmount");
						}
					}
				}]
			}
		]
	
		this.callParent(arguments);
	}
});