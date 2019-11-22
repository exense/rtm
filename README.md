# rtm
Response Time Manager

-- Requirements:
The only thing you need prior to starting RTM is a running mongodb instance (if you don't use default settings, you'll need to edit the file "rtm.properties" first).

-- To start RTM:

option 1 : standalone jar file (uses jetty internally)
java -cp "{pathToJar}\rtm-{version}-jar-with-dependencies.jar" org.rtm.jetty.JettyStarter -config={pathToProperties}\rtm.properties

option 2 : WAR file
add the war file to the webapps folder of a tomcat installation and add the rtm.properties file to its classpath.

--> Measurement ingestion:

curl localhost:8099/rtm/rest/ingest/structured/myMeasurementGroup/1494428139000/myMeasurementName/1234

--> Accessing the webapp:
http://localhost:8099/rtm
