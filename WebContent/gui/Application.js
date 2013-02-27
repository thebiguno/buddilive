Ext.Loader.setConfig({"enabled": true});
//Ext.Loader.loadScript({"url": "parameters.js"});

Ext.state.Manager.setProvider(new Ext.state.LocalStorageProvider());

Ext.application({
	"name": "BuddiLive",
	"appFolder": "gui",
	
	"requires": [
		"BuddiLive.view.Viewport"
	],
	
	"controllers": [
		"Accounts"
	],
	"launch": function() {
		// This is fired as soon as the page is ready
		var viewport = Ext.create("BuddiLive.view.Viewport");
		BuddiLive.app = this;
		BuddiLive.app.viewport = viewport;
	}
});