package zh;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Random;
import java.util.logging.Level;

import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import zh.logging.MyLogger;

public class Navigator
{
    private static MyLogger logger = MyLogger.getLogger( Navigator.class.getName() );

    static
    {
        logger.setEnabled( true );
        logger.setLevel( Level.FINER );
    }

    private UnderDog robot;
    public boolean startGameProceduresExecuted;
    private boolean NORMAL_DIRECTION = true;
    private double WIDTH;
    private double HEIGHT;
    private double ROBOT_WIDTH_PADDING;
    private double ROBOT_HEIGHT_PADDING;
    private Random randomVelocity = new Random();
    private double lastDistanceSegmenterValue = 1.0D;
    private double currDistanceSegmenterValue = 2.0D;

    int FORWARD = 1;

    public Navigator( UnderDog r )
    {
        this.robot = r;
    }

    public void makeDecision()
    {
    }

    private Point2D getClosestCornerPoint()
    {
        Point2D p = new Point2D.Double( this.robot.getX() > this.WIDTH / 2.0D ? this.WIDTH : 0.0D, this.robot.getY() > this.HEIGHT / 2.0D ? this.HEIGHT : 0.0D );

        p.setLocation( Math.abs( p.getX() - this.ROBOT_WIDTH_PADDING ), Math.abs( p.getY() - this.ROBOT_HEIGHT_PADDING ) );
        return p;
    }

    private void determineHeadingChangeToGoToPoint( Point2D destination )
    {
        determineHeadingChangeToGoToPoint( destination, true );
    }

    private double determineHeadingChangeToGoToPoint( Point2D destination, boolean goToLocation )
    {
        double distance = getDistanceToPointFromCurrent( destination );
        double angle = normalRelativeAngle( absoluteBearing( destination ) - this.robot.getHeading() );

        if ( Math.abs( angle ) > 90.0D )
        {
            distance *= -1.0D;
            if ( angle > 0.0D )
            {
                angle -= 180.0D;
            }
            else
            {
                angle += 180.0D;
            }
        }
        this.robot.setTurnRight( angle );
        if ( goToLocation )
        {
            this.robot.setAhead( distance );
        }

        logger.log( Level.FINE, "setTurnRight(" + angle + ")" );
        logger.log( Level.FINE, "setAhead(" + distance + ")" );

        return distance;
    }

    private void setRobotAheadMovement( double distance, double modByValue )
    {
        double remainder = distance % modByValue;
        double modifiedDistance = distance - remainder;
        boolean remainderUsed = false;
        double tempDist = 0.0D;
        for ( double i = 0.0D; i < modByValue; i += 1.0D )
        {
            if ( !remainderUsed )
            {
                remainderUsed = true;
                tempDist += modifiedDistance / modByValue + remainder;
                this.robot.setAhead( tempDist );
            }
            else
            {
                tempDist += modifiedDistance / modByValue;
                this.robot.setAhead( modifiedDistance / modByValue );
            }
        }
    }

    private double getNextSegmenterValue()
    {
        if ( this.lastDistanceSegmenterValue < this.currDistanceSegmenterValue )
        {
            if ( this.currDistanceSegmenterValue < 4.0D )
            {
                this.lastDistanceSegmenterValue = this.currDistanceSegmenterValue;
                this.currDistanceSegmenterValue += 1.0D;
            }
            else
            {
                this.lastDistanceSegmenterValue = this.currDistanceSegmenterValue;
                this.currDistanceSegmenterValue -= 1.0D;
            }
        }
        else if ( this.lastDistanceSegmenterValue > this.currDistanceSegmenterValue )
        {
            if ( this.currDistanceSegmenterValue > 1.0D )
            {
                this.lastDistanceSegmenterValue = this.currDistanceSegmenterValue;
                this.currDistanceSegmenterValue -= 1.0D;
            }
            else if ( this.currDistanceSegmenterValue <= 1.0D )
            {
                this.lastDistanceSegmenterValue = 0.0D;
            }
        }
        return this.currDistanceSegmenterValue;
    }

    private double getNextRandomVelocity()
    {
        return 50.0D * this.randomVelocity.nextDouble();
    }

    private double absoluteBearing( Point2D target )
    {
        double dx = target.getX() - this.robot.getX();
        double dy = target.getY() - this.robot.getY();

        double theta = Math.toDegrees( Math.atan2( dx, dy ) );
        return theta;
    }

    private double normalRelativeAngle( double angle )
    {
        angle = Math.toRadians( angle );
        double returnvalue = Math.toDegrees( Math.atan2( Math.sin( angle ), Math.cos( angle ) ) );

        return returnvalue;
    }

    private double getDistanceToPointFromCurrent( Point2D otherPoint )
    {
        return Point2D.distance( this.robot.getX(), this.robot.getY(), otherPoint.getX(), otherPoint.getY() );
    }

    public void processEvent( ScannedRobotEvent event )
    {
        if ( this.robot.getDistanceRemaining() == 0.0D )
        {
            this.FORWARD = ( -this.FORWARD );
            double forwardValue = 185.0D * Math.random() * this.FORWARD;
            this.robot.setAhead( forwardValue );
            System.out.println( "forwardValue: " + forwardValue );
        }
        this.robot.setTurnRightRadians( event.getBearingRadians() + 1.570796326794897D - 0.5236D * this.FORWARD * ( event.getDistance() > 200.0D ? 1 : -1 ) );
    }

    public void processEvent( HitByBulletEvent event )
    {
    }

    public void processEvent( HitRobotEvent event )
    {
        Enemy enemy = EnemyManager.getInstance().getEnemy( event.getName() );

        boolean goBackward = false;
        double turnRightAmount = 45.0D;
        if ( ( event.getBearing() > -90.0D ) && ( event.getBearing() < 90.0D ) )
        {
            goBackward = this.robot.getDistanceRemaining() >= 0.0D;
            if ( goBackward )
            {
                turnRightAmount = -45.0D;
            }
        }
        this.robot.setAhead( goBackward ? -200 : 200 );
        if ( this.robot.getTurnRemaining() == 0.0D )
        {
            this.robot.setTurnRight( turnRightAmount );
        }

        this.NORMAL_DIRECTION = ( !this.NORMAL_DIRECTION );
    }

    public void processEvent( HitWallEvent event )
    {
        System.out.println( "hit wall dr: " + this.robot.getDistanceRemaining() );
        if ( this.robot.getDistanceRemaining() == 0.0D )
        {
            this.FORWARD = ( -this.FORWARD );
            double forwardValue = 185.0D * Math.random() * this.FORWARD;
            this.robot.setAhead( forwardValue );
            System.out.println( "forwardValue: " + forwardValue );
        }
    }

    public void processEvent( DeathEvent event )
    {
        reset();
    }

    public void processEvent( WinEvent event )
    {
        reset();
    }

    private void reset()
    {
        this.startGameProceduresExecuted = false;
        this.NORMAL_DIRECTION = true;
    }

    public void onPaint( Graphics2D g )
    {
        g.setColor( Color.RED );
        int CIRCLE_SIZE = 50;
        
        Point2D currentLocation = robot.getLocation();
        g.drawOval( (int)currentLocation.getX(), (int)currentLocation.getY(), CIRCLE_SIZE, CIRCLE_SIZE );
                
//        for ( Point2D pt : this.WAYPOINTLIST )
//        {
//            g.fill( new Ellipse2D.Double( pt.getX() + CIRCLE_SIZE / 2.0D, pt.getY() + CIRCLE_SIZE / 2.0D, CIRCLE_SIZE, CIRCLE_SIZE ) );
//        }
    }

    public static void main( String[] args )
    {
        int y = 0;
        Random randomVelocity = new Random();

        for ( int i = 0; i < 100; i++ )
        {
            System.out.println( "randomVelocity.nextDouble(): " + 3.0D * randomVelocity.nextDouble() );
        }
    }

}

