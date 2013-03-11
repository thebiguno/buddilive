Ext.define("BuddiLive.controller.Viewport", {
	"extend": "Ext.app.Controller",

	"init": function() {
		this.control({
			"buddiviewport button[itemId='logout']": {"click": this.logout}
		});
	},
	
	"logout": function(component){
		window.location.search += '&logout';
	}
});