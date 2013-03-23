package zh;

import java.awt.geom.Point2D;
import java.util.logging.Level;

import zh.logging.MyLogger;

public class LinearTargetingSystem
        implements TargetingSystem
{
    private static MyLogger logger = MyLogger.getLogger( LinearTargetingSystem.class.getName() );

    public Point2D predictNewLocation( Point2D myCurLocation, Point2D targetCurLocation, double targetsHeading, double targetsVelocity, double powerOfBullet )
    {
        double time = determineTimeToHitTarget( myCurLocation, targetCurLocation, targetsHeading, targetsVelocity, powerOfBullet );
        return predictNewLocation( targetCurLocation, targetsHeading, targetsVelocity, time );
    }

    private Point2D predictNewLocation( Point2D targetCurLocation, double targetsHeading, double targetsVelocity, double time )
    {
        double x = targetCurLocation.getX() + targetsVelocity * time * Math.sin( Math.toRadians( targetsHeading ) );
        double y = targetCurLocation.getY() + targetsVelocity * time * Math.cos( Math.toRadians( targetsHeading ) );
        logger.log( "predictNewLocation (" + targetsVelocity + " * " + time + " * Math.sin(Math.toRadians(" + targetsHeading + "))): " + Math.abs( targetsVelocity ) * time * Math.sin( Math.toRadians( targetsHeading ) ) + " : " + x );
        logger.log( "predictNewLocation (" + targetsVelocity + " * " + time + " * Math.cos(Math.toRadians(" + targetsHeading + "))): " + Math.abs( targetsVelocity ) * time * Math.cos( Math.toRadians( targetsHeading ) ) + " : " + y );
        return new Point2D.Double( x, y );
    }

    private double determineTimeToHitTarget( Point2D myCurLocation, Point2D targetCurLocation, double targetsHeading, double targetsVelocity, double powerOfBullet )
    {
        double velocityOfBullet = velocityOfBullet( powerOfBullet );
        double currTime = 2.0D;
        double lastTime = 1.0D;
        int iterationCount = 0;

        double lastBulletTargetDiff = getDiffToPredictedLocationAndBulletsTraveledDist( myCurLocation, targetCurLocation, targetsHeading, targetsVelocity, velocityOfBullet, lastTime );

        logger.log( Level.FINE, iterationCount + "  currTime: " + currTime + "  lastTime: " + lastTime + "  lastBulletTargetDiff: " + lastBulletTargetDiff );
        while ( ( Math.abs( currTime - lastTime ) >= 0.005D ) && ( iterationCount < 15 ) )
        {
            iterationCount++;
            double currBulletTargetDiff = getDiffToPredictedLocationAndBulletsTraveledDist( myCurLocation, targetCurLocation, targetsHeading, targetsVelocity, velocityOfBullet, currTime );
            logger.log( Level.FINE, "\tcurrBulletTargetDiff: " + currBulletTargetDiff );
            if ( currBulletTargetDiff - lastBulletTargetDiff == 0.0D )
            {
                break;
            }

            double nextTime = currTime - currBulletTargetDiff * ( currTime - lastTime ) / ( currBulletTargetDiff - lastBulletTargetDiff );

            logger.log( Level.FINE, currTime + " - " + currBulletTargetDiff + "*(" + currTime + "-" + lastTime + ")/(" + currBulletTargetDiff + "-" + lastBulletTargetDiff + ")" );

            lastTime = currTime;
            currTime = nextTime;
            lastBulletTargetDiff = currBulletTargetDiff;
            logger.log( Level.FINE, iterationCount + "  currTime: " + currTime + "  lastTime: " + lastTime + "  lastBulletTargetDiff: " + lastBulletTargetDiff );
            logger.log( Level.FINE, "(Math.abs(" + currTime + " - " + lastTime + ") >= 0.005): " + ( Math.abs( currTime - lastTime ) >= 0.005D ) );
        }
        logger.log( Level.FINE, "RETURN currTime: " + currTime );
        return currTime;
    }

    private double getDiffToPredictedLocationAndBulletsTraveledDist( Point2D myCurLocation, Point2D targetCurLocation, double targetsHeading, double targetsVelocity, double time, double velocityOfBullet )
    {
        Point2D predictedLocation = predictNewLocation( targetCurLocation, targetsHeading, targetsVelocity, time );
        double xLocDiff = predictedLocation.getX() - myCurLocation.getX();
        double yLocDiff = predictedLocation.getY() - myCurLocation.getY();
        logger.log( "\txLocDiff: " + xLocDiff );
        logger.log( "\tyLocDiff: " + yLocDiff );
        logger.log( "\tMath.sqrt((" + xLocDiff + "*" + xLocDiff + ") + (" + yLocDiff + "*" + yLocDiff + ")): " );
        logger.log( Level.FINE, "\t(" + velocityOfBullet + "*" + time + "): " + velocityOfBullet * time );

        return Math.sqrt( xLocDiff * xLocDiff + yLocDiff * yLocDiff ) - velocityOfBullet * time;
    }

    private double velocityOfBullet( double powerOfBullet )
    {
        return 20.0D - 3.0D * powerOfBullet;
    }

    static
    {
        logger.setEnabled( true );
        logger.setLevel( Level.FINER );
    }
}
