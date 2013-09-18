package net.smudgecraft.heroeslib.util;
//import org.bukkit.ChatColor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.smudgecraft.heroeslib.HeroesLib;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

/**
 *
 * @author Administrator
 */
public class CuboidArea
{
    private Location highPoints;
    private Location lowPoints;
    
    
    protected CuboidArea()
    {
        
    }

    public CuboidArea(Location startLoc, Location endLoc)
    {
        int highx, highy, highz, lowx, lowy, lowz;
        if (startLoc.getBlockX() > endLoc.getBlockX())
        {
            highx = startLoc.getBlockX();
            lowx = endLoc.getBlockX();
        } 
        else
        {
            highx = endLoc.getBlockX();
            lowx = startLoc.getBlockX();
        }
        if (startLoc.getBlockY() > endLoc.getBlockY())
        {
            highy = startLoc.getBlockY();
            lowy = endLoc.getBlockY();
        }
        else
        {
            highy = endLoc.getBlockY();
            lowy = startLoc.getBlockY();
        }
        if (startLoc.getBlockZ() > endLoc.getBlockZ())
        {
            highz = startLoc.getBlockZ();
            lowz = endLoc.getBlockZ();
        }
        else
        {
            highz = endLoc.getBlockZ();
            lowz = startLoc.getBlockZ();
        }
        highPoints = new Location(startLoc.getWorld(),highx,highy,highz);
        lowPoints = new Location(startLoc.getWorld(),lowx,lowy,lowz);
    }
    
    public CuboidArea(Location center, int radius)
    {
    	highPoints = center.clone().add(radius, radius, radius);
    	lowPoints = center.clone().add(-radius, -radius, -radius);
    }

    public boolean isAreaWithinArea(CuboidArea area)
    {
        return (this.containsLoc(area.highPoints) && this.containsLoc(area.lowPoints));
    }

    public boolean containsLoc(Location loc)
    {
        if(loc==null)
            return false;
        if(!loc.getWorld().equals(highPoints.getWorld()))
            return false;
        if(lowPoints.getBlockX() <= loc.getBlockX() && highPoints.getBlockX() >= loc.getBlockX())
        {
            if(lowPoints.getBlockZ() <= loc.getBlockZ() && highPoints.getBlockZ() >= loc.getBlockZ())
            {
                if(lowPoints.getBlockY() <= loc.getBlockY() && highPoints.getBlockY() >= loc.getBlockY())
                {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkCollision(CuboidArea area)
    {
    	if(area.getWorld() == null)
    		return false;
        if(!area.getWorld().equals(this.getWorld()))
            return false;
        if(area.containsLoc(lowPoints) || area.containsLoc(highPoints) || this.containsLoc(area.highPoints) || this.containsLoc(area.lowPoints))
            return true;
        return advCuboidCheckCollision(highPoints, lowPoints,area.highPoints,area.lowPoints);
    }

    private boolean advCuboidCheckCollision(Location A1High, Location A1Low, Location A2High, Location A2Low)
    {
        int A1HX = A1High.getBlockX();
        int A1LX = A1Low.getBlockX();
        int A1HY = A1High.getBlockY();
        int A1LY = A1Low.getBlockY();
        int A1HZ = A1High.getBlockZ();
        int A1LZ = A1Low.getBlockZ();
        int A2HX = A2High.getBlockX();
        int A2LX = A2Low.getBlockX();
        int A2HY = A2High.getBlockY();
        int A2LY = A2Low.getBlockY();
        int A2HZ = A2High.getBlockZ();
        int A2LZ = A2Low.getBlockZ();
        if((A1HX>=A2LX&&A1HX<=A2HX)||(A1LX>=A2LX&&A1LX<=A2HX)||(A2HX>=A1LX&&A2HX<=A1HX)||(A2LX>=A1LX&&A2LX<=A1HX))
            if((A1HY>=A2LY&&A1HY<=A2HY)||(A1LY>=A2LY&&A1LY<=A2HY)||(A2HY>=A1LY&&A2HY<=A1HY)||(A2LY>=A1LY&&A2LY<=A1HY))
                if((A1HZ>=A2LZ&&A1HZ<=A2HZ)||(A1LZ>=A2LZ&&A1LZ<=A2HZ)||(A2HZ>=A1LZ&&A2HZ<=A1HZ)||(A2LZ>=A1LZ&&A2LZ<=A1HZ))
                    return true;
        return false;
    }

    public long getSize()
    {
        int xsize = (highPoints.getBlockX() - lowPoints.getBlockX())+1;
        int ysize = (highPoints.getBlockY() - lowPoints.getBlockY())+1;
        int zsize = (highPoints.getBlockZ() - lowPoints.getBlockZ())+1;
        return xsize * ysize * zsize;
    }

    public int getXSize()
    {
        return (highPoints.getBlockX() - lowPoints.getBlockX())+1;
    }

    public int getYSize()
    {
        return (highPoints.getBlockY() - lowPoints.getBlockY())+1;
    }

    public int getZSize()
    {
        return (highPoints.getBlockZ() - lowPoints.getBlockZ())+1;
    }

    public Location getHighLoc()
    {
        return highPoints;
    }

    public Location getLowLoc()
    {
        return lowPoints;
    }

    public World getWorld()
    {
        return highPoints.getWorld();
    }

    public void save(DataOutputStream out, int version) throws IOException
    {
        out.writeUTF(highPoints.getWorld().getName());
        out.writeInt(highPoints.getBlockX());
        out.writeInt(highPoints.getBlockY());
        out.writeInt(highPoints.getBlockZ());
        out.writeInt(lowPoints.getBlockX());
        out.writeInt(lowPoints.getBlockY());
        out.writeInt(lowPoints.getBlockZ());
    }

    public static CuboidArea load(DataInputStream in, int version) throws IOException
    {
        CuboidArea newArea = new CuboidArea();
        Server server = HeroesLib.getServ();
        World world = server.getWorld(in.readUTF());
        int highx = in.readInt();
        int highy = in.readInt();
        int highz = in.readInt();
        int lowx = in.readInt();
        int lowy = in.readInt();
        int lowz = in.readInt();
        newArea.highPoints = new Location(world, highx, highy, highz);
        newArea.lowPoints = new Location(world, lowx, lowy, lowz);
        return newArea;
    }

    public Map<String,Object> save()
    {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("X1", this.highPoints.getBlockX());
        root.put("Y1", this.highPoints.getBlockY());
        root.put("Z1", this.highPoints.getBlockZ());
        root.put("X2", this.lowPoints.getBlockX());
        root.put("Y2", this.lowPoints.getBlockY());
        root.put("Z2", this.lowPoints.getBlockZ());
        return root;
    }
    
    public static CuboidArea load(Map<String,Object> root, World world) throws Exception
    {
        if(root == null)
            throw new Exception("Invalid residence physical location...");
        CuboidArea newArea = new CuboidArea();
        int x1 = (Integer) root.get("X1");
        int y1 = (Integer) root.get("Y1");
        int z1 = (Integer) root.get("Z1");
        int x2 = (Integer) root.get("X2");
        int y2 = (Integer) root.get("Y2");
        int z2 = (Integer) root.get("Z2");
        newArea.highPoints = new Location(world,x1,y1,z1);
        newArea.lowPoints = new Location(world,x2,y2,z2);
        return newArea;
    }
    
    public Location getRandomLocation()
    {
    	Random object = new Random();
    	int modX = highPoints.getBlockX() - lowPoints.getBlockX();
    	int modY = highPoints.getBlockY() - lowPoints.getBlockY();
    	int modZ = highPoints.getBlockZ() - lowPoints.getBlockZ();
    	int randomX;
    	int randomY;
    	int randomZ;
    	if(modX != 0)
    		randomX = lowPoints.getBlockX() + object.nextInt(modX);
    	else
    		randomX = lowPoints.getBlockX();
    	if(modY != 0)
    		randomY = lowPoints.getBlockY() + object.nextInt(modY);
    	else
    		randomY = lowPoints.getBlockY();
    	if(modZ != 0)
    		randomZ = lowPoints.getBlockZ() + object.nextInt(modZ);
    	else
    		randomZ = lowPoints.getBlockZ();
    	Location location = new Location(getWorld(), randomX, randomY, randomZ);
    	return location;
    }
    
    public List<Location> getLocations()
    {
    	List<Location> locs = new ArrayList<Location>();
    	for(int x = lowPoints.getBlockX();x<=highPoints.getBlockX();++x)
    	{
    		for(int y = lowPoints.getBlockY();y<=highPoints.getBlockY();++y)
        	{
    			for(int z = lowPoints.getBlockZ();z<=highPoints.getBlockZ();++z)
    	    	{
    	    		locs.add(new Location(lowPoints.getWorld(),x,y,z));
    	    	}
        	}
    	}
    	return locs;
    }
    
    public Location getCenter()
    {
    	int xDif = getXSize();
    	int zDif = getYSize();
    	int yDif = getZSize();
    	if(xDif % 2 == 0 || yDif % 2 == 0 || zDif % 2 == 0)
    		return null;
    	return lowPoints.clone().add((xDif/2)+1,(yDif/2)+1, (zDif/2)+1);
    }
}