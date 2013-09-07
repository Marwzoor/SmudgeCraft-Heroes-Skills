package net.smudgecraft.heroeslib.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class Utils
{
	public static Vector getTargetVector(Location shooter, Location target)
	{
		Location first_location = shooter.add(0, 1, 0);
		Location second_location = target.add(0, 1, 0);
		Vector vector = second_location.toVector().subtract(first_location.toVector());
		return vector;
		
	}
	
    public static Entity getTarget(Player player)
    {
        List<Entity> nearbyE = player.getNearbyEntities(20, 20, 20);
        ArrayList<Player> nearPlayers = new ArrayList<Player>();
        for (Entity e : nearbyE)
        {
            if (e instanceof Player)
            {
                nearPlayers.add((Player) e);
            }
        }
        Entity target = null;
        BlockIterator bItr = new BlockIterator(player, 20);
        Block block;
        Location loc;
        int bx, by, bz;
        double ex, ey, ez;
        while (bItr.hasNext())
        {
 
            block = bItr.next();
            bx = block.getX();
            by = block.getY();
            bz = block.getZ();
            for (Player e : nearPlayers)
            {
                loc = e.getLocation();
                ex = loc.getX();
                ey = loc.getY();
                ez = loc.getZ();
                if ((bx - .75 <= ex && ex <= bx + 1.75) && (bz - .75 <= ez && ez <= bz + 1.75) && (by - 1 <= ey && ey <= by + 2.5))
                {
                    target = (Entity)e;
                    break;
                }
            }
            if(target == null)
            {
                for (Entity e : nearbyE)
                {
                    loc = e.getLocation();
                    ex = loc.getX();
                    ey = loc.getY();
                    ez = loc.getZ();
                    if ((bx - .75 <= ex && ex <= bx + 1.75) && (bz - .75 <= ez && ez <= bz + 1.75) && (by - 1 <= ey && ey <= by + 2.5))
                    {
                        target = e;
                        break;
                    }
                }            	
            }
        }
        return target;
    }
    
    public static Entity getTarget(Player player, int range)
    {
        List<Entity> nearbyE = player.getNearbyEntities(range, range, range);
        ArrayList<Player> nearPlayers = new ArrayList<Player>();
        for (Entity e : nearbyE)
        {
            if (e instanceof Player)
            {
                nearPlayers.add((Player) e);
            }
        }
        Entity target = null;
        BlockIterator bItr = new BlockIterator(player, 20);
        Block block;
        Location loc;
        int bx, by, bz;
        double ex, ey, ez;
        while (bItr.hasNext())
        {
 
            block = bItr.next();
            bx = block.getX();
            by = block.getY();
            bz = block.getZ();
            for (Player e : nearPlayers)
            {
                loc = e.getLocation();
                ex = loc.getX();
                ey = loc.getY();
                ez = loc.getZ();
                if ((bx - .75 <= ex && ex <= bx + 1.75) && (bz - .75 <= ez && ez <= bz + 1.75) && (by - 1 <= ey && ey <= by + 2.5))
                {
                    target = (Entity)e;
                    break;
                }
            }
            if(target == null)
            {
                for (Entity e : nearbyE)
                {
                    loc = e.getLocation();
                    ex = loc.getX();
                    ey = loc.getY();
                    ez = loc.getZ();
                    if ((bx - .75 <= ex && ex <= bx + 1.75) && (bz - .75 <= ez && ez <= bz + 1.75) && (by - 1 <= ey && ey <= by + 2.5))
                    {
                        target = e;
                        break;
                    }
                }            	
            }
        }
        return target;
    }
}
