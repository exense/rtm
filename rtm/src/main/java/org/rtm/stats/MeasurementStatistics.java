package org.rtm.stats;

public class MeasurementStatistics {

		public enum AggregationType {
			COUNT("cnt"), MIN("min"), MAX("max"), SUM("sum"), AVG("avg"), PCL("pcl"), STD("std"), TPS("tps");

			String shortName;
			AggregationType(String s) {
				shortName = s;
			}
			public String getShort() {
				return shortName;
			} 
		}
		
		

}
