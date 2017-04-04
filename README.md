# rtm
Response Time Manager


To start RTM, use either jetty via the following command line, simply using the jar file or add the war file to the webapps folder of a tomcat installation.

java -cp "C:\rtm\rtm-1.0.0-jar-with-dependencies.jar" org.rtm.jetty.JettyStarter -config=C:\rtm\rtm.properties

!! IMPORTANT !! : make sure to update the paths to both files (the jar and the properties) before launching the command.
