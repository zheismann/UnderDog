// Copyright 2013 Epsilon Data Management, LLC.  All rights reserved.
package zh;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import robocode.AdvancedRobot;

/**
 * @author zheismann
 */
public class Battlefield
{
    private double width;
    private double height;
    private Point2D.Double center;

    /**
     * This is a rectangle that represents an 800x600 battle field,
     * used for a simple, iterative WallSmoothing method (by PEZ).
     * If you're not familiar with WallSmoothing, the wall stick indicates
     * the amount of space we try to always have on either end of the tank
     * (extending straight out the front or back) before touching a wall.
     */
    private double WALL_BUFFER_PIXELS = 18;
    private Rectangle2D.Double bufferedBattefieldRect;

    public Battlefield( AdvancedRobot robot )
    {
        this.width = robot.getBattleFieldWidth();
        this.height = robot.getBattleFieldHeight();
        this.bufferedBattefieldRect = new java.awt.geom.Rectangle2D.Double(WALL_BUFFER_PIXELS, WALL_BUFFER_PIXELS, width-(WALL_BUFFER_PIXELS*2), height-(WALL_BUFFER_PIXELS*2));
        center = new Point2D.Double( width/2.0, height/2.0 );
    }

    public double getWidth()
    {
        return width;
    }

    public double getHeight()
    {
        return height;
    }

    public Point2D.Double getCenter()
    {
        return center;
    }

    public double wallSmoothing( Point2D.Double botLocation, double angle, int orientation )
    {
//        while ( !bufferedBattefieldRect.contains( WaveUtils.project( botLocation, angle, WaveUtils.WALL_STICK ) ) )
//        {
//            angle += orientation * 0.05;
//        }
        return angle;
    }

    public Rectangle2D.Double getBufferedBattefieldRect()
    {
        return bufferedBattefieldRect;
    }

}
