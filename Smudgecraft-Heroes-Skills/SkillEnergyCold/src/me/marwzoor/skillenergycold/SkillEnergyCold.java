package me.marwzoor.skillenergycold;

import net.smudgecraft.heroeslib.util.CuboidArea;
import net.smudgecraft.heroeslib.util.PlayerUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.HeroRegainManaEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillEnergyCold extends ActiveSkill
{
	public SkillEnergyCold(Heroes instance)
	{
		super(instance, "EnergyCold");
		setDescription("Disables all mana gain for %1 seconds within %2 blocks. M: %3 CD: %4 R: %5 D: %6");
		setUsage("/skill energycold");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill energycold" });
		setTypes(new SkillType[] { SkillType.MANA, SkillType.DEBUFF});
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this, plugin), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 0, false);
			int cooldown = SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false)/1000;
			int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 10, false);
			int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false)
					+ (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(this)))/1000;
			
			return super.getDescription().replace("%1", duration + "").replace("%2", radius + "").replace("%3", mana + "").replace("%4", cooldown + "s").replace("%5", radius + "").replace("%6", duration + "s");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "X").replace("%3", "X").replace("%4", "Xs").replace("%5", "X").replace("%6", "Xs");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.MANA.node(), 0);
		node.set(SkillSetting.COOLDOWN.node(), 0);
		node.set(SkillSetting.RADIUS.node(), 10);
		node.set(SkillSetting.DURATION.node(), 10000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 100);
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		if(hero.hasEffect("EnergyCold"))
		{
			hero.removeEffect(hero.getEffect("EnergyCold"));
			return SkillResult.NORMAL;
		}
		
		int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 10, false);
		int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false)
				+ (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(this)));
		
		Location loc = hero.getPlayer().getLocation();
		
		CuboidArea ca = new CuboidArea(loc.clone().add(radius, radius, radius), loc.clone().subtract(radius, radius, radius));
		EnergyColdEffect ecEffect = new EnergyColdEffect(this, duration, radius, ca);
		
		hero.addEffect(ecEffect);
		
		this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " chills enemies with " + ChatColor.WHITE + "EnergyCold" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		private Heroes plugin;
		
		public SkillHeroListener(SkillEnergyCold skill, Heroes plugin)
		{
			this.plugin=plugin;
		}
		
		@EventHandler
		public void onPlayerMoveEvent(PlayerMoveEvent event)
		{
			if(event.isCancelled())
				return;
			
			Hero hero = plugin.getCharacterManager().getHero(event.getPlayer());
			
			if(hero.hasEffect("EnergyCold"))
			{
				EnergyColdEffect ecEffect = (EnergyColdEffect) hero.getEffect("EnergyCold");
				
				Location loc = hero.getPlayer().getLocation();
				
				int radius = ecEffect.getRadius();
				ecEffect.setCuboidArea(new CuboidArea(loc.clone().add(radius, radius, radius), loc.clone().subtract(radius, radius, radius)));
			}
		}
		
		@EventHandler
		public void onHeroRegainManaEvent(HeroRegainManaEvent event)
		{
			if(event.isCancelled())
				return;
			
			for(Player p : event.getHero().getPlayer().getWorld().getPlayers())
			{
				if(!p.equals(event.getHero().getPlayer()) && PlayerUtils.damageCheck(p, event.getHero().getPlayer()))
				{
					Hero hero = plugin.getCharacterManager().getHero(p);
					
					if(hero.hasEffect("EnergyCold"))
					{
						EnergyColdEffect ecEffect = (EnergyColdEffect) hero.getEffect("EnergyCold");
						
						if(ecEffect.getCuboidArea().containsLoc(event.getHero().getPlayer().getLocation()))
						{
							event.setCancelled(true);
							return;
						}
					}
				}
			}
		}
	}
	
	public class EnergyColdEffect extends ExpirableEffect
	{
		private int radius;
		private CuboidArea ca;
		
		public EnergyColdEffect(SkillEnergyCold skill, int duration, int radius, CuboidArea ca)
		{
			super(skill, "EnergyCold", duration);
			this.radius=radius;
			this.ca=ca;
		}
		
		public int getRadius()
		{
			return this.radius;
		}
		
		public CuboidArea getCuboidArea()
		{
			return this.ca;
		}
		
		public void setCuboidArea(CuboidArea ca)
		{
			this.ca=ca;
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			skill.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " is no longer using " + ChatColor.WHITE + "EnergyCold" + ChatColor.GRAY + "!");
		}
	}
}
