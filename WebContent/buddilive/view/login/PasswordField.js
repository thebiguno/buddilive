Ext.define("BuddiLive.view.login.PasswordField", {
	"extend": "Ext.form.FieldContainer",
	"alias": "widget.buddipasswordfield",

	"initComponent": function(){
		this.passwordStrengthColorId = Ext.id();
		this.layout = {
			"type": "hbox",
			"align": "stretch",
			"defaultMargins": {
				"right": 5
			}
		};
		this.items = [
			{
				"xtype": "textfield",
				"inputType": "password",
				"enableKeyEvents": true,
				"name": this.name,
				"itemId": "password",
				"options": this,
				"minPasswordStrength": (this.minPasswordStrength ? this.minPasswordStrength : 20),
				"flex": 1,
				"passwordStrength": function(password){
					var factor = 0;
					
					//Determine factor, based on character class
					if (/.*[a-z].*/.test(password)) factor = factor + 2.6;
					if (/.*[A-Z].*/.test(password)) factor = factor + 2.6;
					if (/.*[0-9].*/.test(password)) factor = factor + 1.0;
					if (/.*[ ].*/.test(password)) factor = factor + 0.1;
					if (/.*[^a-zA-Z0-9 ].*/.test(password)) factor = factor + 3.2;
					
					return password.length * Math.round(factor);
				},
				"validator": function(value){
					this.ownerCt.getComponent(1).validate();
					if (value.length == 0 || this.passwordStrength(value) > this.initialConfig.minPasswordStrength) return true;
					else return "Password too weak.";
				},
				"listeners": {
					"keyup": function(field){
						var position;
						var value = field.getValue();
						var strength = field.passwordStrength(value);
			
						if (value.length == 0) position = 15;
						else if (strength < 10) position = -135;
						else if (strength < 20) position = -120;
						else if (strength < 30) position = -105;
						else if (strength < 40) position = -90;
						else if (strength < 50) position = -75;
						else if (strength < 60) position = -60;
						else if (strength < 70) position = -45;
						else if (strength < 80) position = -30;
						else if (strength < 90) position = -15;
						else position = 0;
						
						var element = Ext.get(field.initialConfig.options.passwordStrengthColorId).dom.firstChild;
						element.style.backgroundImage = "url(" + "img/password.png)";
						element.style.backgroundRepeat = "no-repeat";
						element.style.backgroundPosition = "center " + position + "px";
					}
				}
			},
			{
				"xtype": "textfield",
				"inputType": "password",
				"flex": 1,
				"submitValue": false,
				"validator": function(value){
					if (this.ownerCt.getComponent(0).getValue() != value) {
						return "Passwords must match";
					} else {
						return true;
					}
				}
			},
			{
				"xtype": "panel",
				"id": this.passwordStrengthColorId,
				"style": "margin-top: 4px; margin-bottom: 4px;",
				"height": 15,
				"width": 200,
				"border": false
			}
		];

		this.callParent(arguments);
	},
	
	"getValue": function(){
		return this.down("textfield[itemId='password']").getValue();
	}
});