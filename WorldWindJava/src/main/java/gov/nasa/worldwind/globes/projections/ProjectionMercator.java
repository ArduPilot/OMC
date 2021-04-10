/*
 * Copyright (C) 2014 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.globes.projections;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.WWMath;

/**
 * Provides a Mercator projection of an ellipsoidal globe.
 *
 * @author tag
 * @version $Id: ProjectionMercator.java 2277 2014-08-28 21:19:37Z dcollins $
 */
public class ProjectionMercator extends AbstractGeographicProjection
{
    public ProjectionMercator()
    {
        super(Sector.fromDegrees(-78, 78, -180, 180));
    }

    @Override
    public String getName()
    {
        return "Mercator";
    }

    @Override
    public boolean isContinuous()
    {
        return true;
    }

    @Override
    public Vec4 geographicToCartesian(Globe globe, Angle latitude, Angle longitude, double metersElevation, Vec4 offset)
    {
        if (latitude.degrees > this.getProjectionLimits().getMaxLatitude().degrees)
            latitude = this.getProjectionLimits().getMaxLatitude();
        if (latitude.degrees < this.getProjectionLimits().getMinLatitude().degrees)
            latitude = this.getProjectionLimits().getMinLatitude();
        if (longitude.degrees > this.getProjectionLimits().getMaxLongitude().degrees)
            longitude = this.getProjectionLimits().getMaxLongitude();
        if (longitude.degrees < this.getProjectionLimits().getMinLongitude().degrees)
            longitude = this.getProjectionLimits().getMinLongitude();

        
      	double rMax = globe.getMaximumRadius();
    	double rMin = globe.getPolarRadius();
     
        
        double x=rMax * Math.toRadians(longitude.degrees);
		
		double es = 1 - (Math.pow(rMin / rMax, 2));
		double ecc = Math.sqrt(es);
		
		double p = Math.toRadians(latitude.degrees);
		
		double con = ecc * Math.sin(p);
		double com = ecc /2.0;
		double con2 = Math.pow((1 - con)/(1 + con), com);
		double ts = Math.tan((Math.PI/2.0 - p) / 2.0)/con2;
		double y = - rMax * Math.log(ts);
        return new Vec4(x, y);
    }

    @Override
    public void geographicToCartesian(Globe globe, Sector sector, int numLat, int numLon, double[] metersElevation,
        Vec4 offset, Vec4[] out)
    {
        double eqr = globe.getEquatorialRadius();
        double ecc = Math.sqrt(globe.getEccentricitySquared());
        double minLat = sector.getMinLatitude().radians;
        double maxLat = sector.getMaxLatitude().radians;
        double minLon = sector.getMinLongitude().radians;
        double maxLon = sector.getMaxLongitude().radians;
        double deltaLat = (maxLat - minLat) / (numLat > 1 ? numLat - 1 : 1);
        double deltaLon = (maxLon - minLon) / (numLon > 1 ? numLon - 1 : 1);
        double minLatLimit = this.getProjectionLimits().getMinLatitude().radians;
        double maxLatLimit = this.getProjectionLimits().getMaxLatitude().radians;
        double minLonLimit = this.getProjectionLimits().getMinLongitude().radians;
        double maxLonLimit = this.getProjectionLimits().getMaxLongitude().radians;
        double offset_x = offset.x;
        int pos = 0;

        // Iterate over the latitude and longitude coordinates in the specified sector, computing the Cartesian point
        // corresponding to each latitude and longitude.
        double lat = minLat;
        for (int j = 0; j < numLat; j++, lat += deltaLat)
        {
            if (j == numLat - 1) // explicitly set the last lat to the max latitude to ensure alignment
                lat = maxLat;
            lat = WWMath.clamp(lat, minLatLimit, maxLatLimit); // limit lat to projection limits

            // Latitude is constant for each row. Values that are a function of latitude can be computed once per row.
            double sinLat = Math.sin(lat);
            double s = ((1 + sinLat) / (1 - sinLat)) * Math.pow((1 - ecc * sinLat) / (1 + ecc * sinLat), ecc);
            double y = eqr * Math.log(s) * 0.5;

            double lon = minLon;
            for (int i = 0; i < numLon; i++, lon += deltaLon)
            {
                if (i == numLon - 1) // explicitly set the last lon to the max longitude to ensure alignment
                    lon = maxLon;
                lon = WWMath.clamp(lon, minLonLimit, maxLonLimit); // limit lon to projection limits

                double x = eqr * lon + offset_x;
                double z = metersElevation[pos];
                out[pos++] = new Vec4(x, y, z);

            }
        }
    }

   /**
    * Code is partly taken from the source http://wiki.openstreetmap.org/wiki/Mercator#JavaScript
    */
    @Override
    public Position cartesianToGeographic(Globe globe, Vec4 cart, Vec4 offset)
    {
    	
    	double rMax = globe.getMaximumRadius();
    	double rMin = globe.getPolarRadius();
     
    	double lon=Math.toDegrees(((cart.x/rMax)));
		
		double temp = rMin / rMax;
		double e = Math.sqrt(1.0 - (temp * temp));
		double lat=Math.toDegrees(pj_phi2( Math.exp( 0-(cart.y/rMax)), e));
		
    	return new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), cart.z);
    }


	public static double pj_phi2 (double ts, double e) 
	{
		double i = 15;
		
		double EPS = 1E-10;
		double p;
		double con;
		double dp;
		double ecc = e / 2.0;
		p = Math.PI/2.0 - 2 * Math.atan (ts);
		do 
		{
			con = e * Math.sin (p);
			dp = Math.PI/2.0 - 2 * Math.atan (ts * Math.pow((1 - con) / (1 + con), ecc)) - p;
			p += dp;
			
		} 
		while ( Math.abs(dp)>EPS && --i > 0);
		return p;
	}
    @Override
    public Vec4 northPointingTangent(Globe globe, Angle latitude, Angle longitude)
    {
        return Vec4.UNIT_Y;
    }
}
