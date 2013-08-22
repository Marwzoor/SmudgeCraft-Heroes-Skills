package me.marwzoor.skillenvenom;

import net.smudgecraft.heroeslib.commoneffects.ImbueEffect;
import net.smudgecraft.heroeslib.commoneffects.PoisonEffect;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillEnvenom extends ActiveSkill
{
	public SkillEnvenom(Heroes instance)
	{
		super(instance, "Envenom");
		setDescription("Your next attack from behind slits the throat of your enemy, instantly killing them. The effect wears off after %1 seconds. D: %2 CD: %3 M: %4");
		setIdentifiers(new String[] { "skill envenom" });
		setArgumentRange(0, 0);
		setTypes(new SkillType[] { SkillType.BUFF });
		
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
		int poisonDuration = SkillConfigManager.getUseSetting(hero, this, "poison-duration", 10000, false);
		int poisonPeriod = SkillConfigManager.getUseSetting(hero, this, "poison-period", 1000, false);
		
		double poisonDamage = (SkillConfigManager.getUseSetting(hero, this, "poison-damage", 30D, false)
				+ (SkillConfigManager.getUseSetting(hero, this, "poison-damage-increase", 0.2D, false) * hero.getSkillLevel(this)));
		
		if(hero.hasEffect("Envenom"))
		{
			hero.removeEffect(hero.getEffect("Envenom"));
		}
		
		EnvenomEffect evEffect = new EnvenomEffect(this, duration, poisonDuration, poisonPeriod, poisonDamage);
		
		hero.addEffect(evEffect);
		
		Messaging.send(hero.getPlayer(), "You imbued your blade with" + ChatColor.DARK_GREEN + " Venom" + ChatColor.GRAY + "! The next opponent you hit will be poisoned!");
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		private SkillEnvenom skill;
		private Heroes plugin;
		
		public SkillHeroListener(SkillEnvenom skill, Heroes plugin)
		{
			this.skill=skill;
			this.plugin=plugin;
		}
		
		@EventHandler
		public void onWeaponDamageEvent(WeaponDamageEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(!(event.getEntity() instanceof LivingEntity))
				return;
			
			if(!(event.getAttackerEntity() instanceof Player))
				return;
			
			ItemStack is = ((Player) event.getAttackerEntity()).getItemInHand();
			
			if(is==null)
				return;
			
			if(!isBlade(is))
				return;
			
			Hero hero = plugin.getCharacterManager().getHero((Player) event.getAttackerEntity());
			
			if(!hero.hasEffect("Envenom"))
				return;
			
			EnvenomEffect evEffect = (EnvenomEffect) hero.getEffect("Envenom");
			PoisonEffect pEffect = new PoisonEffect(skill, evEffect.getPoisonPeriod(), evEffect.getPoisonDuration(), evEffect.getPoisonDamage(), (Player) event.getAttackerEntity());
			
			CharacterTemplate cTemplate = plugin.getCharacterManager().getCharacter((LivingEntity)event.getEntity());
			
			if(cTemplate.hasEffect("Poison"))
			{
				cTemplate.removeEffect(cTemplate.getEffect("Poison"));
			}
			
			cTemplate.addEffect(pEffect);
			hero.removeEffect(evEffect);
		}
	}
	
	public class EnvenomEffect extends ImbueEffect
	{
		private int poisonDuration;
		private int poisonPeriod;
		private double poisonDamage;
		
		public EnvenomEffect(SkillEnvenom skill, int duration, int poisonDuration, int poisonPeriod, double poisonDamage)
		{
			super(skill, "Envenom", duration);
			this.poisonDuration=poisonDuration;
			this.poisonPeriod=poisonPeriod;
			this.poisonDamage=poisonDamage;
		}
		
		public int getPoisonDuration()
		{
			return this.poisonDuration;
		}
		
		public int getPoisonPeriod()
		{
			return this.poisonPeriod;
		}
		
		public double getPoisonDamage()
		{
			return this.poisonDamage;
		}
		
		public void applyToHero(Hero hero)
		{
			super.applyToHero(hero, this);
		}
		
		public void removeFromHero(Hero hero)
		{
			super.removeFromHero(hero);
			Messaging.send(hero.getPlayer(), "Your blade is no longer imbued with " + ChatColor.DARK_GREEN + "Venom" + ChatColor.GRAY + "!");
		}
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
