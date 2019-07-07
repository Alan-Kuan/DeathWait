package me.alan.deathwait;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import me.alan.deathwait.files.Config;
import me.alan.deathwait.files.Data;
import me.alan.deathwait.nms.NMS;

public class PlayerListener implements Listener{

	private Core core;
	
	private Config config;
	private Data data;
	
	private PlayerFunctions pfunc;
	
	private ItemMaker im;
	
	private NMS nms;
	
	public PlayerListener(Core core){
		
		this.core = core;
		
		config = core.getConfigClass();
		data = core.getDataClass();
		
		pfunc = core.getPlayerFunctionsClass();
		
		im = new ItemMaker();

		nms = core.getNMSClass();
		
	}
	
	//如果該生物是target entity，釋放裡面的靈魂
	public void targetEntityCheck(Entity ent) {

	    if(Global.isTargetEntity(ent)){
	    	Player victim = (Player) Global.getPlayerInTargetEntity(ent);
	    	
	    	//nms.setSpectate(victim, victim);
	    	victim.setSpectatorTarget(victim);
	    	Global.removeTargetEntity(ent, victim);
	    	pfunc.setNameTag(victim);
	    }
	    
	}
	
	@EventHandler
	public void onDeath(EntityDamageEvent e){
		
		if(!(e.getEntity() instanceof Player)) {
			return;
		}
		
		Player p = (Player) e.getEntity();
		
		//如果玩家最後一擊使玩家死亡
		if(p.getHealth() - e.getFinalDamage() <= 0.0) {
			e.setCancelled(true);
		}else {
			return;
		}
		
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);
		
		//確認玩家是否為target entity
		targetEntityCheck(p);
		
	    String keepinv = p.getWorld().getGameRuleValue("keepInventory");
	    boolean remove_effect = config.getConfig().getBoolean("config.remove effects after death");
	    boolean lose_xp = config.getConfig().getBoolean("config.lose XP after death");
	    boolean enable_killer_view = config.getConfig().getBoolean("config.enable killer view");
	    boolean allow_moving = config.getConfig().getBoolean("config.allow moving in ghost mode");
	    boolean reset_food_level = config.getConfig().getBoolean("config.reset food level after death");
	    boolean have_to_wait = true;
	    
	    Global.setGameMode(p, p.getGameMode());
	    Global.addGhost(p);
	    p.setHealth(pfunc.getMaxHealth(p));
	    p.setVelocity(new Vector(0, 0, 0));
	    	    
	    //踢下騎在該玩家身上的生物
	    pfunc.kickPassenger(p);
	    
	    //取消騎乘
	    p.teleport(p.getLocation());
	    
	    //若掉進虛空
	    if(e.getCause().equals(DamageCause.VOID) && (p.getLocation().getY() < 0.0)){
	    	World w = p.getWorld();
	    	double x = p.getLocation().getX();
	    	double z = p.getLocation().getZ();
	    	
	    	p.teleport(new Location(w, x, -2.0, z));
	    }
	    
	    //清除藥水效果
	    if(remove_effect){
	    	for(PotionEffect ef : p.getActivePotionEffects()){
	    		p.removePotionEffect(ef.getType());
	    	}
	    }
	    
	    //掉落經驗值
	    if(lose_xp){
	    	int exp = p.getLevel() * 7;
	      
	    	if(exp > 0){
	    		ExperienceOrb orb = (ExperienceOrb) p.getWorld().spawnEntity(p.getLocation(), EntityType.EXPERIENCE_ORB);
	      
	    		if(exp > 100){
	    			exp = 100;
	    		}
	    		orb.setExperience(exp);
	    		p.setLevel(0);
	    		p.setExp(0.0f);
	    	}
	    }

	    //依照Gamerule來決定要不要掉落道具
	    Inventory inv = p.getInventory();
	    if(keepinv.equals("false")){
	    	ItemStack[] items = inv.getContents();
	    	int length = items.length;
	    	
	    	for(int slot = 0; slot < length; slot++){
	    		ItemStack item = items[slot];
	    		
	    		if(item != null){
	    			p.getWorld().dropItem(p.getLocation(), item);
	    		}
	    	}
	    	inv.clear();
	    }
	    
	    //允許移動
	    if(allow_moving){
	    	p.setFlySpeed(0.1f);
	    }else{
	    	p.setFlySpeed(0);
	    }
	    
	    //回復飽食度
	    if(reset_food_level){
	    	p.setFoodLevel(20);
	    }
	    
	    if(p.hasPermission("dw.bypass")){
	    	
	    	have_to_wait = false;
	    	
	    //如果沒有權限才會扣直接復活額度
	    }else{
	    	
	    	int amount = 0;
	    	
		    if(data.getConfig().isSet("players." + p.getUniqueId() + ".quota")){
		    	amount = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
		    }
		    
		    if(amount > 0){
		    	have_to_wait = false;
		    	data.set("players." + p.getUniqueId(), Integer.valueOf(amount - 1));
		    }
		    
	    }
	    
	    p.setGameMode(GameMode.SPECTATOR);
	    
	    if(enable_killer_view && (e instanceof EntityDamageByEntityEvent)){
	    	
	    	EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) e;
	    	
	    	Entity killer = Global.getKiller(edbee.getDamager());
	    	
	    	//如果殺手存在 且 玩家不是殺手 且 殺手不是爆炸實體或落沙
	    	if(killer != null && !p.equals(killer) && !Global.isExplosiveEntity(killer)) {
	    		//nms.setSpectate(p, killer);
	    		p.setSpectatorTarget(killer);
	    		Global.addTargetEntity(killer, p);
	    	}
	    		    	
	    }
	    
	    //顯示名條
	    pfunc.setNameTag(p);
	    
	    //需要等待
	    if(have_to_wait){
	    	
	    	int wait = config.getConfig().getInt("config.waiting seconds");
	    	
	    	nms.sendTitle(p, ChatColor.RED + "你已經死了", 0, wait*20, 0);
	    	
	    	BukkitTask countdwon_task = new BukkitRunnable(){

		    	int temp = wait;
		    	
		    	@Override
		    	public void run(){
		    		
		    		Global.setLeftWaitingTimes(p, temp);
		    		
		    		if(temp > 0){
		    				
		    			String show = temp + "秒後復活";
		            
		    			nms.sendSubTitle(p, ChatColor.GOLD + show, 0, 25, 0);
		    				
		    			temp--;
		    			
		    		}else{
		    			cancel();
		    			pfunc.Respawn(p);
		    		}
		    		
		    	}
		    	
	    	}.runTaskTimer(core, 0, 20);
	    	
	    	Global.addCountdownTask(p, countdwon_task);
	    	
	    }else{
	    	
	    	pfunc.Respawn(p);
	    	
	    }
	    
	}
	
	//當裡面有靈魂的生物死亡，靈魂被釋放
	@EventHandler
	public void onTargetEntityDeath(EntityDeathEvent e){
		
	    Entity ent = e.getEntity();
	    
	    targetEntityCheck(ent);
	    
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		
		Player p = e.getPlayer();
				
		//如果退出的玩家裡面有其他玩家的靈魂，靈魂被釋放
		targetEntityCheck(p);
	    
	    //如果退出的玩家是幽靈
	    if(Global.isGhost(p)){
	    	
	    	pfunc.removeNameTag(p);
	    	
	    	data.set("players." + p.getUniqueId() + ".gamemode", Global.getGameMode(p).toString());
	    	
	    	if(Global.hasLeftWaitingTimes(p)){
	    		
		    	Global.cancelCountdownTask(p);
		    	
		    	data.set("players." + p.getUniqueId() + ".left waiting times", Global.getLeftWaitingTimes(p));
		    	
		    	Global.removeLeftWaitingTimes(p);
	    	}

	    	Global.removeGameMode(p);
			Global.removeGhost(p);
			
			if(Global.isInTargetEntity(p)){
				for(Entity target: Global.getTargetEntities()){

					if(Global.getPlayerInTargetEntity(target).equals(p)){
						Global.removeTargetEntity(target, p);
					}
				}
			}
	    }
	    
	}
	//如果在等待時登出，下次登入時要繼續等待
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
				
		Player p = e.getPlayer();
		
		boolean have_to_wait = data.getConfig().isSet("players." + p.getUniqueId() + ".left waiting times");
		
		if(have_to_wait){
			
			boolean allow_moving = config.getConfig().getBoolean("config.allow moving in ghost mode");

			Global.addGhost(p);
			Global.setGameMode(p, GameMode.valueOf(data.getConfig().getString("players." + p.getUniqueId() + ".gamemode")));
			
			data.set("players." + p.getUniqueId() + ".gamemode", null);
			
			new BukkitRunnable(){

		    	@Override
		    	public void run(){
		    		
					//顯示名條		    
				    pfunc.setNameTag(p);

	    		if(allow_moving){
						p.setFlySpeed(0.1f);
					}else{
						p.setFlySpeed(0);
					}
		    		
		    	}
		    	
			}.runTaskLater(core, 2);
    		
			int left = data.getConfig().getInt("players." + p.getUniqueId() + ".left waiting times");
			
			nms.sendTitle(p, ChatColor.RED + "上次登出時仍在等待復活", 0, left*20, 0);
			
			BukkitTask countdown_task = new BukkitRunnable(){

				int temp = left;
				
		    	@Override
		    	public void run(){

		    		Global.setLeftWaitingTimes(p, temp);
		    		
		    		if(temp > 0){
		    			
		    			String show = temp + "秒後復活";
		    				
		    			nms.sendSubTitle(p, ChatColor.GOLD + show, 0, 25, 0);
		    				
		    			temp--;
		    			
		    		}else{
		    			cancel();
		    			data.set("players." + p.getUniqueId() + ".left waiting times", null);
		    			pfunc.Respawn(p);
		    		}
		    		
		    	}
		    	
			}.runTaskTimer(core, 0, 20);
			
			Global.addCountdownTask(p, countdown_task);
			
		}
		
	}
	
	//當玩家使用蹲下來求救
	@EventHandler
	public void onShiftYelling(PlayerToggleSneakEvent e) {
		
		Player p = e.getPlayer();
		
		if(p.isSneaking() && Global.isGhost(p) && p.hasPermission("dw.yell")) {
			pfunc.yell(p);
		}
		
	}
	
	//幽靈模式禁用指令
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent e){
		
	    Player p = e.getPlayer();
	    
		if(Global.isGhost(p)){
			e.setCancelled(true);
	    }
		
	}
	
	//使用道具
	@EventHandler
	public void onUseItem(PlayerInteractEvent e){
			    
	    Player p = e.getPlayer();
	    PlayerInventory inv = p.getInventory();
	    Action action = e.getAction();
	    ItemStack item;
	    
		if(e.getHand() == EquipmentSlot.HAND){
			item = inv.getItemInMainHand();
	    }else if(e.getHand() == EquipmentSlot.OFF_HAND){
	    	item = inv.getItemInOffHand();
	    }else{
	    	return;
	    }
		
		if(item.getType() == Material.AIR){
			return;
		}
		
	    ItemMeta meta = item.getItemMeta();
	    
	    ItemStack instant_respawn_item = im.createItem(Material.getMaterial(config.getConfig().getString("config.instant respawn item.type")),
				config.getConfig().getInt("config.instant respawn item.damage"),
				config.getConfig().getString("config.instant respawn item.name"),
				config.getConfig().getStringList("config.instant respawn item.lore"), true);
		ItemStack assistant_respawn_item = im.createItem(Material.getMaterial(config.getConfig().getString("config.assistant respawn item.type")),
				config.getConfig().getInt("config.assistant respawn item.damage"),
				config.getConfig().getString("config.assistant respawn item.name"),
				config.getConfig().getStringList("config.assistant respawn item.lore"), true);
	    
	    if(!meta.hasLore()){
	    	return;
	    }
	    if(!meta.hasDisplayName()){
	    	return;
	    }
	    
	    if((action.equals(Action.RIGHT_CLICK_AIR)) || (action.equals(Action.RIGHT_CLICK_BLOCK))){
	    	
	    	//直接復活道具
	    	if((meta.getDisplayName().equals(instant_respawn_item.getItemMeta().getDisplayName())) && (meta.getLore().equals(instant_respawn_item.getItemMeta().getLore()))){
	    		e.setCancelled(true);
	        
	    		int amount = 0;
	    		if(data.getConfig().isSet("players." + p.getUniqueId() + ".quota")){
	    			amount = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
	    		}
	    		data.set("players." + p.getUniqueId() + ".quota", Integer.valueOf(amount + 1));
	    		
	    		if(item.getAmount() > 1){
	    			item.setAmount(item.getAmount() - 1);
	    		}else{
	    			
	    			if(e.getHand() == EquipmentSlot.HAND){
	    				inv.setItemInMainHand(null);
	    			}else{
	    				inv.setItemInOffHand(null);
	    			}

	    		}
	    		
	    		int quota = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
	    		
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "已增加一次免等額度");
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "剩餘" + quota + "次免等額度");
	    	}
	    	
	    	//原地復活道具
	    	if((meta.getDisplayName().equals(assistant_respawn_item.getItemMeta().getDisplayName())) && (meta.getLore().equals(assistant_respawn_item.getItemMeta().getLore()))){
	    		e.setCancelled(true);
	    		
	    		for(Entity ent : p.getNearbyEntities(1.0, 0.0, 1.0)){
	    			if((ent instanceof Player)){

    					Player entp = (Player) ent;
    					
	    				if(Global.isGhost(entp) && !Global.isInTargetEntity(entp)){
	    					
	    					if(Global.didNotChoose(entp)){
	    						Global.removeNoChoose(entp);
	    						Global.cancelTimeLimitTask(entp);
	    						entp.closeInventory();
	    					}
	    					
	    					if(Global.hasCountdownTask(entp)){
		    					Global.cancelCountdownTask(entp);
	    					}
	    					
	    					pfunc.removeNameTag(entp);
	    					entp.teleport(entp.getLocation().add(0.0, 0.2, 0.0));
	    					pfunc.TurnBack(entp);
	              
	    					nms.sendTitle(entp, "", 0, 40, 0);
	    					nms.sendSubTitle(entp, 
	    							ChatColor.DARK_GREEN + p.getName() + "使用" + ChatColor.RESET + assistant_respawn_item.getItemMeta().getDisplayName() + ChatColor.DARK_GREEN + "讓你原地復活",
	    							0, 40, 10);
	    					entp.getWorld().spawnEntity(entp.getLocation(), EntityType.FIREWORK);
	    					if(item.getAmount() > 1){
	    						item.setAmount(item.getAmount() - 1);
	    						break;
	    					}

	    	    			if(e.getHand() == EquipmentSlot.HAND){
	    	    				inv.setItemInMainHand(null);
	    	    			}else{
	    	    				inv.setItemInOffHand(null);
	    	    			}
	              
	    					break;
	    				}
	    			}
	    		}
	    	}
	    }
	    if((action.equals(Action.LEFT_CLICK_AIR)) || (action.equals(Action.LEFT_CLICK_BLOCK))){
	    	if((meta.getDisplayName().equals(instant_respawn_item.getItemMeta().getDisplayName())) && (meta.getLore().equals(instant_respawn_item.getItemMeta().getLore()))){
	    		e.setCancelled(true);
	    		
	    		int quota = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "剩餘" + quota + "次免等額度");
	    	}
	    }
	}
	
}
