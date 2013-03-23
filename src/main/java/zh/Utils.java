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
}
