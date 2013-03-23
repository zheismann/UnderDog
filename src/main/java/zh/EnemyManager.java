package zh;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import robocode.BulletHitEvent;
import robocode.BulletMissedEvent;
import robocode.DeathEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.WinEvent;
import zh.logging.MyLogger;

public class EnemyManager
{
    private static MyLogger logger = MyLogger.getLogger( EnemyManager.class.getName() );
    static
    {
        logger.setEnabled( false );

        instance = new EnemyManager();
    }
    
    private HashMap<String, Enemy> enemyMap;
    private static final EnemyManager instance;

    private EnemyManager()
    {
        this.enemyMap = new HashMap();
    }

    public static final EnemyManager getInstance()
    {
        return instance;
    }

    public void processEvent( ScannedRobotEvent scanned, Point2D enemyLocation )
    {
        Enemy enemy = null;
        if ( this.enemyMap.containsKey( scanned.getName() ) )
        {
            logger.log( "EXISTING Enemy: " + scanned.getName() );
            enemy = ( Enemy ) this.enemyMap.get( scanned.getName() );
            enemy.updateData( scanned, enemyLocation );
        }
        else
        {
            logger.log( "NEW Enemy: " + scanned.getName() );
            enemy = new Enemy( scanned, enemyLocation );
            this.enemyMap.put( scanned.getName(), enemy );
        }
    }

    public void processEvent( RobotDeathEvent killed )
    {
        if ( this.enemyMap.containsKey( killed.getName() ) )
        {
            this.enemyMap.remove( killed.getName() );
        }
    }

    public void processEvent( DeathEvent event )
    {
        this.enemyMap.clear();
    }

    public void processEvent( WinEvent event )
    {
        this.enemyMap.clear();
    }

    public void processEvent( BulletMissedEvent event )
    {
    }

    public void processEvent( BulletHitEvent event )
    {
        if ( this.enemyMap.containsKey( event.getName() ) )
        {
            Enemy enemy = ( Enemy ) this.enemyMap.get( event.getName() );
            if ( enemy != null )
            {
                enemy.bulletHitMe();
            }
        }
    }

    public boolean hasEnemy()
    {
        return this.enemyMap.keySet().size() > 0;
    }

    public int getNumberOfKnownEnemies()
    {
        return this.enemyMap.keySet().size();
    }

    public Collection<Enemy> getAllEnemies()
    {
        return Collections.unmodifiableCollection( this.enemyMap.values() );
    }

    public Enemy getClosestEnemy()
    {
        Enemy closestEnemy = null;
        for ( String name : this.enemyMap.keySet() )
        {
            Enemy enemy = ( Enemy ) this.enemyMap.get( name );
            if ( ( closestEnemy == null ) || ( enemy.getDistance() < closestEnemy.getDistance() ) )
            {
                closestEnemy = enemy;
            }
        }
        return closestEnemy;
    }

    public Enemy getEnemy( String name )
    {
        return ( Enemy ) this.enemyMap.get( name );
    }
}
