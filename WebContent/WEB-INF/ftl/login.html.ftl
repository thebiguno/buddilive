<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<title>Buddi Web</title>
		<link rel="stylesheet" type="text/css" href="extjs/resources/css/ext-all-gray.css">
		<link rel="icon" type="image/png" href="img/favicon.png"/>
		<script type="text/javascript" src="extjs/bootstrap.js"></script>
		<script type="text/javascript">
			Ext.Loader.require([
				"Ext.form.*"
			]);
			Ext.application({
				"name": "Sieve",				//Root Package name
				"launch": function() {
					// This is fired as soon as the page is ready
					var panel = Ext.widget("panel", {
						"title": "Enrich Mail Rule Editor Login",
						"renderTo": "form",
						"layout": "fit",
						"items": {
							"xtype": "form",
							"border": false,
							"standardSubmit": true,
							"padding": 2,
							"defaults": {
								"anchor": "100%",
								"margin": 5,
								"listeners": {
									"specialkey": function(field, event) {
										if (event.getKey() == event.ENTER) {
											panel.down("form").getForm().submit();
										}
									}
								}
							},
							"items": [
								{
									"xtype": "textfield",
									"name": "user",
									"allowBlank": false,
									"fieldLabel": "Username"
								},
								{
									"xtype": "textfield",
									"name": "password",
									"inputType": "password", 
									"allowBlank": false,
									"fieldLabel": "Password"
								},
								{
									"xtype": "checkbox",
									"name": "secure",
									"fieldLabel": "Secure Login"
								}
							],
							"buttons": [
								{
									"xtype": "button",
									"formbind": true,
									"text": "Login",
									"handler": function(){
										panel.down("form").getForm().submit();
									}
								}
							]
						}
					});
				}
			});
		</script>
	</head>
	<body>
		<div style="height: 100%; margin: 0 auto -2em;">
			<div style="float: left; width:100%; border-bottom: 1px solid black;"><img src="img/header.jpg"/></div>
			<div id="form" style="width: 300px; margin-top: 50px; margin-right: 50px; float: right;"></div>
			<div style="margin-top: 50px; margin-left: 100px; float: left;">Welcome to Buddi Web.  Please log in, or sign up for an account.</div>
		</div>
		<div style="width: 100%; height: 2em; border-top: 1px solid black; text-align: right; padding-right: 50px;">&copy; Wyatt Olson  http://digitalcave.ca</div>
	</body>
</html>

