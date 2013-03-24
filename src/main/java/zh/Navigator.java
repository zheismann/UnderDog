package zh;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.text.AttributedString;
import java.util.Random;
import java.util.logging.Level;

import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import zh.logging.MyLogger;

/**
 * Working on Wall Smoothing now: http://robowiki.net/wiki/Wall_Smoothing
 * 
 */
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

    
    private double lastRightTurnRadians = 0;
    public void processEvent( ScannedRobotEvent event )
    {
        if ( this.robot.getDistanceRemaining() == 0.0D )
        {
            this.FORWARD = ( -this.FORWARD );
            double forwardValue = 185.0D * Math.random() * this.FORWARD;
            this.robot.setAhead( forwardValue );
        }
        double rightTurnRadians = event.getBearingRadians() + 1.570796326794897D - 0.5236D * this.FORWARD * ( event.getDistance() > 200.0D ? 1 : -1 );
//        final double WALL_STICK = 150;
//        double testX = this.robot.getX() + Utils.getCartesianX(WALL_STICK, rightTurnRadians );
//        double testY = this.robot.getY() + Utils.getCartesianY(WALL_STICK, rightTurnRadians );
        
        this.lastRightTurnRadians = rightTurnRadians;
        this.robot.setTurnRightRadians( rightTurnRadians );
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
        int bufferCircleDiameter = 150;
        int smallCircleDiameter = 12;
        paintBufferCircleAroundRobot( g, bufferCircleDiameter );
        paintForwardCircle( g, bufferCircleDiameter, smallCircleDiameter );
        paintRightCircle( g, bufferCircleDiameter, smallCircleDiameter );
        paintBackwardCircle( g, bufferCircleDiameter, smallCircleDiameter );
        paintLeftCircle( g, bufferCircleDiameter, smallCircleDiameter );
        paintTurnRemainingLine( g, bufferCircleDiameter, smallCircleDiameter );
        paintDebugLabels( g );
    }

    private void paintTurnRemainingLine( Graphics2D g, int bufferCircleDiameter, int diameter )
    {
        if ( robot.getTurnRemainingRadians() != 0.0D )
        {
            double bufferRadius = bufferCircleDiameter/2.0D;
            double turnRemainingX = Utils.getCartesianX( bufferRadius, robot.getTurnRemainingRadians() );
            double turnRemainingY = Utils.getCartesianY( bufferRadius, robot.getTurnRemainingRadians() );
            int radius = (diameter/2);
            int x = ( int ) ( robot.getX() + turnRemainingX - radius );
            int y = ( int ) ( robot.getY() + turnRemainingY - radius );
            g.setColor( Color.WHITE );
            g.fillOval( x, y, diameter, diameter );
            g.setColor( Color.BLACK );
            g.drawLine( (int)robot.getX(), (int)robot.getY(), (int)(robot.getX() + turnRemainingX), (int)(robot.getY() + turnRemainingY) );
        }        
    }

    private void paintDebugLabels( Graphics2D g )
    {
        int textYvalue = 0;
        int textSpacingConst = 13;
        int halfHeight = ( int ) ( this.robot.getBattlefield().getCenter().getX() );
        int halfWidth = ( int ) ( this.robot.getBattlefield().getCenter().getY() );
        
        g.setColor( Color.WHITE );
        
//        AttributedString attrString = new AttributedString( "Movement direction: " + ( (this.lastRightTurnRadians == 0.0D ) ? "?" : (this.lastRightTurnRadians > 0.0D ) ? "CW" : "C-CW" ) );
        AttributedString attrString = new AttributedString( "lastRightTurnRadians: " + this.lastRightTurnRadians );
        attrString.addAttribute( TextAttribute.WIDTH, TextAttribute.WIDTH_EXTENDED );
        g.drawString( attrString.getIterator(), halfWidth, halfHeight+(textSpacingConst*(++textYvalue)) );

        attrString = new AttributedString( "FORWARD: " + this.FORWARD );
        attrString.addAttribute( TextAttribute.WIDTH, TextAttribute.WIDTH_EXTENDED );
        g.drawString( attrString.getIterator(), halfWidth, halfHeight+(textSpacingConst*(++textYvalue)) );

        attrString = new AttributedString( "turnRemainingRadians: " + this.robot.getTurnRemainingRadians() + "     turnRemainingDegrees: " + this.robot.getTurnRemaining() );
        attrString.addAttribute( TextAttribute.WIDTH, TextAttribute.WIDTH_EXTENDED );
        g.drawString( attrString.getIterator(), halfWidth, halfHeight+(textSpacingConst*(++textYvalue)) );

        attrString = new AttributedString( "headingRadians: " + this.robot.getHeadingRadians() + "     headingDegrees: " + this.robot.getHeading() );
        attrString.addAttribute( TextAttribute.WIDTH, TextAttribute.WIDTH_EXTENDED );
        g.drawString( attrString.getIterator(), halfWidth, halfHeight+(textSpacingConst*(++textYvalue)) );
    }

    private void paintRightCircle( Graphics2D g, int bufferCircleDiameter, int diameter )
    {
        g.setColor( Color.RED );
        double bufferRadius = bufferCircleDiameter/2.0D;
        int radius = (diameter/2);
        int x = ( int ) ( robot.getX() + ( bufferRadius - radius ) );
        int y = ( int ) ( robot.getY() - radius );
        g.drawOval( x, y, diameter, diameter );
    }

    private void paintForwardCircle( Graphics2D g, double bufferCircleDiameter, int diameter )
    {
        g.setColor( Color.PINK );
        double bufferRadius = bufferCircleDiameter/2.0D;
        int radius = (diameter/2);
        
        double headingX = Utils.getCartesianX( bufferRadius, robot.getHeadingRadians() );
        double headingY = Utils.getCartesianY( bufferRadius, robot.getHeadingRadians() );
        int x = ( int ) ( robot.getX() + headingX - radius );
        int y = ( int ) ( robot.getY() + headingY - radius );
        g.fillOval( x, y, diameter, diameter );
        g.setColor( Color.BLACK );
        g.drawLine( (int)robot.getX(), (int)robot.getY(), (int)(robot.getX() + headingX), (int)(robot.getY() + headingY) );
    }

    private void paintLeftCircle( Graphics2D g, double bufferCircleDiameter, int diameter )
    {
        g.setColor( Color.MAGENTA );
        double bufferRadius = bufferCircleDiameter/2.0D;
        int radius = (diameter/2);
        int x = ( int ) ( robot.getX() - bufferRadius - radius );
        int y = ( int ) ( robot.getY() - radius );
        g.drawOval( x, y, diameter, diameter );
    }

    private void paintBackwardCircle( Graphics2D g, double bufferCircleDiameter, int diameter )
    {
        g.setColor( Color.BLUE );
        double bufferRadius = bufferCircleDiameter/2.0D;
        int radius = (diameter/2);

        // Adding 180 degrees so it is opposite of the heading
        double headingX = Utils.getCartesianX( bufferRadius, robot.getHeadingRadians() + Math.toRadians( 180.0D ) );
        double headingY = Utils.getCartesianY( bufferRadius, robot.getHeadingRadians() + Math.toRadians( 180.0D ) );
        int x = ( int ) ( robot.getX() + headingX - radius );
        int y = ( int ) ( robot.getY() + headingY - radius );
        g.fillOval( x, y, diameter, diameter );
        g.setColor( Color.WHITE );
        g.drawLine( (int)robot.getX(), (int)robot.getY(), (int)(robot.getX() + headingX), (int)(robot.getY() + headingY) );
    }

    private void paintBufferCircleAroundRobot( Graphics2D g, double bufferCircleDiameter )
    {
        g.setColor( Color.GREEN );
        double radius = bufferCircleDiameter/2.0D;
        int x = ( int ) ( robot.getX() - radius );
        int y = ( int ) ( robot.getY() - radius );
        g.drawOval( x, y, ( int )bufferCircleDiameter, ( int )bufferCircleDiameter );
    }
    
    
    public static void main( String[] args )
    {
//        int y = 0;
//        Random randomVelocity = new Random();
//
//        for ( int i = 0; i < 100; i++ )
//        {
//            System.out.println( "randomVelocity.nextDouble(): " + 3.0D * randomVelocity.nextDouble() );
//        }
        double[] degreeAngles = new double[]{2.5,5.0, 25.0, 45.0, 90.0, 125.90, 179.0, 180.0, 220.4, 270.0, 293.0, 310.0, 358.0 };
        for ( double angle : degreeAngles )
        {
            System.out.println( "Degrees: " + angle  + "\tNRA(Degrees): " + Utils.radiansToDegree( robocode.util.Utils.normalRelativeAngle( Utils.degreesToRadians( angle ) ) ) + "\tRadians: " + Utils.degreesToRadians( angle ) + "\tNRA(Radians): " + robocode.util.Utils.normalRelativeAngle( Utils.degreesToRadians( angle ) ) );
        }
    }

}

