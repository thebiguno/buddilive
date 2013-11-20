Ext.application({
	"name": "BuddiLive",
	"appFolder": "buddilive",
	
	"views": ["login.Login"],
	"controllers": ["login.Login"],

	"launch": function() {
		BuddiLive.app = this;
		Ext.widget('login');
	}
});

animateMessage = function(label, text){
	label.animate({"to": {"opacity": 0},"duration": 0});	//Set label opacity to 0 initially; this only matters on first call for a given label.  TODO How do we set opacity without animation?
	label.setText(text);
	label.animate({
		"to": {
			"opacity": 1
		},
		"duration": 200
	}).animate({
		"to": {
			"opacity": 1
		},
		"duration": 1000
	}).animate({
		"to": {
			"opacity": 0
		},
		"duration": 2000
	});
};