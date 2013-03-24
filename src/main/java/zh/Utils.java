package zh;

public class Utils
{
    public static double normalRelativeAngle( double angle )
    {
        if ( ( angle > -180.0D ) && ( angle <= 180.0D ) )
        {
            return angle;
        }
        double fixedAngle = angle;

        while ( fixedAngle <= -180.0D )
        {
            fixedAngle += 360.0D;
        }
        while ( fixedAngle > 180.0D )
        {
            fixedAngle -= 360.0D;
        }
        return fixedAngle;
    }

    public static double sign( double value )
    {
        if ( value == 0.0D )
        {
            return 1.0D;
        }

        return value / Math.abs( value );
    }

    /**
     * Robocode uses a clockwise direction convention where 
     *      0/360 deg is "North", 90 deg towards "East", 180 towards "South", and 270 deg towards "West".
     * This is a flipped mirror from the usual polar coordinate convention of
     *      0/360 deg is "East", 90 deg towards "North", 180 towards "West", and 270 deg towards "South".
     * This results in the X & Y axis being reversed and the reason for using the standard
     * y formula ( y = r*sin(radians) ) to get the X value. 
     * @param radius
     * @param radians
     * @return
     */
    public static double getCartesianX( double radius, double radians )
    {
        // y = r*sin(radians) SWAPPED on purpose, see javadoc!
        return radius * ( Math.sin( radians ) );
    }

    /**
     * Robocode uses a clockwise direction convention where 
     *      0/360 deg is "North", 90 deg towards "East", 180 towards "South", and 270 deg towards "West".
     * This is a flipped mirror from the usual polar coordinate convention of
     *      0/360 deg is "East", 90 deg towards "North", 180 towards "West", and 270 deg towards "South".
     * This results in the X & Y axis being reversed and the reason for using the standard
     * y formula (  x = r*cos(radians) ) to get the Y value. 
     * @param radius
     * @param radians
     * @return
     */
    public static double getCartesianY( double radius, double radians )
    {
        // x = r*cos(radians) SWAPPED on purpose, see javadoc!
        return radius * ( Math.cos( radians ) );
    }
    
    public static double degreesToRadians( double degrees )
    {
        return (Math.PI/180)*degrees;
    }
    
    public static double radiansToDegree( double radians )
    {
        return (180/Math.PI)*radians;
    }
    
}
