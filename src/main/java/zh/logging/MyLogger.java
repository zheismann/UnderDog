package zh.logging;

import java.util.logging.Level;

public class MyLogger
{
    private static final Level DEFAULTLEVEL = Level.FINE;
    private boolean ENABLED = true;
    private String name;
    private Level currentLevel = DEFAULTLEVEL;

    private MyLogger( String nm )
    {
        this.name = nm;
    }

    public static synchronized MyLogger getLogger( String name )
    {
        return new MyLogger( name );
    }

    public void log( String message )
    {
        if ( ( isEnabled() ) && ( this.currentLevel.intValue() >= DEFAULTLEVEL.intValue() ) )
        {
            System.err.println( this.name + " : " + this.currentLevel + " DEFAULTLEVEL: " + DEFAULTLEVEL + " : " + message );
        }
    }

    public void log( Level level, String message )
    {
        if ( ( isEnabled() ) && ( level.intValue() <= this.currentLevel.intValue() ) )
        {
            System.err.println( this.name + " : " + level.getName() + " : " + message );
        }
    }

    public boolean isEnabled()
    {
        return this.ENABLED;
    }

    public void setEnabled( boolean e )
    {
        this.ENABLED = e;
    }

    public void setLevel( Level level )
    {
        this.currentLevel = level;
    }
}
