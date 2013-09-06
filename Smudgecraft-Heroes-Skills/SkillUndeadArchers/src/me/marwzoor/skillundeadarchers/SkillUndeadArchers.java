package me.marwzoor.skillundeadarchers;

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
import org.bukkit.inventory.ItemStack;

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

public class SkillUndeadArchers extends ActiveSkill
{
public static SkillUndeadArchers skill;	
	
	public SkillUndeadArchers(Heroes instance)
	{
		super(instance, "UndeadArchers");
		skill=this;
		setDescription("You summon up to $X dead archers with $Y health and $Z damage for $Q seconds. M: $1 CD: $2");
		setUsage("/skill undeadarchers");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill undeadarchers" });
		setTypes(new SkillType[] { SkillType.SUMMON });
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 100, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 1, false) * hero.getSkillLevel(skill)));
			double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 30000, false) +
					(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(skill)));
			duration = duration / 1000;
			double health = (SkillConfigManager.getUseSetting(hero, skill, "health", 300, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "health-increase", 1, false) * hero.getSkillLevel(skill)));
			int amount = SkillConfigManager.getUseSetting(hero, skill, "amount", 1, false);
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", amount + "").replace("$Y", health + "").replace("$Z", damage + "").replace("$Q", duration + "")
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
		node.set("amount", 1);
		node.set("range", 25);
		node.set("health", 300);
		node.set("health-increase", 1);
		node.set("multiple-spawn", 0.5);
		node.set(SkillSetting.DAMAGE.node(), 100);
		node.set(SkillSetting.DAMAGE_INCREASE.node(), 1);
		node.set(SkillSetting.DURATION.node(), 30000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 100);
		return node;
	}
	
	@Override
	public SkillResult use(Hero hero, String[] args)
	{
		int amount = SkillConfigManager.getUseSetting(hero, skill, "amount", 1, false);
		double health = (SkillConfigManager.getUseSetting(hero, skill, "health", 300, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "health-increase", 1, false) * hero.getSkillLevel(skill)));
		double damage = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, 100, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, 1, false) * hero.getSkillLevel(skill)));
		int duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 30000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 100, false) * hero.getSkillLevel(skill)));
		int range = SkillConfigManager.getUseSetting(hero, skill, "range", 25, false);
		List<Companion> cmps = new ArrayList<Companion>();
		Block b = hero.getPlayer().getTargetBlock(null, range);
		if(b.getType().equals(Material.AIR))
		{
			Messaging.send(hero.getPlayer(), "Out of range!");
			return SkillResult.INVALID_TARGET_NO_MSG;
		}
		Location loc = b.getLocation().add(0,1,0);
		CompanionPlayer cp = Companions.getPlayerManager().getCompanionPlayer(hero.getPlayer());
		Companion cmp = new Companion(EntityType.SKELETON.getName(), hero.getName(), cp.getCompanions().size() + "", "", damage, health, health, loc, null, false);
		((Skeleton)cmp.getLivingEntity()).getEquipment().setItemInHand(new ItemStack(Material.BOW));
		cp.addCompanion(cmp);
		cmps.add(cmp);
		if(amount > 1)
		{
			double multiSpawn = SkillConfigManager.getUseSetting(hero, skill, "multiple-spawn", 0.5, false);
			for(int i = 0;i < amount-1;++i)
			{
				if(Math.random() > multiSpawn)
					break;
				Companion temp = new Companion(EntityType.SKELETON.getName(), hero.getName(), cp.getCompanions().size() + "", "", damage, health, health, loc, null, false);
				((Skeleton)temp.getLivingEntity()).getEquipment().setItemInHand(new ItemStack(Material.BOW));
				cp.addCompanion(temp);
				cmps.add(temp);
			}
		}
		try
		{
			ParticleEffects.CLOUD.sendToLocation(loc, 1, 1, 1, 1.0f, 100);
		}
		catch(Exception e)
		{
		}
		hero.addEffect(new UndeadArchersEffect(this, duration, cmps));
		broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY + 
				" has raised " + cmps.size() + " undead archer!");
		return SkillResult.NORMAL;
	}
	
	public class UndeadArchersEffect extends ExpirableEffect
	{
		List<Companion> skeletons = new ArrayList<Companion>();
		public UndeadArchersEffect(Skill skill, long duration, List<Companion> skeletons)
		{
			super(skill, "UndeadArchers", duration);
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
				Messaging.send(hero.getPlayer(), "Your skeleton minions has perished!");
			super.removeFromHero(hero);
		}
	}
}
