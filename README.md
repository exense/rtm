# rtm
Response Time Manager


To start RTM, use either jetty via the following command line, simply using the jar file or add the war file to the webapps folder of a tomcat installation.

java -cp "{pathToJar}\rtm-{version}-jar-with-dependencies.jar" org.rtm.jetty.JettyStarter -config={pathToProperties}\rtm.properties





--> RTM is then accessible through the url http://localhost:8099/rtm
