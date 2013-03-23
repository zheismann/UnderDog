package zh;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.logging.Level;

import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import zh.logging.MyLogger;

public class RobotBrain
{
    private static RobotBrain instance;
    private static MyLogger logger = MyLogger.getLogger( RobotBrain.class.getName() );
    static
    {
        logger.setEnabled( false );
        logger.setLevel( Level.FINE );
    }
    private UnderDog robot;
    private Navigator navigator;
    private TacticalAdvisor tacticalAdvisor;
    private boolean iWon;

    public static final synchronized RobotBrain getInstance( UnderDog r )
    {
        if ( instance == null )
        {
            instance = new RobotBrain( r );
        }
        return instance;
    }

    private RobotBrain( UnderDog r )
    {
        this.robot = r;
        this.navigator = new Navigator( this.robot );
        this.tacticalAdvisor = new TacticalAdvisor( this.robot );
    }

    public void reset()
    {
        this.iWon = false;
    }

    public void makeDecision()
    {
        if ( ( !this.iWon ) && ( this.robot.doOtherOpponentsExist() ) )
        {
            this.tacticalAdvisor.makeDecision();
            this.navigator.makeDecision();
        }
        else if ( this.iWon )
        {
            this.robot.clearAllEvents();
            this.robot.doNothing();
        }
        this.robot.execute();
    }

    public void processEvent( ScannedRobotEvent event )
    {
        double enemyBearing = this.robot.getHeading() + event.getBearing();

        double enemyX = event.getDistance() * Math.sin( Math.toRadians( enemyBearing ) );
        double enemyY = event.getDistance() * Math.cos( Math.toRadians( enemyBearing ) );
        Point2D enemyLocation = new Point2D.Double( this.robot.getX() + enemyX, this.robot.getY() + enemyY );
        logger.log( Level.FINEST, "\nprocessEvent(ScannedRobotEvent event)" );
        logger.log( Level.FINEST, "enemyX: " + enemyX + " enemyY: " + enemyY + "  enemyBearing: " + enemyBearing );
        logger.log( Level.FINEST, "enemyLocation: " + enemyLocation );
        logger.log( Level.FINEST, "robot.getLocation(): " + this.robot.getLocation() );
        logger.log( Level.FINEST, "robot.getHeading(): " + this.robot.getHeading() + "  event.getBearing(): " + event.getBearing() );

        EnemyManager.getInstance().processEvent( event, enemyLocation );
        this.navigator.processEvent( event );
        this.tacticalAdvisor.processEvent( event );
        this.robot.execute();
    }

    public void processEvent( HitRobotEvent event )
    {
        this.tacticalAdvisor.processEvent( event );
        this.navigator.processEvent( event );
        this.robot.execute();
    }

    public void processEvent( HitWallEvent event )
    {
        this.navigator.processEvent( event );
        this.robot.execute();
    }

    public void processEvent( BulletMissedEvent event )
    {
        EnemyManager.getInstance().processEvent( event );
        this.tacticalAdvisor.processEvent( event );
    }

    public void processEvent( BulletHitEvent event )
    {
        EnemyManager.getInstance().processEvent( event );
    }

    public void processEvent( HitByBulletEvent event )
    {
        this.navigator.processEvent( event );
        this.tacticalAdvisor.processEvent( event );
        this.robot.execute();
    }

    public void processEvent( CustomEvent event )
    {
//        this.navigator.processEvent( event );
        this.tacticalAdvisor.processEvent( event );
    }

    public void processEvent( RobotDeathEvent event )
    {
        EnemyManager.getInstance().processEvent( event );
        this.tacticalAdvisor.processEvent( event );
        this.robot.execute();
    }

    public void processEvent( DeathEvent event )
    {
        this.tacticalAdvisor.processEvent( event );
        this.navigator.processEvent( event );
        EnemyManager.getInstance().processEvent( event );
        this.robot.execute();
    }

    public void processEvent( WinEvent event )
    {
        this.iWon = true;
        this.tacticalAdvisor.processEvent( event );
        this.navigator.processEvent( event );
        EnemyManager.getInstance().processEvent( event );
        this.robot.execute();
    }

    public void onPaint( Graphics2D g )
    {
        this.tacticalAdvisor.onPaint( g );
        this.navigator.onPaint( g );
    }

}

 