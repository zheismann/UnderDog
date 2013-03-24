package zh;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.SkippedTurnEvent;
import robocode.WinEvent;
import zh.logging.MyLogger;

public class UnderDog extends AdvancedRobot
{
    private static MyLogger logger = MyLogger.getLogger( UnderDog.class.getName() );
    private final RobotBrain myBrain;
    private Battlefield battlefield;
    private boolean hasPlayedClip = false;
    private static final int EXTERNAL_BUFFER_SIZE = 128000;

    public UnderDog()
    {
        this.myBrain = RobotBrain.getInstance( this );
    }

    public void run()
    {
        // can't be called in the constructor
        this.battlefield = new Battlefield( this );
        
        this.myBrain.reset();
        if ( ( getOthers() == 1 ) && ( !this.hasPlayedClip ) )
        {
            this.hasPlayedClip = true;
        }

        setRadarColor( Color.red );
        setGunColor( Color.yellow );
        setBodyColor( Color.yellow );
        setScanColor( Color.yellow );
        setBulletColor( Color.yellow );
        setAdjustGunForRobotTurn( true );
        while ( true )
        {
            this.myBrain.makeDecision();
        }
    }

    public boolean isGoingForward()
    {
        return getDistanceRemaining() >= 0.0D;
    }

    public void onScannedRobot( ScannedRobotEvent event )
    {
        this.myBrain.processEvent( event );
    }

    public void onHitByBullet( HitByBulletEvent event )
    {
        this.myBrain.processEvent( event );
    }

    public void onHitRobot( HitRobotEvent event )
    {
        this.myBrain.processEvent( event );
    }

    public void onHitWall( HitWallEvent event )
    {
        this.myBrain.processEvent( event );
    }

    public void onBulletMissed( BulletMissedEvent event )
    {
        this.myBrain.processEvent( event );
    }

    public void onBulletHit( BulletHitEvent event )
    {
        this.myBrain.processEvent( event );
    }

    public void onCustomEvent( CustomEvent event )
    {
        this.myBrain.processEvent( event );
    }

    public void onRobotDeath( RobotDeathEvent event )
    {
        this.myBrain.processEvent( event );
    }

    public void onDeath( DeathEvent event )
    {
        this.myBrain.processEvent( event );
    }

    public void onWin( WinEvent event )
    {
        this.myBrain.processEvent( event );
    }

    public void onSkippedTurn( SkippedTurnEvent event )
    {
        System.out.println( "WHOOPSTakinto   LONG!" );
    }

    public boolean doOtherOpponentsExist()
    {
        return 0 < getOthers();
    }

    public Point2D getLocation()
    {
        return new Point2D.Double( getX(), getY() );
    }

    public Battlefield getBattlefield()
    {
        return battlefield;
    }
    
    private void playClip()
    {
        try
        {
            File dataDir = getDataDirectory();
            File file = new File( dataDir, "dangw.wav" );
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream( file );
            AudioFormat audioFormat = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info( SourceDataLine.class, audioFormat );
            SourceDataLine line = ( SourceDataLine ) AudioSystem.getLine( info );
            line.open( audioFormat );
            line.start();

            int nBytesRead = 0;
            byte[] abData = new byte[128000];
            int nBytesWritten;
            while ( nBytesRead != -1 )
            {
                try
                {
                    nBytesRead = audioInputStream.read( abData, 0, abData.length );
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
                if ( nBytesRead < 0 )
                {
                    continue;
                }
                nBytesWritten = line.write( abData, 0, nBytesRead );
            }

            line.drain();
            line.close();
        }
        catch ( Exception ex )
        {
            ex.printStackTrace();
        }
    }

    public void onPaint( Graphics2D g )
    {
        this.myBrain.onPaint( g );
    }
}
