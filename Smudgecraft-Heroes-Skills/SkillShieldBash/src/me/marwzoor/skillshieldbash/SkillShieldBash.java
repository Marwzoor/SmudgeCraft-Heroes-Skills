package me.marwzoor.skillshieldbash;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;

public class SkillShieldBash extends TargettedSkill
{
	
	public SkillShieldBash(Heroes instance)
	{
		super(instance, "ShieldBash");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill shieldbash" });
		setDescription("Dash forward and bash your opponent with your shield stunning them for $1s and dealing $2 damage. (Need to have a shield equipped)");
		setTypes(new SkillType[] { SkillType.BUFF });
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set(SkillSetting.MAX_DISTANCE.node(), Integer.valueOf(10));
		node.set(SkillSetting.DURATION.node(), Integer.valueOf(1000));
		node.set(SkillSetting.DURATION_INCREASE.node(), Integer.valueOf(10));
		node.set(SkillSetting.DAMAGE.node(), Integer.valueOf(70));
		node.set(SkillSetting.DAMAGE_INCREASE.node(), Integer.valueOf(1));
		node.set("shield-item", Integer.valueOf(36));
		node.set(SkillSetting.COOLDOWN.node(), Integer.valueOf(20000));
		node.set(SkillSetting.MANA.node(), Integer.valueOf(20));
		return node;
	}
	
	public String getDescription(Hero hero)
	{
		int stun = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(1000), false) 
				+ SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Integer.valueOf(10), false) * hero.getSkillLevel(this)); 
		int damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Integer.valueOf(70), false) 
				+ SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Integer.valueOf(1), false) * hero.getSkillLevel(this));
		return getDescription().replace("$1", stun + "").replace("$2", damage + "");
	}

	public SkillResult use(Hero hero, LivingEntity target, String[] args) 
	{
		if (hero.getPlayer() == target) {
			return SkillResult.FAIL;
		}
		
		int maxdistance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, Integer.valueOf(10), false);
		
		final Player player = hero.getPlayer();
		
		if(player.getLocation().distance(target.getLocation()) > maxdistance)
		{
			Messaging.send(player, "Target is too far away!");
			
			return SkillResult.FAIL;
		}
		
		int shielditem = SkillConfigManager.getUseSetting(hero, this, "shield-item", 36, false);
		
		if(player.getItemInHand().getTypeId() != shielditem)
		{
			Messaging.send(player, "You are not wielding a shield!");
			return SkillResult.FAIL;
		}
		
		Vector vector = target.getLocation().toVector().subtract(player.getLocation().toVector());
		vector.multiply(1.75);
		
		player.setVelocity(vector);
		
		broadcast(player.getLocation(), player.getDisplayName() + ChatColor.GRAY + " used " + ChatColor.WHITE + "ShieldBash" + ChatColor.GRAY + "!");
		
		int stun = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, Integer.valueOf(1000), false) 
				+ SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE, Integer.valueOf(10), false) * hero.getSkillLevel(this)); 
		int damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, Integer.valueOf(70), false) 
				+ SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE, Integer.valueOf(1), false) * hero.getSkillLevel(this));
				
		CharacterTemplate ct = plugin.getCharacterManager().getCharacter(target);
		if (stun > 0) {
			ct.addEffect(new StunEffect(this, stun));
			addSpellTarget(target, hero);
			damageEntity(target, player, damage, DamageCause.ENTITY_ATTACK);
		}
				
		return SkillResult.NORMAL;
	}
	
}
