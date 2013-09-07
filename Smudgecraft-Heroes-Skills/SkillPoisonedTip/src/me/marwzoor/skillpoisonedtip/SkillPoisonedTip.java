package me.marwzoor.skillpoisonedtip;

import net.minecraft.server.v1_6_R2.DataWatcher;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.smudgecraft.heroeslib.whip.WhipDamageEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.effects.PeriodicDamageEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillPoisonedTip extends ActiveSkill
{
	public static SkillPoisonedTip skill;	
	
	public SkillPoisonedTip(Heroes instance)
	{
		super(instance, "PoisonedTip");
		skill=this;
		setDescription("You posion the tip of your whip and fling it at your enemy, dealing $X posion damage every second for $Y seconds. M: $1 CD: $2 D: $3");
		setUsage("/skill poisonedtip");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill poisonedtip" });
		setTypes(new SkillType[] { SkillType.BUFF });
		Bukkit.getPluginManager().registerEvents(new PoisonedTipLinstener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double poisonDamage = (SkillConfigManager.getUseSetting(hero, skill, "poison-damage", 50, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "poison-damage-increase", 0.075, false) * hero.getSkillLevel(skill)));
			double poisonDuration = (SkillConfigManager.getUseSetting(hero, skill, "poison-duration", 10000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "poison-duration-increase", 50, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 60000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
			duration = duration / 1000;
			poisonDuration = poisonDuration / 1000;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", poisonDamage + "").replace("$Y", poisonDuration + "").replace("$1", mana + "").replace("$2", cd + "").replace("$3", duration + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DAMAGE.node(), 50);
		node.set("poison-damage", 50);
		node.set("poison-damage-increase", 0.075);
		node.set("period", 1000);
		node.set("poison-duration", 10000);
		node.set("poison-duration-increase", 50);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double damage = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 60, false);
		double poisonDamage = (SkillConfigManager.getUseSetting(hero, skill, "poison-damage", 50, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "poison-damage-increase", 0.075, false) * hero.getSkillLevel(skill)));
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 60000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 50, false) * hero.getSkillLevel(skill)));
		double poisonDuration = (SkillConfigManager.getUseSetting(hero, skill, "poison-duration", 10000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "poison-duration-increase", 50, false) * hero.getSkillLevel(skill)));
		double period = SkillConfigManager.getUseSetting(hero, skill, "period", 60, false);
		hero.addEffect(new PoisonedTipEffect(this, (long) duration, damage, hero.getPlayer(), poisonDamage, (long)period, (long)poisonDuration));
		Messaging.send(hero.getPlayer(), "You have poisoned the tip of your whip!");
		return SkillResult.NORMAL;
	}
	
	public class PoisonedTipEffect extends ExpirableEffect
	{
		public final PoisonedTipDamageEffect ptde;
		public final double damage;
		
		public PoisonedTipEffect(Skill skill, long duration, double damage, Player applier, double poisonDamage, long period, long poisonDuration)
		{
			super(skill, "PoisonedTip", duration);
			ptde = new PoisonedTipDamageEffect(skill, period, poisonDuration, poisonDamage, applier, true);
			this.damage = damage;
		}
		
		@Override
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero);
		}
		
		@Override
		public void applyToMonster(Monster monster)
		{
			super.applyToMonster(monster);
		}
		
		@Override
		public void removeFromHero(Hero hero)
		{
			Messaging.send(hero.getPlayer(), "The tip of your whip is no longer poisoned!");
			super.removeFromHero(hero);
		}
	}
	
	public class PoisonedTipDamageEffect extends PeriodicDamageEffect
	{
		private boolean particleEffects;
		
		public PoisonedTipDamageEffect(Skill skill, long period, long duration, double damage, Player applier, boolean particleEffects) 
		{
			super(skill, "PoisonedTipDamage", period, duration, damage, applier);
			this.particleEffects=particleEffects;
		}
		
		public PoisonedTipDamageEffect(Skill skill, long period, long duration, double damage, Player applier) 
		{
			super(skill, "PoisonedTipDamage", period, duration, damage, applier);
			this.particleEffects=true;
		}
		
		public void applyToHero(Hero hero)
		{
			skill.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() +
					ChatColor.GRAY + " was struck by " + ChatColor.DARK_RED + applier.getName() + ChatColor.WHITE + "'s" + ChatColor.GRAY + " poisoned whip!");
			if(particleEffects)
				addPotionGraphicalEffect(hero.getEntity(), 0x02A000, this.getDuration());
			super.applyToHero(hero);
		}
		
		/*public void removeFromHero(Hero hero)
		{
			skill.broadcast(hero.getPlayer().getLocation(), hero.getPlayer().getDisplayName() + ChatColor.GRAY  + " is no longer poisoned!");
			super.removeFromHero(hero);
		}
		
		public void applyToMonster(Monster monster)
		{
			skill.broadcast(monster.getEntity().getLocation(), ChatColor.WHITE + monster.getEntity().getType().getName() + ChatColor.GRAY + " is poisoned!");
			if(particleEffects)
				addPotionGraphicalEffect(monster.getEntity(), 0x02A000, this.getDuration());
			super.applyToMonster(monster);
		}
		
		public void removeFromMonster(Monster monster)
		{
			skill.broadcast(monster.getEntity().getLocation(), ChatColor.WHITE + monster.getEntity().getType().getName() + ChatColor.GRAY + " is no longer poisoned!");
			super.removeFromMonster(monster);
		}*/
		
		private void addPotionGraphicalEffect(LivingEntity entity, int color, long duration)
		{
	        final EntityLiving el = ((CraftLivingEntity)entity).getHandle();
	        final DataWatcher dw = el.getDataWatcher();
	        dw.watch(8, (byte) color);
	 
	        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	            public void run() {
	                int c = 0;
	                if (!el.effects.isEmpty()) {
	                    c = net.minecraft.server.v1_6_R2.PotionBrewer.a(el.effects.values());
	                }
	                dw.watch(8, Integer.valueOf(c));
	            }
	        }, duration);
	    }
	}
	
	public class PoisonedTipLinstener implements Listener
	{
		@EventHandler(priority = EventPriority.HIGH)
		public void onWhipDamageEvent(WhipDamageEvent event)
		{
			Hero hero = plugin.getCharacterManager().getHero(event.getAttacker());
			if(!hero.hasEffect("PoisonedTip"))
				return;
			PoisonedTipEffect pte = (PoisonedTipEffect)hero.getEffect("PoisonedTip");
			LivingEntity target = event.getTarget();
			skill.damageEntity(target, hero.getPlayer(), pte.damage);
			plugin.getCharacterManager().getCharacter(target).addEffect(pte.ptde);
		}
	}
}
