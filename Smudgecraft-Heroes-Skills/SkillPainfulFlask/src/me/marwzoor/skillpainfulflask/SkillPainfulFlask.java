package me.marwzoor.skillpainfulflask;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillPainfulFlask extends ActiveSkill
{
	public SkillPainfulFlask(Heroes instance)
	{
		super(instance, "PainfulFlask");
		setDescription("You throw a painful flask, dealing damage to everyone the flask hits. DMG: %DMG M: %M CD: %CD");
		setUsage("/skill painfulflask");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill painfulflask" });
		setTypes(new SkillType[] { SkillType.HARMFUL, SkillType.DAMAGING });
		Bukkit.getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(this))
		{
		int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Integer.valueOf(70), false) +
				(SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Double.valueOf(0.5), false) * hero.getSkillLevel(this)));
		int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, Integer.valueOf(0), false) / 1000);
		int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, Integer.valueOf(0), false);
			return super.getDescription().replace("%DMG", damage + "").replace("%M", mana + "").replace("%CD", cooldown + "");
		}
		else
		{
			return super.getDescription().replace("%DMG", "X").replace("%M", "X").replace("%CD", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DAMAGE.node(), Integer.valueOf(70));
		node.set(SkillSetting.DAMAGE_INCREASE.node(), Double.valueOf(0.5));
		node.set(SkillSetting.COOLDOWN.node(), Integer.valueOf(0));
		node.set(SkillSetting.MANA.node(), Integer.valueOf(0));
		return node;
	}
	
	public SkillResult use(Hero hero, String[] args)
	{
		ThrownPotion tp = hero.getPlayer().launchProjectile(ThrownPotion.class);
		ItemStack flask = new ItemStack(Material.POTION);
		flask.setDurability((short) 12);
		tp.setItem(flask);
		tp.setMetadata("PainfulFlask", new FixedMetadataValue(plugin, hero.getPlayer().getName()));
		
		this.broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + hero.getPlayer().getDisplayName() + ChatColor.GRAY + " threw a " + ChatColor.WHITE + "PainfulFlask" + ChatColor.GRAY + "!");
		
		return SkillResult.NORMAL;
	}
	
	public class SkillHeroListener implements Listener
	{
		private SkillPainfulFlask skill;
		
		public SkillHeroListener(SkillPainfulFlask skill)
		{
			this.skill=skill;
		}
		
		@EventHandler
		public void onPotionSplashEvent(PotionSplashEvent event)
		{
			if(event.isCancelled())
				return;
			
			if(event.getPotion()==null)
				return;
				
			List<MetadataValue> metadata = event.getPotion().getMetadata("PainfulFlask");
			
			if(metadata!=null && !metadata.isEmpty())
			{
				for(MetadataValue mdv : metadata)
				{
					if(mdv instanceof FixedMetadataValue && mdv.getOwningPlugin().equals(plugin))
					{
						String name = mdv.asString();
						if(name==null)
							return;
						
						Hero hero = null;
						
						for(Player p : event.getPotion().getWorld().getPlayers())
						{
							if(p.getName().equalsIgnoreCase(name))
							{
								hero = plugin.getCharacterManager().getHero(p);
							}
						}
						
						if(hero==null)
							return;
												
						int damage = (int) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE, Integer.valueOf(70), false) +
								(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DAMAGE_INCREASE, Double.valueOf(0.5), false) * hero.getSkillLevel(skill)));
						
						event.getPotion().removeMetadata("PainfulFlask", plugin);
						
						for(LivingEntity le : event.getAffectedEntities())
						{
							if(!le.equals(hero.getPlayer()))
							{
							event.setIntensity(le, 0);
							skill.addSpellTarget(le, hero);
							Skill.damageEntity(le, hero.getPlayer(), damage, DamageCause.MAGIC);
							}
						}
					}
				}
			}
		}
	}
}
