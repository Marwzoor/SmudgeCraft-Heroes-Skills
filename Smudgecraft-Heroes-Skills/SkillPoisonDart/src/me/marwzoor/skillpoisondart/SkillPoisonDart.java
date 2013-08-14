package me.marwzoor.skillpoisondart;

import net.minecraft.server.v1_6_R2.DataWatcher;
import net.minecraft.server.v1_6_R2.EntityLiving;
import net.smudgecraft.heroeslib.events.ImbueArrowHitEvent;
import net.smudgecraft.heroeslib.commoneffects.ImbueEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicDamageEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillPoisonDart extends ActiveSkill
{
	public static Heroes plugin;
	public static SkillPoisonDart skill;
	
	public SkillPoisonDart(Heroes instance)
	{
		super(instance, "PoisonDart");
		plugin=instance;
		skill=this;
		setDescription("Harvest poison from the poison dart frog and apply to your next arrow. The next arrow you fire makes your opponent poisoned for $1 seconds. You have $2 seconds to fire your arrow.");
		setUsage("/skill poisondart");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill poisondart" });
		setTypes(new SkillType[] { SkillType.BUFF, SkillType.MOVEMENT});
		
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
		String desc = super.getDescription();
		double duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(30000), false);
		double poisonduration = SkillConfigManager.getUseSetting(hero, skill, "poison-duration", Integer.valueOf(6000), false);
		poisonduration += SkillConfigManager.getUseSetting(hero, skill, "poison-duration-increase", Integer.valueOf(10), false) * hero.getSkillLevel(skill);
		poisonduration = poisonduration/1000;
		duration = duration/1000;
		desc = desc.replace("$1", poisonduration + "");
		desc = desc.replace("$2", duration + "");
		return desc;
		}
		else
		{
			return getDescription().replace("$1", "X").replace("$2", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(30000));
		node.set("poison-duration", Integer.valueOf(6000));
		node.set("poison-duration-increase", Integer.valueOf(10));
		node.set("poison-damage", Integer.valueOf(20));
		node.set("poison-damage-increase", Integer.valueOf(1));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		int duration = SkillConfigManager.getUseSetting(hero, skill, "duration", Integer.valueOf(30000), false);
		int poisonduration = SkillConfigManager.getUseSetting(hero, skill, "poison-duration", Integer.valueOf(6000), false);
		poisonduration += SkillConfigManager.getUseSetting(hero, skill, "poison-duration-increase", Integer.valueOf(10), false) * hero.getSkillLevel(skill);
		
		int poisondamage = SkillConfigManager.getUseSetting(hero, skill, "poison-damage", Integer.valueOf(20), false);
		poisondamage += SkillConfigManager.getUseSetting(hero, skill, "poison-damage-increase", Integer.valueOf(1), false) * hero.getSkillLevel(skill);
		
		if(hero.hasEffect("PoisonDart"))
		{
			hero.removeEffect(hero.getEffect("PoisonDart"));
		}
		
		PoisonDartEffect pEffect = new PoisonDartEffect(skill, duration, hero.getPlayer(), poisonduration, poisondamage);
		
		hero.addEffect(pEffect);
		
		Messaging.send(hero.getPlayer(), "You used " + ChatColor.WHITE + "PoisonDart" + ChatColor.GRAY + "! Your next arrow will poison your opponent!", new Object());
		
		return SkillResult.NORMAL;
	}
	
	public void addPotionGraphicalEffect(LivingEntity entity, int color, int duration) {
        final EntityLiving el = ((CraftLivingEntity)entity).getHandle();
        final DataWatcher dw = el.getDataWatcher();
        dw.watch(8, Integer.valueOf(color));
 
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
	
	public class SkillHeroListener implements Listener
	{
		@EventHandler
		public void onImbueArrowHitEvent(ImbueArrowHitEvent event)
		{
			if(event.getImbueEffect() instanceof PoisonDartEffect)
			{
				PoisonDartEffect pde = (PoisonDartEffect) event.getImbueEffect();
				int poisondamage = pde.getPoisonDamage();
				int poisonduration = pde.getPoisonDuration();
				
				PoisonEffect pEffect = new PoisonEffect(skill, 2000, poisonduration, poisondamage, pde.getPlayer());
				
				int duration = poisonduration/1000;
				duration = duration*20;
				
				CharacterTemplate ct = plugin.getCharacterManager().getCharacter(event.getShotEntity());
				
				ct.addEffect(pEffect);
				
				addPotionGraphicalEffect(event.getShotEntity(), 0x02A000, duration);
				
				if(pde.getPlayer()!=null)
				{
					Hero hero = plugin.getCharacterManager().getHero(pde.getPlayer());
					
					hero.removeEffect(pde);
				}
			}
		}
	}
	
	public class PoisonDartEffect extends ImbueEffect
	{
		private int damage;
		private int poisonduration;
		public PoisonDartEffect(Skill skill, int duration, Player player, int poisonduration, int damage)
		{
			super(skill, "PoisonDart", duration, player);
			this.poisonduration=poisonduration;
			this.damage=damage;
		}
		
		public int getPoisonDamage()
		{
			return this.damage;
		}
		
		public int getPoisonDuration()
		{
			return this.poisonduration;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero, this);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "You no longer have " + ChatColor.WHITE + "PoisonDart" + ChatColor.GRAY + "!", new Object());
		}
	}
	
	public class PoisonEffect extends PeriodicDamageEffect
	{
		public PoisonEffect(Skill skill, long period, long duration, double damage, Player applier) 
		{
			super(skill, "Poison", period, duration, damage, applier);
		}
		
		public void applyToHero(Hero hero)
		{
			skill.broadcast(hero.getPlayer().getLocation(), hero.getPlayer().getDisplayName() + ChatColor.GRAY + " is poisoned!");
			super.applyToHero(hero);
		}
		
		public void removeFromHero(Hero hero)
		{
			skill.broadcast(hero.getPlayer().getLocation(), hero.getPlayer().getDisplayName() + ChatColor.GRAY  + " is no longer poisoned!");
			super.removeFromHero(hero);
		}
		
		public void applyToMonster(Monster monster)
		{
			skill.broadcast(monster.getEntity().getLocation(), ChatColor.WHITE + monster.getEntity().getType().getName() + ChatColor.GRAY + " is poisoned!");
			super.applyToMonster(monster);
		}
		
		public void removeFromMonster(Monster monster)
		{
			skill.broadcast(monster.getEntity().getLocation(), ChatColor.WHITE + monster.getEntity().getType().getName() + ChatColor.GRAY + " is no longer poisoned!");
			super.removeFromMonster(monster);
		}
	}
}
