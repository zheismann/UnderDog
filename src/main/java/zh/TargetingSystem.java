package zh;

import java.awt.geom.Point2D;

public abstract interface TargetingSystem
{
  public abstract Point2D predictNewLocation(Point2D paramPoint2D1, Point2D paramPoint2D2, double paramDouble1, double paramDouble2, double paramDouble3);
}
