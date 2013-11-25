package aspects;

import interactive.Client;

import java.util.HashMap;
import java.util.UUID;


public abstract aspect AspectITD {

	public static PerformanceMeasure Client.performanceMeasure = new PerformanceMeasure();
	public static HashMap<UUID, Long> Client.sendMarkers = new HashMap<UUID, Long>();
}
