package org.hifly.bikedump.utility;

import org.hifly.bikedump.domain.gps.SlopeSegment;
import org.hifly.bikedump.domain.gps.Waypoint;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class SlopeUtilityTest {

    @Test
    public void testTotalAltimetricProfile_withValidWaypoints() {
        // Create a list of waypoints simulating a climb
        List<Waypoint> waypoints = new ArrayList<>();
        
        // Starting point: 0km, 100m elevation
        Waypoint wp1 = createWaypoint(45.0, 7.0, 0.0, 100.0, 0L);
        waypoints.add(wp1);
        
        // Middle point: 0.5km, 150m elevation (gained 50m)
        Waypoint wp2 = createWaypoint(45.01, 7.01, 0.5, 150.0, 600000L); // 10 minutes later
        waypoints.add(wp2);
        
        // End point: 1km, 200m elevation (gained 100m total)
        Waypoint wp3 = createWaypoint(45.02, 7.02, 1.0, 200.0, 1200000L); // 20 minutes later
        waypoints.add(wp3);
        
        // Calculate altimetric profile
        SlopeSegment profile = SlopeUtility.totalAltimetricProfile(waypoints);
        
        // Verify basic properties are set
        assertNotNull("Profile should not be null", profile);
        assertNotNull("Waypoints should be set", profile.getWaypoints());
        assertEquals("Should have 3 waypoints", 3, profile.getWaypoints().size());
        
        // Verify start and end coordinates
        assertEquals("Start latitude", 45.0, profile.getStartLatitude(), 0.001);
        assertEquals("Start longitude", 7.0, profile.getStartLongitude(), 0.001);
        assertEquals("End latitude", 45.02, profile.getEndLatitude(), 0.001);
        assertEquals("End longitude", 7.02, profile.getEndLongitude(), 0.001);
        
        // Verify elevations
        assertEquals("Start elevation", 100.0, profile.getStartElevation(), 0.001);
        assertEquals("End elevation", 200.0, profile.getEndElevation(), 0.001);
        assertEquals("Net elevation gain", 100.0, profile.getElevation(), 0.001);
        
        // Verify distances
        assertEquals("Start distance", 0.0, profile.getStartDistance(), 0.001);
        assertEquals("End distance", 1.0, profile.getEndDistance(), 0.001);
        assertEquals("Total distance", 1.0, profile.getDistance(), 0.001);
        
        // Verify gradient (100m elevation over 1km = 10%)
        assertEquals("Gradient", 10.0, profile.getGradient(), 0.1);
        
        // Verify dates are set
        assertNotNull("Start date should be set", profile.getStartDate());
        assertNotNull("End date should be set", profile.getEndDate());
        
        // Verify speed is calculated (1km in 20 minutes = 3 km/h)
        assertTrue("Average speed should be positive", profile.getAvgSpeed() > 0);
        
        // Verify VAM is calculated
        assertTrue("VAM should be positive", profile.getVam() > 0);
    }
    
    @Test
    public void testTotalAltimetricProfile_withEmptyList() {
        List<Waypoint> waypoints = new ArrayList<>();
        SlopeSegment profile = SlopeUtility.totalAltimetricProfile(waypoints);
        
        assertNotNull("Profile should not be null for empty list", profile);
        assertNull("Waypoints should be null for empty list", profile.getWaypoints());
    }
    
    @Test
    public void testTotalAltimetricProfile_withNullList() {
        SlopeSegment profile = SlopeUtility.totalAltimetricProfile(null);
        
        assertNotNull("Profile should not be null for null list", profile);
        assertNull("Waypoints should be null for null list", profile.getWaypoints());
    }
    
    @Test
    public void testTotalAltimetricProfile_withDescentAndAscent() {
        // Create waypoints with both ascent and descent
        List<Waypoint> waypoints = new ArrayList<>();
        
        waypoints.add(createWaypoint(45.0, 7.0, 0.0, 100.0, 0L));
        waypoints.add(createWaypoint(45.01, 7.01, 0.5, 150.0, 600000L)); // +50m
        waypoints.add(createWaypoint(45.02, 7.02, 1.0, 120.0, 1200000L)); // -30m
        waypoints.add(createWaypoint(45.03, 7.03, 1.5, 180.0, 1800000L)); // +60m
        
        SlopeSegment profile = SlopeUtility.totalAltimetricProfile(waypoints);
        
        // Net elevation: 180 - 100 = 80m
        assertEquals("Net elevation", 80.0, profile.getElevation(), 0.001);
        
        // Total distance: 1.5km
        assertEquals("Total distance", 1.5, profile.getDistance(), 0.001);
        
        // VAM should be calculated based on total ascent (50 + 60 = 110m), not net elevation
        assertTrue("VAM should be positive", profile.getVam() > 0);
    }
    
    private Waypoint createWaypoint(double lat, double lon, double distanceFromStart, 
                                   double elevation, long timeOffset) {
        Waypoint wp = new Waypoint();
        wp.setLat(lat);
        wp.setLon(lon);
        wp.setDistanceFromStartingPoint(distanceFromStart);
        wp.setEle(elevation);
        wp.setDateRelevation(new Date(timeOffset));
        return wp;
    }
}
