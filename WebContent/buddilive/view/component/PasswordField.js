Ext.define("BuddiLive.view.component.PasswordField", {
	"extend": "Ext.form.FieldContainer",
	"alias": "widget.passwordfield",
	
	"layout": {
		"type": "form"
	},

	"getValue": function(){
		return this.down("textfield[itemId='password']").getValue();
	},
	
	"isValid": function(){
		return this.down("textfield[itemId='password']").isValid() && this.down("textfield[itemId='confirm']").isValid();
	},
	
	"initComponent": function(){
		this.items = [
			{
				"xtype": "container",
				"layout": "hbox",
				"items": [
					{
						"xtype": "textfield",
						"inputType": "password",
						"name": this.name,
						"enableKeyEvents": true,
						"itemId": "password",
						"options": this,
						"flex": 1,
						"minPasswordStrength": (this.minPasswordStrength ? this.minPasswordStrength : 20),
						"passwordStrength": function(password){
							var factor = 0;
							
							//Determine factor, based on character class
							if (/.*[a-z].*/.test(password)) factor = factor + 2.6;
							if (/.*[A-Z].*/.test(password)) factor = factor + 2.6;
							if (/.*[0-9].*/.test(password)) factor = factor + 1.0;
							if (/.*[ ].*/.test(password)) factor = factor + 0.1;
							if (/.*[!@#$%^&*()].*/.test(password)) factor = factor + 1.0;
							if (/.*[^a-zA-Z0-9!@#$%^&*() ].*/.test(password)) factor = factor + 2.2;
							
							return Math.pow(password.length, 3) * factor / 100;
						},
						"validator": function(value){
							this.ownerCt.getComponent(1).validate();
							if (value.length == 0 || this.passwordStrength(value) > this.initialConfig.minPasswordStrength) return true;
							else return "Password too weak.";
						},
						"listeners": {
							"keyup": function(field){
								var color;
								var value = field.getValue();
								var strength = field.passwordStrength(value);
	
								if (value.length == 0 || strength < 10) color = "#953131";
								else if (strength < 20) color = "#ab5e4a";
								else if (strength < 30) color = "#b17253";
								else if (strength < 40) color = "#b2894f";
								else if (strength < 50) color = "#b18c51";
								else if (strength < 60) color = "#bc9c45";
								else if (strength < 70) color = "#b5b557";
								else if (strength < 80) color = "#8cac4a";
								else if (strength < 90) color = "#74b254";
								else if (strength < 100) color = "#4aa94a";
								else { color = "#26a826"; strength = 100; }
								
								var draw = field.up("passwordfield").down("draw[itemId=passwordbar]");
								var surface = draw.surface;
								var sprite = surface.items.get(0);
								sprite.stopAnimation();
								sprite.animate({
									"to": {
										"width": strength * draw.getWidth() / 100,
										"fill": color
									},
									"duration": 500
								});
							}
						}
					},
					{
						"xtype": "textfield",
						"inputType": "password",
						"itemId": "confirm",
						"flex": 1,
						"margin": "0 0 0 5",
						"submitValue": false,
						"validator": function(value){
							if (this.ownerCt.getComponent(0).getValue() != value) {
								return "Passwords must match";
							} else {
								return true;
							}
						}
					}
				]
			},
			{
				"xtype": "draw",
				"height": 7,
				"itemId": "passwordbar",
				"viewBox": false,
				"items": [
					{
						"type": "rect",
						"width": 0,
						"height": 5,
						"fill": "#000",
						"stroke": "#666",
						"strokeWidth": 1
					}
				]
			}
		];
		
		this.callParent(arguments);
	}
});