package zh;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.logging.Level;

import robocode.ScannedRobotEvent;
import zh.logging.MyLogger;

public class Enemy
{
    private static MyLogger logger = MyLogger.getLogger( Enemy.class.getName() );
    private long lastEventReceivedTime;
    private final String name;
    private ScannedRobotEvent data;
    private static final int HISTORY_SIZE = 5;
    private int numTimesUpdated = 0;
    private Point2D[] positions = new Point2D[5];
    private double[] distances = new double[5];
    private static Point2D ZERO;
    private double numBulletsFiredAtMe;
    private double numBulletsHitMe;
    private int LIMIT = 0;
    static
    {
        logger.setLevel( Level.FINER );

        ZERO = new Point2D.Double( 0.0D, 0.0D );
    }


    public Enemy( ScannedRobotEvent event, Point2D enemyLocation )
    {
        Arrays.fill( this.positions, ZERO );
        this.name = event.getName();
        updateData( event, enemyLocation );
    }

    public void updateData( ScannedRobotEvent event, Point2D enemyLocation )
    {
        if ( event.getTime() < this.lastEventReceivedTime )
        {
            logger.log( Level.WARNING, "Old data from an event" );
            return;
        }
        this.numTimesUpdated += 1;
        this.data = event;
        this.lastEventReceivedTime = event.getTime();

        Point2D tmp = null;
        double dbl = 0.0D;
        for ( int i = 4; i > 0; i-- )
        {
            tmp = this.positions[( i - 1 )];
            this.positions[i] = tmp;
            dbl = this.distances[( i - 1 )];
            this.distances[i] = dbl;
        }
        this.positions[0] = enemyLocation;
        this.distances[0] = this.data.getDistance();
    }

    public double getSlope()
    {
        double slope = 0.0D;
        Point2D a = this.positions[0];
        Point2D b = this.positions[1];
        double yDiff = a.getY() - b.getY();
        double xDiff = a.getX() - b.getX();
        if ( yDiff != 0.0D )
        {
            slope = yDiff / xDiff;
        }

        double yIntercept = b.getY();

        return slope;
    }

    public int getNumTimesUpdated()
    {
        return this.numTimesUpdated;
    }

    public String getName()
    {
        return this.name;
    }

    public double getBearing()
    {
        return this.data.getBearing();
    }

    public boolean equals( Object o )
    {
        if ( ( o == null ) || ( !( o instanceof Enemy ) ) )
        {
            return false;
        }
        return this.name.equals( ( ( Enemy ) o ).getName() );
    }

    public int hashCode()
    {
        return this.name.hashCode();
    }

    public double getDistance()
    {
        return this.data.getDistance();
    }

    public double getLastUpdatedTime()
    {
        return this.data.getTime();
    }

    public double getEnergy()
    {
        return this.data.getEnergy();
    }

    public Point2D getLocation()
    {
        return this.positions[0];
    }

    public void addBulletFiredAtMe()
    {
        this.numBulletsFiredAtMe += 1.0D;
    }

    public void bulletHitMe()
    {
        this.numBulletsHitMe += 1.0D;
    }

    public double getHitPercentage()
    {
        double hitPercentage = this.numBulletsHitMe / ( this.numBulletsFiredAtMe != 0.0D ? this.numBulletsFiredAtMe : 1.0D );
        logger.log( Level.FINE, "hitPercentage: " + hitPercentage + " " + this.numBulletsHitMe + "/(" + ( this.numBulletsFiredAtMe != 0.0D ? this.numBulletsFiredAtMe : 1.0D ) + ")" );
        return hitPercentage;
    }

}
