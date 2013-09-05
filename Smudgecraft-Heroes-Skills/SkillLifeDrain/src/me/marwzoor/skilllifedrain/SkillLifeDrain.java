package me.marwzoor.skilllifedrain;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.meta.FireworkMeta;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.PeriodicDamageEffect;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;

public class SkillLifeDrain extends TargettedSkill
{
	public static SkillLifeDrain skill;	
	
	public SkillLifeDrain(Heroes instance)
	{
		super(instance, "LifeDrain");
		skill=this;
		setDescription("Drain $X of your opponents life every second for $Y seconds and gain $Z% as health. M: $1 CD: $2");
		setUsage("/skill lifedrain");
		setArgumentRange(0,0);
		setIdentifiers(new String[] { "skill lifedrain" });
		setTypes(new SkillType[] { SkillType.BUFF });
		//Bukkit.getPluginManager().registerEvents(new SkillHeroListener(), plugin);
	}
	
	public String getDescription(Hero hero)
	{
		if(hero.hasAccessToSkill(skill))
		{
			double drain = (SkillConfigManager.getUseSetting(hero, skill, "drain", 70, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "drain-increase", 0.5D, false) * hero.getSkillLevel(skill)));
			int duration = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 5000, false) / 1000;
			double percent = (SkillConfigManager.getUseSetting(hero, skill, "gain-percentage", 0.75, false) +
					(SkillConfigManager.getUseSetting(hero, skill, "gain-increase", 0.005, false) * hero.getSkillLevel(skill)));
			percent *= 100;
			int mana = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 70, false);
			int cd = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 70, false);
			return super.getDescription().replace("$X", drain + "").replace("$Y", duration + "").replace("$Z", percent + "").replace("$1", mana + "").replace("$2", cd + "");
		}
		else
		{
			return super.getDescription().replace("%1", "X");
		}
	}
	
	public ConfigurationSection getDefaultConfig()
	{
		ConfigurationSection node = super.getDefaultConfig();
		node.set("drain", 70);
		node.set("drain-increase", 0.5);
		node.set("gain-percentage", 0.75);
		node.set("gain-increase", 0.005);
		node.set("range", 15);
		node.set(SkillSetting.DURATION.node(), 5000);
		node.set(SkillSetting.DURATION_INCREASE.node(), 0);
		return node;
	}
	
	@Override
	public SkillResult use(final Hero hero, final LivingEntity target, String[] args)
	{
		if(hero.getEntity().equals(target))
		{
			Messaging.send(hero.getPlayer(), "You can't target yourself!");
			return SkillResult.INVALID_TARGET_NO_MSG;
		}
		double distance = hero.getPlayer().getLocation().distance(target.getLocation());
		if(SkillConfigManager.getUseSetting(hero, skill, "range", 15, false) < distance)
		{
			Messaging.send(hero.getPlayer(), "The target is out of range!");
			return SkillResult.INVALID_TARGET_NO_MSG;
		}
		
		double duration = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION, 5000, false) +
				(SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION_INCREASE, 0, false) * hero.getSkillLevel(skill)));
		double damage = (SkillConfigManager.getUseSetting(hero, skill, "drain", 70, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "drain-increase", 0.5D, false) * hero.getSkillLevel(skill)));
		plugin.getCharacterManager().getCharacter(target).addEffect(new LifeDrainEffect(this, 1000L, (long)duration, damage, hero.getPlayer(), true));
		
		if(target instanceof Player)
			broadcast(hero.getPlayer().getLocation(), ChatColor.GRAY + "[" + ChatColor.DARK_RED + "Skill" + ChatColor.GRAY + "] " + ChatColor.DARK_RED + hero.getName() + ChatColor.GRAY + 
					" used " + ChatColor.WHITE + "LifeDrain " + ChatColor.GRAY + "on " + ChatColor.DARK_RED + ((Player)target).getName() + ChatColor.GRAY + "!");
		return SkillResult.NORMAL;	
	}
	
	public class LifeDrainEffect extends PeriodicDamageEffect
	{
		public LifeDrainEffect(Skill skill, long peroid, long duration, double damage, Player applier, boolean knockback)
		{
			super(skill, "LifeDrain", peroid, duration, damage, applier, knockback);
		}
		
		@Override
		public void applyToHero(Hero hero)
		{
			Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + applier.getName() + ChatColor.GRAY + " is draining your life!");
			super.applyToHero(hero);
		}
		
		@Override
		public void removeFromHero(Hero hero)
		{
			Messaging.send(hero.getPlayer(), ChatColor.DARK_RED + applier.getName() + ChatColor.GRAY + " is draining your life!");
			Messaging.send(applier.getPlayer(), "You are no longer draining " + ChatColor.DARK_RED + hero.getPlayer().getName() + ChatColor.WHITE + "'s" + ChatColor.GRAY + " life!");
			super.removeFromHero(hero);
		}
		
		@Override
		public void removeFromMonster(Monster monster)
		{
			Messaging.send(applier.getPlayer(), ChatColor.GRAY + "You are no longer draining the " + ChatColor.DARK_GREEN + monster.getEntity().getType().getName() + ChatColor.WHITE + 
					"'s" + ChatColor.GRAY + " life!");
			super.removeFromMonster(monster);
		}
		
		@Override
		public void tickMonster(Monster monster)
		{
			if(!monster.getEntity().getWorld().equals(applier.getWorld()))
			{
				monster.removeEffect(this);
				return;
			}
			double distance = applier.getLocation().distance(monster.getEntity().getLocation());
			if(SkillConfigManager.getUseSetting(plugin.getCharacterManager().getHero(applier), skill, "range", 15, false) < distance)
			{
				monster.removeEffect(this);
				return;
			}
			lifeDrainFWE(plugin.getCharacterManager().getHero(applier), monster.getEntity(), tickDamage);
			super.tickMonster(monster);
		}
		
		@Override
		public void tickHero(Hero hero)
		{
			if(!hero.getEntity().getWorld().equals(applier.getWorld()))
			{
				hero.removeEffect(this);
				return;
			}
			double distance = applier.getLocation().distance(hero.getEntity().getLocation());
			if(SkillConfigManager.getUseSetting(plugin.getCharacterManager().getHero(applier), skill, "range", 15, false) < distance)
			{
				hero.removeEffect(this);
				return;
			}
			lifeDrainFWE(plugin.getCharacterManager().getHero(applier), hero.getEntity(), tickDamage);
			super.tickHero(hero);
		}
	}
	
	private void lifeDrainFWE(final Hero hero, final LivingEntity target, final double damage)
	{
		final FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
		final FireworkEffect fe = FireworkEffect.builder().with(Type.BURST).withColor(Color.BLACK).withColor(Color.RED).build();
		final double x = ((target.getLocation().getX() - hero.getPlayer().getLocation().getX()) / 10) * -1;
		final double y = ((target.getLocation().getY() - hero.getPlayer().getLocation().getY()) / 10) * -1;
		final double z = ((target.getLocation().getZ() - hero.getPlayer().getLocation().getZ()) / 10) * -1;
		final Location loc = target.getLocation().add(x,y,z);
		final double gainPercentage = (SkillConfigManager.getUseSetting(hero, skill, "gain-percentage", 0.75, false) +
				(SkillConfigManager.getUseSetting(hero, skill, "gain-increase", 0.005, false) * hero.getSkillLevel(skill)));
		try
		{
			fplayer.playFirework(loc.getWorld(), loc, fe);
			loc.add(x,y,z);
			Skill.damageEntity(target, hero.getEntity(), damage, DamageCause.MAGIC);
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						//fplayer.playFirework(loc.getWorld(), loc, fe);
						loc.add(x,y,z);
						Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
						{
							@Override
							public void run()
							{
								try
								{
									fplayer.playFirework(loc.getWorld(), loc, fe);
									loc.add(x,y,z);
									Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
									{
										@Override
										public void run()
										{
											try
											{
												//fplayer.playFirework(loc.getWorld(), loc, fe);
												loc.add(x,y,z);
												Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
												{
													@Override
													public void run()
													{
														try
														{
															fplayer.playFirework(loc.getWorld(), loc, fe);
															loc.add(x,y,z);
															Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
															{
																@Override
																public void run()
																{
																	try
																	{
																		//fplayer.playFirework(loc.getWorld(), loc, fe);
																		loc.add(x,y,z);
																		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
																		{
																			@Override
																			public void run()
																			{
																				try
																				{
																					fplayer.playFirework(loc.getWorld(), loc, fe);
																					loc.add(x,y,z);
																					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
																					{
																						@Override
																						public void run()
																						{
																							try
																							{
																								//fplayer.playFirework(loc.getWorld(), loc, fe);
																								loc.add(x,y,z);
																								Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
																								{
																									@Override
																									public void run()
																									{
																										try
																										{
																											fplayer.playFirework(loc.getWorld(), loc, fe);
																											loc.add(x,y,z);
																											Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable()
																											{
																												@Override
																												public void run()
																												{
																													try
																													{
																														//fplayer.playFirework(loc.getWorld(), loc, fe);																														
																														try
																														{
																															fplayer.playFirework(target.getWorld(), target.getLocation(), fe);
																														}
																														catch(Exception exc)
																														{
																															try
																															{
																																fplayer.playFirework(target.getWorld(), target.getLocation(), fe);
																															}
																															catch(Exception exc2)
																															{
																																
																															}
																														}
																														hero.heal(gainPercentage * damage);
																													}
																													catch(Exception exc)
																													{
																													}
																												}
																											},1L);
																										}
																										catch(Exception exc)
																										{
																										}
																									}
																								},1L);
																							}
																							catch(Exception exc)
																							{
																							}
																						}
																					},1L);
																				}
																				catch(Exception exc)
																				{
																				}
																			}
																		},1L);
																	}
																	catch(Exception exc)
																	{
																	}
																}
															},1L);
														}
														catch(Exception exc)
														{
														}
													}
												},1L);
											}
											catch(Exception exc)
											{
											}
										}
									},1L);
								}
								catch(Exception exc)
								{
								}
							}
						},1L);
					}
					catch(Exception exc)
					{
					}
				}
			},1L);
		}
		catch(Exception exc)
		{
		}
	}
	
	public class FireworkEffectPlayer {
	    
	    /*
	     * Example use:
	     * 
	     * public class FireWorkPlugin implements Listener {
	     * 
	     * FireworkEffectPlayer fplayer = new FireworkEffectPlayer();
	     * 
	     * @EventHandler
	     * public void onPlayerLogin(PlayerLoginEvent event) {
	     *   fplayer.playFirework(event.getPlayer().getWorld(), event.getPlayer.getLocation(), Util.getRandomFireworkEffect());
	     * }
	     * 
	     * }
	     */
	    
	    // internal references, performance improvements
	    private Method world_getHandle = null;
	    private Method nms_world_broadcastEntityEffect = null;
	    private Method firework_getHandle = null;
	    
	    /**
	     * Play a pretty firework at the location with the FireworkEffect when called
	     * @param world
	     * @param loc
	     * @param fe
	     * @throws Exception
	     */
	    public void playFirework(World world, Location loc, FireworkEffect fe) throws Exception {
	        // Bukkity load (CraftFirework)
	        Firework fw = (Firework) world.spawn(loc, Firework.class);
	        // the net.minecraft.server.World
	        Object nms_world = null;
	        Object nms_firework = null;
	        /*
	         * The reflection part, this gives us access to funky ways of messing around with things
	         */
	        if(world_getHandle == null) {
	            // get the methods of the craftbukkit objects
	            world_getHandle = getMethod(world.getClass(), "getHandle");
	            firework_getHandle = getMethod(fw.getClass(), "getHandle");
	        }
	        // invoke with no arguments
	        nms_world = world_getHandle.invoke(world, (Object[]) null);
	        nms_firework = firework_getHandle.invoke(fw, (Object[]) null);
	        // null checks are fast, so having this seperate is ok
	        if(nms_world_broadcastEntityEffect == null) {
	            // get the method of the nms_world
	            nms_world_broadcastEntityEffect = getMethod(nms_world.getClass(), "broadcastEntityEffect");
	        }
	        /*
	         * Now we mess with the metadata, allowing nice clean spawning of a pretty firework (look, pretty lights!)
	         */
	        // metadata load
	        FireworkMeta data = (FireworkMeta) fw.getFireworkMeta();
	        // clear existing
	        data.clearEffects();
	        // power of one
	        data.setPower(1);
	        // add the effect
	        data.addEffect(fe);
	        // set the meta
	        fw.setFireworkMeta(data);
	        /*
	         * Finally, we broadcast the entity effect then kill our fireworks object
	         */
	        // invoke with arguments
	        nms_world_broadcastEntityEffect.invoke(nms_world, new Object[] {nms_firework, (byte) 17});
	        // remove from the game
	        fw.remove();
	    }
	    
	    /**
	     * Internal method, used as shorthand to grab our method in a nice friendly manner
	     * @param cl
	     * @param method
	     * @return Method (or null)
	     */
	    private Method getMethod(Class<?> cl, String method) {
	        for(Method m : cl.getMethods()) {
	            if(m.getName().equals(method)) {
	                return m;
	            }
	        }
	        return null;
	    }

	}
}
