package net.smudgecraft.companions;

import java.io.File;

import net.smudgecraft.companions.imbuearrows.ArrowListener;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.java.JavaPlugin;

import com.herocraftonline.heroes.Heroes;

public class Companions extends JavaPlugin
{
	public static ComWolves cwolves;
	public static Companions plugin;
	public static Heroes heroes;
	
	@Override
	public void onEnable()
	{
		plugin=this;
		heroes = (Heroes) Bukkit.getPluginManager().getPlugin("Heroes");
		if(heroes==null)
		{
			return;
		}
		cwolves = new ComWolves();
		Bukkit.getPluginManager().registerEvents(new CompanionListener(), plugin);
		Bukkit.getPluginManager().registerEvents(new ArrowListener(), plugin);
		killWolves(WolfType.TAMEDWOLF);
		//loadComWolves();
		//loadWolves();
		
		File folder = this.getDataFolder();
		
		File file = new File(this.getDataFolder() + "/companions.yml");
		
		if(!folder.exists())
		{
			try
			{
				folder.mkdir();
			}
			catch(Exception e)
			{
				
			}
		}
		
		if(!file.exists())
		{
			try
			{
				file.createNewFile();
			}
			catch(Exception e)
			{
				
			}
		}
	}
	
	@Override
	public void onDisable()
	{
		saveWolves();
		killWolves(WolfType.COMWOLF);
		killWolves(WolfType.TAMEDWOLF);
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if(sender instanceof Player)
		{
			Player player = (Player) sender;
			
			if(commandLabel.equalsIgnoreCase("wjump"))
			{
				if(cwolves.hasWolf(player))
				{
					ComWolf cwolf = cwolves.getComWolf(player);
					
					cwolf.jump();
				}
			}
			else if(commandLabel.equalsIgnoreCase("wcreate"))
			{
				if(cwolves.hasWolf(player))
				{
					ComWolf cwolf = cwolves.getComWolf(player);
					cwolf.kill();
					cwolves.removeComWolf(cwolf);
				}
				
				spawnNewComWolf(player.getLocation(), player.getName(), 240, 40, 240, "§e§lAslan", DyeColor.MAGENTA);
			}
		}
		return true;
	}
	
	public static void loadWolves()
	{
		int number=0;
		for(World w : Bukkit.getWorlds())
		{
			final World world = w;
			++number;
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					for(Entity e : world.getEntities())
					{
						if(e instanceof Wolf)
						{
							Wolf wolf = (Wolf) e;
							if(wolf.isTamed())
							{
								OfflinePlayer player = (OfflinePlayer) wolf.getOwner();
								
								ComWolf cwolf = new ComWolf(wolf, player.getName());
								
								cwolves.addComWolf(cwolf);
							}
						}
					}
				}
			}, 2L*number);
		}
	}
	
	public static void loadComWolves()
	{
		File file = new File(plugin.getDataFolder() + "/companions.yml");
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		ConfigurationSection players = config.getConfigurationSection("players");
		
		for(String pname : players.getKeys(true))
		{
			if(!pname.contains("."))
			{
			Location loc = new Location(Bukkit.getWorld(players.getString(pname + ".wolf.location.world")), players.getInt(pname + ".wolf.location.x"), players.getInt(pname + ".wolf.location.y"), players.getInt(pname + ".wolf.location.z"));
			int health = players.getInt(pname + ".wolf.health");
			int maxhealth = players.getInt(pname + ".wolf.maxhealth");
			String name = players.getString(pname + ".wolf.name");
			String owner = pname;
			int damage = players.getInt(pname + ".wolf.damage");
			
			spawnNewComWolf(loc, owner, maxhealth, damage, health, name, DyeColor.BLUE);
			}
		}
	}
	
	public static ComWolf spawnNewComWolf(Location loc, String owner, int maxHealth, int damage, int health, String name, DyeColor dc)
	{
		Wolf wolf = loc.getWorld().spawn(loc, Wolf.class);
		
		wolf.setMaxHealth(maxHealth);
		wolf.setHealth(health);
		wolf.setCustomName(name);
		wolf.setCustomNameVisible(true);
		wolf.setOwner(Bukkit.getOfflinePlayer(owner));
		wolf.setCollarColor(dc);
		
		ComWolf cwolf = new ComWolf(wolf, owner, name);
		cwolf.setDamage(damage);
		cwolf.setName(name);
		
		cwolves.addComWolf(cwolf);
		
		return cwolf;
	}
	
	public static ComWolf spawnNewComWolf(Location loc, String owner, int maxHealth, int damage, int health, String name)
	{
		Wolf wolf = loc.getWorld().spawn(loc, Wolf.class);
		
		wolf.setMaxHealth(maxHealth);
		wolf.setHealth(health);
		wolf.setCustomName(name);
		wolf.setCustomNameVisible(true);
		wolf.setOwner(Bukkit.getOfflinePlayer(owner));
		
		ComWolf cwolf = new ComWolf(wolf, owner, name);
		cwolf.setDamage(damage);
		cwolf.setName(name);
		
		cwolves.addComWolf(cwolf);
		
		return cwolf;
	}
	
	public static ComWolf spawnNewComWolf(Location loc, String owner, int maxHealth, int damage, int health)
	{
		Wolf wolf = loc.getWorld().spawn(loc, Wolf.class);
		
		wolf.setMaxHealth(maxHealth);
		wolf.setHealth(health);
		wolf.setCustomNameVisible(true);
		wolf.setOwner(Bukkit.getOfflinePlayer(owner));
		
		ComWolf cwolf = new ComWolf(wolf, owner);
		cwolf.setDamage(damage);
		
		cwolves.addComWolf(cwolf);
		
		return cwolf;
	}
	
	public static ComWolf spawnNewComWolf(Location loc, String owner, int maxHealth, int health)
	{
		Wolf wolf = loc.getWorld().spawn(loc, Wolf.class);
		
		wolf.setMaxHealth(maxHealth);
		wolf.setHealth(health);
		wolf.setCustomNameVisible(true);
		wolf.setOwner(Bukkit.getOfflinePlayer(owner));
		
		ComWolf cwolf = new ComWolf(wolf, owner);
		
		cwolves.addComWolf(cwolf);
		
		return cwolf;
	}
	
	public static ComWolf spawnNewComWolf(Location loc, String owner, int maxHealth)
	{
		Wolf wolf = loc.getWorld().spawn(loc, Wolf.class);
		
		wolf.setMaxHealth(maxHealth);
		wolf.setHealth(maxHealth);
		wolf.setCustomNameVisible(true);
		wolf.setOwner(Bukkit.getOfflinePlayer(owner));
		
		ComWolf cwolf = new ComWolf(wolf, owner);
		
		cwolves.addComWolf(cwolf);
		
		return cwolf;
	}
	
	public static void killWolves(WolfType wt)
	{
		cwolves.killWolves(wt);
	}
	
	public static void saveWolf(ComWolf cwolf)
	{
		File folder = plugin.getDataFolder();
		
		File file = new File(plugin.getDataFolder() + "/companions.yml");
		
		boolean isFirstTime=false;
		
		if(!folder.exists())
		{
			try
			{
				folder.mkdir();
			}
			catch(Exception e)
			{
				
			}
		}
		
		if(!file.exists())
		{
			try
			{
				file.createNewFile();
				isFirstTime=true;
			}
			catch(Exception e)
			{
				
			}
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		ConfigurationSection players;
		
		if(isFirstTime)
		{
			players = config.createSection("players");
		}
		else
		{
			if(config.contains("players"))
			{
				players = config.getConfigurationSection("players");
			}
			else
			{
				players = config.createSection("players");
			}
		}
		
		players.set(cwolf.getWolfOwnerName() + ".wolf.location.world", cwolf.getWolf().getLocation().getWorld().getName());
		players.set(cwolf.getWolfOwnerName() + ".wolf.location.x", cwolf.getWolf().getLocation().getX());
		players.set(cwolf.getWolfOwnerName() + ".wolf.location.y", cwolf.getWolf().getLocation().getY());
		players.set(cwolf.getWolfOwnerName() + ".wolf.location.z", cwolf.getWolf().getLocation().getZ());
		players.set(cwolf.getWolfOwnerName() + ".wolf.maxhealth", cwolf.getWolf().getMaxHealth());
		players.set(cwolf.getWolfOwnerName() + ".wolf.health", cwolf.getWolf().getHealth());
		players.set(cwolf.getWolfOwnerName() + ".wolf.damage", cwolf.getDamage());
		players.set(cwolf.getWolfOwnerName() + ".wolf.name", cwolf.getName());
		
		try
		{
			config.save(file);
		}
		catch(Exception e)
		{
			
		}
	}
	
	public static void saveWolves()
	{
		File folder = plugin.getDataFolder();
		
		File file = new File(plugin.getDataFolder() + "/companions.yml");
		
		boolean isFirstTime=false;
		
		if(!folder.exists())
		{
			try
			{
				folder.mkdir();
			}
			catch(Exception e)
			{
				
			}
		}
		
		if(!file.exists())
		{
			try
			{
				file.createNewFile();
				isFirstTime=true;
			}
			catch(Exception e)
			{
				
			}
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		ConfigurationSection players;
		
		if(isFirstTime)
		{
			players = config.createSection("players");
		}
		else
		{
			players = config.getConfigurationSection("players");
		}
		
		for(ComWolf cwolf : cwolves.getComWolves())
		{
			players.set(cwolf.getWolfOwnerName() + ".wolf.location.world", cwolf.getWolf().getLocation().getWorld().getName());
			players.set(cwolf.getWolfOwnerName() + ".wolf.location.x", cwolf.getWolf().getLocation().getX());
			players.set(cwolf.getWolfOwnerName() + ".wolf.location.y", cwolf.getWolf().getLocation().getY());
			players.set(cwolf.getWolfOwnerName() + ".wolf.location.z", cwolf.getWolf().getLocation().getZ());
			players.set(cwolf.getWolfOwnerName() + ".wolf.maxhealth", cwolf.getWolf().getMaxHealth());
			players.set(cwolf.getWolfOwnerName() + ".wolf.health", cwolf.getWolf().getHealth());
			players.set(cwolf.getWolfOwnerName() + ".wolf.damage", cwolf.getDamage());
			players.set(cwolf.getWolfOwnerName() + ".wolf.name", cwolf.getName());
		}
		
		try
		{
			config.save(file);
		}
		catch(Exception e)
		{
			
		}
	}
	
	public static enum WolfType
	{
		COMWOLF(1),
		TAMEDWOLF(2),
		WILDWOLF(3);
		
		private int id;
		
		private WolfType(int id)
		{
			this.id=id;
		}
		
		public int getId()
		{
			return this.id;
		}
		
		public static WolfType getById(int id)
		{
			for(WolfType wt : WolfType.values())
			{
				if(wt.getId()==id)
				{
					return wt;
				}
			}
			return null;
		}
	}
	
	public static FileConfiguration getFileConfig()
	{
		File file = new File(plugin.getDataFolder() + "/companions.yml");
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		return config;
	}
	
}
