package me.marwzoor.skillcounterattack;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;

public class SkillCounterAttack extends ActiveSkill
{
	protected List<Player> unPlayers = new ArrayList<Player>();
	
	public SkillCounterAttack(Heroes plugin)
	{
		super(plugin,"CounterAttack");
		setDescription("Makes You Invisible To All Mobs");
		setArgumentRange(0,0);
		setTypes(new SkillType[]{SkillType.BUFF});
		setIdentifiers(new String[]{"skill counterattack"});		
		Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
	}
	
	@Override
	public String getDescription(Hero hero)
	{
		String description = getDescription();
		int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN, 0, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE, 0, false) * hero.getSkillLevel(this)) / 1000;

		 if(cooldown > 0)
	            description = (new StringBuilder()).append(description).append(" CD:").append(cooldown).append("s").toString();
	        int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA, 10, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE, 0, false) * hero.getSkillLevel(this);
	        if(mana > 0)
	            description = (new StringBuilder()).append(description).append(" M:").append(mana).toString();
	        int healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, 0, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this);
	        if(healthCost > 0)
	            description = (new StringBuilder()).append(description).append(" HP:").append(healthCost).toString();
	        int staminaCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA, 0, false) - SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA_REDUCE, 0, false) * hero.getSkillLevel(this);
	        if(staminaCost > 0)
	            description = (new StringBuilder()).append(description).append(" FP:").append(staminaCost).toString();
	        int delay = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DELAY.node(), 0, false) / 1000;
	        if(delay > 0)
	            description = (new StringBuilder()).append(description).append(" W:").append(delay).append("s").toString();
	        int exp = SkillConfigManager.getUseSetting(hero, this, SkillSetting.EXP.node(), 0, false);
	        if(exp > 0)
	            description = (new StringBuilder()).append(description).append(" XP:").append(exp).toString();

		return description;
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.DAMAGE.node(),Double.valueOf(100.0D));
		node.set(SkillSetting.DAMAGE_INCREASE.node(),Double.valueOf(1.0D));
        return node;

	}
	
	@Override
	public SkillResult use(Hero hero,String[] args)
	{
		Player player = hero.getPlayer();
		if(unPlayers.contains(player))
		{
			return SkillResult.FAIL;
		}
		unPlayers.add(player);
		return SkillResult.NORMAL;
	}
}
