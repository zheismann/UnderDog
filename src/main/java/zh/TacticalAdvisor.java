package zh;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.geom.Point2D;
import java.text.AttributedString;
import java.util.logging.Level;
import robocode.Bullet;
import robocode.BulletMissedEvent;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.RadarTurnCompleteCondition;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import zh.logging.MyLogger;

public class TacticalAdvisor
{
    private static MyLogger logger = MyLogger.getLogger( TacticalAdvisor.class.getName() );

    static
    {
        logger.setEnabled( true );
        logger.setLevel( Level.FINER );
    }

    private UnderDog robot;
    private static final String ZERO_EVENT = "ZeroRadarSweepLeft";
    private boolean startGameProceduresExecuted = false;
    private RadarTurnCompleteCondition radarTurnCompleteCondition;
    private boolean addEventExecuted = false;
    private long lastTimeScanRadarCalled;
    private Point2D lastTargetedLocation;
    private double lastAngleDiff;
    double CURRENT_RADAR_TURN_DIRECTION = 1.0D;

    public TacticalAdvisor( UnderDog r )
    {
        this.robot = r;
    }

    public void makeDecision()
    {
        if ( !this.startGameProceduresExecuted )
        {
            this.radarTurnCompleteCondition = new RadarTurnCompleteCondition( this.robot );
            this.robot.addCustomEvent( this.radarTurnCompleteCondition );
            this.startGameProceduresExecuted = true;
            logger.log( "makeDecision(): startGameProceduresExecuted " + this.startGameProceduresExecuted );
        }
        if ( ( !EnemyManager.getInstance().hasEnemy() ) && ( this.robot.getRadarTurnRemaining() == 0.0D ) )
        {
            logger.log( "makeDecision(): !EnemyManager.getInstance().hasEnemy()" );
            this.robot.setTurnRadarRight( 360.0D );
        }
        if ( this.robot.getTime() - this.lastTimeScanRadarCalled > 40L )
        {
            new Exception( "**********$********" ).printStackTrace();
        }
    }

    public void processEvent( CustomEvent event )
    {
        if ( ( event.getCondition() instanceof RadarTurnCompleteCondition ) )
        {
            scanRadar();
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
        this.robot.removeCustomEvent( this.radarTurnCompleteCondition );
    }

    public void processEvent( ScannedRobotEvent event )
    {
        if ( this.robot.getGunTurnRemaining() == 0.0D )
        {
            handleAimingAndFiring( event );
        }
    }

    private void scanRadar()
    {
        EnemyManager em = EnemyManager.getInstance();

        double maxRadarTurnAngle = 0.0D;
        for ( Enemy enemy : em.getAllEnemies() )
        {
            double angleFromRadarToRobot = Utils.normalRelativeAngle( this.robot.getHeading() + enemy.getBearing() - this.robot.getRadarHeading() );
            if ( Math.abs( angleFromRadarToRobot ) > Math.abs( maxRadarTurnAngle ) )
            {
                maxRadarTurnAngle = angleFromRadarToRobot;
            }
        }

        double radarTurn = this.CURRENT_RADAR_TURN_DIRECTION * 180.0D;
        if ( ( em.getAllEnemies().size() == 1 ) && ( isRecent( em.getClosestEnemy() ) ) )
        {
            radarTurn = maxRadarTurnAngle + 27.0D * ( maxRadarTurnAngle >= 0.0D ? 1.0D : -1.0D );
        }
        logger.log( Level.FINE, "radarTurn: " + radarTurn );
        this.robot.setTurnRadarRight( radarTurn );
        this.CURRENT_RADAR_TURN_DIRECTION = ( radarTurn >= 0.0D ? 1.0D : -1.0D );
        this.lastTimeScanRadarCalled = this.robot.getTime();
    }

    private boolean isRecent( Enemy enemy )
    {
        if ( this.robot.getTime() - enemy.getLastUpdatedTime() >= 15.0D )
        {
            logger.log( this.robot.getTime() + " " + enemy.getLastUpdatedTime() );
        }
        return this.robot.getTime() - enemy.getLastUpdatedTime() < 15.0D;
    }

    private void handleAimingAndFiring( ScannedRobotEvent event )
    {
        String enemyName = event.getName();
        Enemy closestEnemy = EnemyManager.getInstance().getClosestEnemy();

        if ( ( enemyName == null ) || ( closestEnemy == null ) || ( !enemyName.equals( closestEnemy.getName() ) ) )
        {
            return;
        }
        double distance = event.getDistance();
        double otherRobotsHeading = event.getHeading();
        double enemyVelocity = event.getVelocity();
        Enemy enemy = EnemyManager.getInstance().getEnemy( enemyName );
        double power = 0.1D;
        power = distance < 450.0D ? 0.3D : power;
        power = distance < 350.0D ? 0.5D : power;
        power = distance < 250.0D ? 1.0D : power;
        power = distance < 200.0D ? 2.0D : power;
        power = distance < 150.0D ? 1.0D : power;
        if ( enemy.getHitPercentage() > 0.6D )
        {
            System.out.println( "adding 1 power" );
            power += 1.0D;
        }
        double angleDiff = determineNewGunHeading( distance, enemyVelocity, power, otherRobotsHeading, enemy.getLocation() );
        this.robot.setTurnGunRight( angleDiff );
        logger.log( Level.FINE, "angleDiff: " + angleDiff );
        if ( ( this.robot.getGunHeat() <= 0.5D ) && ( this.robot.getEnergy() >= 3.1D ) )
        {
            Bullet bullet = this.robot.setFireBullet( power );

            enemy.addBulletFiredAtMe();
        }
    }

    private double velocityOfBullet( double powerOfBullet )
    {
        return 20.0D - 3.0D * powerOfBullet;
    }

    private static double determineDegreeAngleToPoint( Point2D currentLocation, Point2D targetsDestination )
    {
        double c = currentLocation.distance( targetsDestination );
        Point2D trueNorthPoint = new Point2D.Double( currentLocation.getX(), currentLocation.getY() + c );
        double b = currentLocation.distance( trueNorthPoint );
        double a = trueNorthPoint.distance( targetsDestination );

        double degreeAngle = Math.toDegrees( Math.acos( ( b * b + c * c - a * a ) / ( 2.0D * b * c ) ) );
        return degreeAngle;
    }

    private double determineNewGunHeading( double distanceToTarget, double targetsVelocity, double powerOfBullet, double targetsHeading, Point2D targetsLocation )
    {
        Point2D currentLocation = this.robot.getLocation();
        double currentGunHeading = this.robot.getGunHeading();
        StringBuilder sb = new StringBuilder( "targetsVelocity: " + targetsVelocity + "  targetsHeading: " + targetsHeading + "  powerOfBullet: " + powerOfBullet + "  " );
        LinearTargetingSystem t = new LinearTargetingSystem();
        Point2D targetsNextLocation = t.predictNewLocation( currentLocation, targetsLocation, targetsHeading, targetsVelocity, powerOfBullet );
        this.lastTargetedLocation = targetsNextLocation;
        sb.append( "\ntargetsNextLocation: " + targetsNextLocation );
        logger.log( Level.FINE, sb.toString() );
        double angle = determineDegreeAngleToPoint( currentLocation, targetsNextLocation );
        if ( this.lastTargetedLocation.getX() < this.robot.getX() )
        {
            angle = 360.0D - angle;
        }
        double angleDiff = angle - this.robot.getGunHeading();
        this.lastAngleDiff = angleDiff;
        logger.log( Level.FINE, "angleDiff: " + angleDiff );
        if ( ( angleDiff > 90.0D ) || ( angleDiff < -90.0D ) )
        {
            angleDiff = Utils.normalRelativeAngle( angleDiff );
        }

        return angleDiff;
    }

    public void processEvent( HitRobotEvent event )
    {
        double turnGunAmt = Utils.normalRelativeAngle( event.getBearing() + this.robot.getHeading() - this.robot.getGunHeading() );
        this.robot.setTurnGunRight( turnGunAmt );
        this.robot.setFire( 3.0D );
    }

    public void processEvent( BulletMissedEvent event )
    {
    }

    public void processEvent( RobotDeathEvent event )
    {
        this.robot.setTurnRadarRight( 360.0D );
    }

    public void processEvent( HitByBulletEvent event )
    {
    }

    public void onPaint( Graphics2D g )
    {
        Enemy enemy = EnemyManager.getInstance().getClosestEnemy();
        if ( enemy != null )
        {
            Point2D loc = enemy.getLocation();
            g.setColor( Color.WHITE );
            g.drawLine( ( int ) this.robot.getX(), ( int ) this.robot.getY(), ( int ) loc.getX(), ( int ) loc.getY() );
            g.setColor( Color.GREEN );
            double gunHeadingRads = this.robot.getGunHeadingRadians();

            double dist = enemy.getDistance();
            g.drawLine( ( int ) this.robot.getX(), ( int ) this.robot.getY(), ( int ) ( Math.toDegrees( Math.sin( gunHeadingRads ) ) * dist ), ( int ) ( Math.toDegrees( Math.cos( gunHeadingRads ) ) * dist ) );
        }

        if ( this.lastTargetedLocation != null )
        {
            g.setColor( Color.RED );
            g.drawLine( ( int ) this.robot.getX(), ( int ) this.robot.getY(), ( int ) this.lastTargetedLocation.getX(), ( int ) this.lastTargetedLocation.getY() );
            double angle = 0.0D;
            g.setColor( Color.WHITE );
            AttributedString attrString = new AttributedString( "lastAngleDiff: " + this.lastAngleDiff );
            attrString.addAttribute( TextAttribute.WIDTH, TextAttribute.WIDTH_EXTENDED );
            int halfHeight = ( int ) ( this.robot.getBattleFieldHeight() / 2.0D );
            int halfWidth = ( int ) ( this.robot.getBattleFieldWidth() / 2.0D );
            attrString.addAttribute( TextAttribute.WIDTH, TextAttribute.WIDTH_EXTENDED );
            g.drawString( attrString.getIterator(), halfWidth, halfHeight );
        }
    }

}
