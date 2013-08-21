package net.smudgecraft.heroeslib.commoneffects;

import net.minecraft.server.v1_6_R2.DataWatcher;
import net.minecraft.server.v1_6_R2.EntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicDamageEffect;
import com.herocraftonline.heroes.characters.skill.Skill;

public class PoisonEffect extends PeriodicDamageEffect
{
	public PoisonEffect(Skill skill, long period, long duration, double damage, Player applier) 
	{
		super(skill, "Poison", period, duration, damage, applier);
	}
	
	public void applyToHero(Hero hero)
	{
		skill.broadcast(hero.getPlayer().getLocation(), hero.getPlayer().getDisplayName() + ChatColor.GRAY + " is poisoned!");
		addPotionGraphicalEffect(hero.getEntity(), 0x02A000, this.getDuration());
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
		addPotionGraphicalEffect(monster.getEntity(), 0x02A000, this.getDuration());
		super.applyToMonster(monster);
	}
	
	public void removeFromMonster(Monster monster)
	{
		skill.broadcast(monster.getEntity().getLocation(), ChatColor.WHITE + monster.getEntity().getType().getName() + ChatColor.GRAY + " is no longer poisoned!");
		super.removeFromMonster(monster);
	}
	
	public void addPotionGraphicalEffect(LivingEntity entity, int color, long duration) {
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
}
