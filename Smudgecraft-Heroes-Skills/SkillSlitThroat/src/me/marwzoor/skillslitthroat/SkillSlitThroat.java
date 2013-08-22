package me.marwzoor.skillslitthroat;

import net.smudgecraft.heroeslib.commoneffects.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillSlitThroat extends ActiveSkill
{
	public SkillSlitThroat(Heroes instance)
	{
		super(instance, "SlitThroat");
		setDescription("When you attack your enemy from behind, you have a %1 chance to split their spine, dealing %2 more damage. DMG: %3 M: %4 CD: %5 C: %6");
		setUsage("/skill slitthroat");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill slitthroat" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.MOVEMENT, SkillType.COUNTER });
	
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this, plugin), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
			int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false)/1000;
			
			int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 0, false);
			int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false)/1000);
			
			return super.getDescription().replace("%1", duration + "").replace("%2", duration + "s").replace("%3", cooldown + "s").replace("%4", mana + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X").replace("%2", "Xs").replace("%3", "Xs").replace("%4", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), 10000);
		node.set(SkillSetting.MANA.node(), 0);
		node.set(SkillSetting.COOLDOWN.node(), 0);
		
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false);
		
		if(hero.hasEffect("SlitThroat"))
		{
			hero.removeEffect(hero.getEffect("SlitThroat"));
		}
		
		SlitThroatEffect stEffect = new SlitThroatEffect(this, duration);
		
		hero.addEffect(stEffect);
		
		Messaging.send(hero.getPlayer(), "You are now able to slit your enemy’s throat from behind!");
		this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " used " + ChatColor.WHITE + "SlitThroat" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		private Heroes plugin;
		private SkillSlitThroat skill;
		
		public SkillHeroListener(SkillSlitThroat skill, Heroes plugin)
		{
			this.skill=skill;
			this.plugin=plugin;
		}
		
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(!(event.getEntity() instanceof Player))
				return;
			
			if(!(event.getAttackerEntity() instanceof Player))
				return;
			
			if(event.getEntity().getLocation().getDirection().dot(event.getAttackerEntity().getLocation().getDirection()) <= 0.0D)
				return;
			
			if(!isBlade(((Player)event.getAttackerEntity()).getItemInHand()))
				return;
			
			Hero hero = plugin.getCharacterManager().getHero((Player) event.getAttackerEntity());
			
			if(!hero.hasEffect("SlitThroat"))
				return;
			
			final Player target = (Player) event.getEntity();
			skill.broadcast(target.getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.GRAY + " slit " + ChatColor.DARK_RED + target.getName() + "'s " + ChatColor.GRAY + "throat!");
			Skill.damageEntity(target, hero.getPlayer(), event.getDamage(), DamageCause.MAGIC);
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				public void run()
				{
					target.setHealth(0D);
				}
			});
			
			skill.addSpellTarget(target, hero);
			hero.removeEffect(hero.getEffect("SlitThroat"));
		}
		
		public boolean isBlade(ItemStack is)
		{
			int id = is.getTypeId();
		
			switch(id)
			{
				case 268: return true;
				case 272: return true;
				case 276: return true;
				case 283: return true;
				case 267: return true;
				default: return false;
			}
		}
	}
	
	public class SlitThroatEffect extends ImbueEffect
	{
		public SlitThroatEffect(SkillSlitThroat skill, int duration)
		{
			super(skill, "SlitThroat", duration);
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero, this);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You are no longer able to slit your enemies throat from behind!");
		}
	}
}
