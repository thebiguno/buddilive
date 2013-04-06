Ext.define("BuddiLive.store.report.PieIncomeByCategoryStore", {
	"extend": "Ext.data.Store",
	"requires": [
		"BuddiLive.model.report.PieModel"
	],
	"model": "BuddiLive.model.report.PieModel",
	"autoLoad": true,
	"proxy": {
		"type": "ajax",
		"url": "buddilive/report/pietotalsbycategory.json?type=I&fromDate=2012-01-01&toDate=2013-01-01",
		"reader": {
			"type": "json",
			"root": "data"
		}
	}
});