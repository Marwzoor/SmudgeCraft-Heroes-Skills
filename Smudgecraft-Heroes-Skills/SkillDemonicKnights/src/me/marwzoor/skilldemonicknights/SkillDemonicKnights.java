package me.marwzoor.skilldemonicknights;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.smudgecraft.heroeslib.companions.Companion;
import net.smudgecraft.heroeslib.companions.CompanionPlayer;
import net.smudgecraft.heroeslib.companions.Companions;
import net.smudgecraft.heroeslib.util.ParticleEffects;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;

public class SkillDemonicKnights extends ActiveSkill
{
	public static SkillDemonicKnights skill;	
	
	public SkillDemonicKnights(Heroes instance)
	{
		super(instance, "DemonicKnights");
		skill=this;
		setDescription("You summon up to 2 Demonic Knights with $X health and $Y damage for $Z seconds. M: $1 CD: $2");
		setUsage("/skill demonicknights");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill demonicknights" });
		setTypes(new SkillType[] { SkillType.SUMMON });
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 100, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 60000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(skill)));
			duration = duration / 1000;
			double health = (SkillConfigManager.getUseSetting(hero, skill, "health", 800, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "health-increase", 0, false) * hero.getSkillLevel(skill)));
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", health + "").replace("$Y", damage + "").replace("$Z", duration + "")
					.replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("range", 25);
		node.set("health", 800);
		node.set("health-increase", 0);
		node.set(SkillSetting.DAMAGE.node(), 100);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 0);
		node.set(SkillSetting.DURATION.node(), 60000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 0);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		double health = (SkillConfigManager.getUseSetting(hero, skill, "health", 800, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "health-increase", 0, false) * hero.getSkillLevel(skill)));
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 100, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 0, false) * hero.getSkillLevel(skill)));
		int duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 60000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(skill)));
		int range = SkillConfigManager.getUseSetting(hero, skill, "range", 25, false);
		List<Companion> cmps = new ArrayList<Companion>();
		Block b = hero.getPlayer().getTargetBlock(null, range);
		if(b.getType().equals(Material.AIR))
		{
			Messaging.send(hero.getPlayer(), "Out of range!");
			return SkillResult.INVALID_TARGET_NO_MSG;
		}
		Location loc = b.getLocation().add(0,1,0);
		PotionEffect pe = new PotionEffect(PotionEffectType.SPEED,duration/1000*21,0);
		CompanionPlayer cp = Companions.getPlayerManager().getCompanionPlayer(hero.getPlayer());
		for(int i = 0;i<2;++i)
		{
			Companion cmp = new Companion(EntityType.SKELETON.getName(), hero.getName(), cp.getCompanions().size() + "", "", damage, health, health, loc, null, false);
			((Skeleton)cmp.getLivingEntity()).setSkeletonType(SkeletonType.WITHER);
			((Skeleton)cmp.getLivingEntity()).getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
			((Skeleton)cmp.getLivingEntity()).getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
			((Skeleton)cmp.getLivingEntity()).getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
			((Skeleton)cmp.getLivingEntity()).getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
			cmp.getLivingEntity().addPotionEffect(pe);
			cp.addCompanion(cmp);
			cmps.add(cmp);
		}
		try
		{
			ParticleEffects.CLOUD.sendToLocation(loc, 1, 1, 1, 1.0f, 100);
		}
		catch(Exception e)
		{
		}
		hero.addEffect(new DemonicKnightsEffect(this, duration, cmps));
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY + 
				" has summoned two Demonic Knights!");
		return SkillResult.NORMAL;
	}
	
	public class DemonicKnightsEffect extends ExpirableEffect
	{
		List<Companion> skeletons = new ArrayList<Companion>();
		public DemonicKnightsEffect(Skill skill, long duration, List<Companion> skeletons)
		{
			super(skill, "DemonicKnights", duration);
			this.skeletons = skeletons;
		}
		
		@Override
		public void removeFromHero(Hero hero)
		{
			boolean check = false;;
			Iterator<Companion> itr = skeletons.iterator();
			while(itr.hasNext())
			{
				Companion cmp = itr.next();
				if(!cmp.getLivingEntity().isDead())
				{
					cmp.getLivingEntity().remove();
					check = true;
				}
				CompanionPlayer cp = Companions.getPlayerManager().getCompanionPlayer(hero.getPlayer());
				if(cp != null)
					cp.removeCompanion(cmp);
				itr.remove();
			}
			if(check)
				Messaging.send(hero.getPlayer(), "Your Demonic Knights has perished!");
			super.removeFromHero(hero);
		}
	}
}
