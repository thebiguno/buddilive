We use JSON as the link between the server and the web GUI.  The following standards should be kept when communicating:

1) While the server uses fixed point long integers for amounts in internal storage and processing, all communication between server and client will be in human readable format (i.e. dollars and cents; $123.45 will be sent as '123.45').
2) All POST commands sent to the server will have have an action parameter.  This will determine whether the server is to insert, update, etc.  Valid action parameters will vary based on the resource being accessed.
