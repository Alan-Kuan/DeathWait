package me.alan.deathwait;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import me.alan.deathwait.anvilgui.AnvilGUI;
import me.alan.deathwait.files.Config;
import me.alan.deathwait.files.Data;
import me.alan.deathwait.files.Spawns;
import me.alan.deathwait.nms.NMS;

public class PlayerListener implements Listener{

	private Core core;
	private Config config;
	private Spawns spawns;
	private Data data;
	private ListSpawns list;
	private PlayerFunctions pfunc;
	private AnvilGUI anvil;
	private NMS nms;
	private ItemMaker im;
	
	public PlayerListener(Core core){
		
		this.core = core;
		
		config = core.getConfigClass();
		spawns = core.getSpawnsClass();
		data = core.getDataClass();
		pfunc = core.getPlayerFunctionsClass();
		list = new ListSpawns(core, pfunc);
		anvil = core.getAnvilGUIClass();
		nms = core.getNMSClass();
		im = new ItemMaker();
		
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e){
		
	    if(!(e.getEntity() instanceof Player)){
	    	return;
	    }
	    
	    final Player p = e.getEntity();
	    
	    String keepinv = p.getWorld().getGameRuleValue("keepInventory");
	    boolean remove_effect = config.getConfig().getBoolean("config.remove effects after death");
	    boolean lose_xp = config.getConfig().getBoolean("config.lose XP after death");
	    boolean enable_killer_view = config.getConfig().getBoolean("config.enable killer view");
	    boolean allow_moving = config.getConfig().getBoolean("config.allow moving in ghost mode");
	    boolean reset_food_level = config.getConfig().getBoolean("config.reset food level after death");
	    
	    Global.setGameMode(p, p.getGameMode());
	    Global.addGhost(p);
	    p.setHealth(pfunc.getMaxHealth(p));
	    p.setVelocity(new Vector(0, 0, 0));
	    e.getDrops().clear();

	    //踢下騎在該玩家身上的生物
	    pfunc.kickPassenger(p);
	    
	    //取消騎乘
	    p.teleport(p.getLocation());
	    
	    //自殺
	    if(Global.isKilled(p)){
	    	e.setDeathMessage(p.getName() + " 已死亡");
	    	Global.removeKilled(p);
	    }
	    
	    //若掉進虛空
	    if((p.getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.VOID)) && (p.getLocation().getY() < 0.0)){
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
	    
	    if(lose_xp){
	    	int exp = p.getLevel() * 7;
	      
	    	if(exp > 0){
	    		ExperienceOrb orb = (ExperienceOrb) p.getWorld().spawnEntity(p.getLocation(), EntityType.EXPERIENCE_ORB);
	      
	    		if(exp > 100){
	    			exp = 100;
	    		}
	    		orb.setExperience(exp);
	    		p.setLevel(0);
	    		p.setExp(0.0F);
	    	}
	    }
	    
	    p.setGameMode(GameMode.SPECTATOR);
	    
	    if(enable_killer_view && (p.getLastDamageCause() instanceof EntityDamageByEntityEvent)){
	    	
	    	EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) p.getLastDamageCause();
	    	Entity killer = edbee.getDamager();
	    	Entity shooter = pfunc.getShooter(killer);
	    	
	    	if((shooter != null) && (!killer.equals(shooter)) && (!p.equals(shooter))){
	    		nms.setSpectate(p, shooter);
	    		Global.addTargetEntity(shooter, p);
	    	}else if((killer.equals(shooter)) && (!(killer instanceof Creeper)) && (!(killer instanceof EnderCrystal)) && (!(killer instanceof FallingBlock)) && (!(killer instanceof TNTPrimed)) && (!(killer instanceof ExplosiveMinecart))){
	    		nms.setSpectate(p, killer);
	    		Global.addTargetEntity(shooter, p);
	    	}
	    	
	    }
	    
	    if(allow_moving){
	    	p.setFlySpeed(0.1f);
	    }else{
	    	p.setFlySpeed(0);
	    }
	    
	    if(reset_food_level){
	    	p.setFoodLevel(20);
	    }
	    
	    //依照Gamerule來決定要不要噴道具
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
	    
	    //顯示名條
	    if(!Global.isInTargetEntity(p)) {
	    	pfunc.setNameTag(p);
	    }
	    
	    boolean needwait = true;
	    
	    if(p.hasPermission("dw.bypass")){
	    	
	    	needwait = false;
	    	
	    //如果沒有權限才會扣直接復活額度
	    }else{
	    	
	    	int amount = 0;
		    if(data.getConfig().isSet("players." + p.getUniqueId() + ".quota")){
		    	amount = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
		    }
		    if(amount > 0){
		    	needwait = false;
		    	data.set("players." + p.getUniqueId(), Integer.valueOf(amount - 1));
		    }
		    
	    }
	    	    
	    //需要等待
	    if(needwait){
	    	
	    	int wait = config.getConfig().getInt("config.waiting seconds");
	    	
	    	nms.sendTitle(p, ChatColor.RED + "你已經死了", 0, wait*20, 0);
	    	
		    int countdown = core.getServer().getScheduler().scheduleSyncRepeatingTask(core, new Runnable(){
		     
		    	int temp = wait;
		    	
		    	@Override
		    	public void run(){
		    		
		    		Global.setLeftWaitingTimes(p, temp);
		    		
		    		if(temp > 0){
		    				
		    			String show = temp + "秒後復活";
		            
		    			nms.sendSubTitle(p, ChatColor.GOLD + show, 0, 25, 0);
		    				
		    			temp--;
		    			
		    		}else{
		    			core.getServer().getScheduler().cancelTask(Global.getIds(p));
		    			Global.removeIds(p);
		    			pfunc.Respawn(p);
		    		}
		    		
		    	}
		    	
		    }, 0, 20);
		    Global.setIds(p, countdown);
		    
	    }else{
	    	
	    	pfunc.Respawn(p);
	    	
	    }
	    
	}
	
	//當裡面有靈魂的生物死亡，靈魂被釋放
	@EventHandler
	public void onTargetEntityDeath(EntityDeathEvent e){
		
	    Entity ent = e.getEntity();
	    
	    if(Global.isTargetEntity(ent)){
	    	Player p = (Player) Global.getPlayerInTargetEntity(ent);
	    	
	    	nms.setSpectate(p, p);
	    	Global.removeTargetEntity(ent, p);
	    	pfunc.setNameTag(p);
	    }
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		
		Player p = e.getPlayer();
				
		//如果退出的玩家裡面有其他玩家的靈魂，靈魂被釋放
	    if(Global.isTargetEntity(p)){
	    	Player victim = (Player) Global.getPlayerInTargetEntity(p);
	    	
	    	nms.setSpectate(victim, victim);
	    	Global.removeTargetEntity(p, victim);
	    	pfunc.setNameTag(victim);
	    }
	    
	    //如果退出的玩家是幽靈
	    if(Global.isGhost(p)){
	    	
	    	pfunc.removeNameTag(p);
	    	
	    	data.set("players." + p.getUniqueId() + ".gamemode", Global.getGameMode(p).toString());
	    	
	    	if(Global.hasLeftWaitingTimes(p)){
		    	core.getServer().getScheduler().cancelTask(Global.getIds(p));
		    	data.set("players." + p.getUniqueId() + ".left waiting times", Global.getLeftWaitingTimes(p));
		    	
		    	Global.removeLeftWaitingTimes(p);
		    	Global.removeIds(p);
	    	}

	    	Global.removeGameMode(p);
			Global.removeGhost(p);
			Global.removeKilled(p);
			
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
			
		    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
		    	
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
		    	
		    }, 2);
    		
			int left = data.getConfig().getInt("players." + p.getUniqueId() + ".left waiting times");
			
			nms.sendTitle(p, ChatColor.RED + "由於上次在等待時登出，所以必須繼續等待", 0, left*20, 0);
			
			int countdown = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(core, new Runnable(){
			    
				int temp = left;
				
		    	@Override
		    	public void run(){

		    		Global.setLeftWaitingTimes(p, temp);
		    		
		    		if(temp > 0){
		    			
		    			String show = temp + "秒後復活";
		    				
		    			nms.sendSubTitle(p, ChatColor.GOLD + show, 0, 25, 0);
		    				
		    			temp--;
		    			
		    		}else{
		    			core.getServer().getScheduler().cancelTask(Global.getIds(p));
		    			Global.removeIds(p);
		    			data.set("players." + p.getUniqueId() + ".left waiting times", null);
		    			pfunc.Respawn(p);
		    			
		    		}
		    		
		    	}
		    	
		    }, 0, 20);
		    Global.setIds(p, countdown);		    
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
	
	//防止還沒選復活點就關閉畫面
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent e){
		
	    final Player p = (Player) e.getPlayer();
	    Inventory gui = e.getInventory();
	    
	    if(!Global.hasTurnedPage(p)){
	    	
	    	if(gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "所有復活點") && Global.didNotChoose(p)){
	    	    
	    		try{
	    			
	    			String s = gui.getItem(31).getItemMeta().getDisplayName().replace("§9-第", "").replace("頁-", "");
	    			final int page_num = Integer.parseInt(s);
	    			
	    			Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	          
	    				@Override
	    				public void run(){
	    					list.List(p, page_num);
	    				}
	    				
	    			}, 1L);
	    			
	    		}catch(NumberFormatException ex){
	    			
	    			ex.printStackTrace();
	    			WarningGen.Warn("在獲取目前所在頁數時出了問題");
	    			
	    		}
	    		
	    	}
	    	
	    }else{
	    	Global.removeTurnPage(p);
	    }
	}
	
	//復活點目錄的按鈕
	@EventHandler
	public void ClickButton(InventoryClickEvent e){
				
	    Inventory gui = e.getClickedInventory();

	    if(gui == null){
	    	return;
	    }
	    
	    if(!(e.getWhoClicked() instanceof Player)){
	    	return;
	    }
	    
	    if(!gui.getType().equals(InventoryType.CHEST)){
	    	return;
	    }
	    
	    if(!gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "所有復活點")) {
	    	return;
	    }
	    	    
	    Player p = (Player) e.getWhoClicked();
	    ItemStack item = e.getCurrentItem();
	    ClickType click = e.getClick();
	    int slot = e.getSlot();
	    
	    if(item.getType() == Material.AIR){
	    	return;
	    }
	    
	    String name = item.getItemMeta().getDisplayName();
	    
	    List<String> lore = new ArrayList<String>();
	    
	    if(item.getItemMeta().hasLore()){
	    	lore = item.getItemMeta().getLore();
	    }
	    
	    String id = lore.get(0).toString().replace("§bID:", "");
	    
	    e.setCancelled(true);
	    
	    //回到自然重生點
	    if((click.equals(ClickType.LEFT)) && (slot == 27) && (name.equals(ChatColor.DARK_GREEN + "自然重生點"))){
		    
	    	if(Global.didNotChoose(p)){
		    	pfunc.removeNameTag(p);
	    		Global.removeNoChoose(p);
	    	}
	    	
	    	//先讓名條被刪除再傳送
	    	Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	    		
	    		@Override
	    		public void run(){
	    	    	pfunc.tpNormalSpawnPoint(p);
	    	    	
	    	    	p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
	    		}
	    		
	    	}, 2);
	    	
	    }
	    
	    //上一頁
	    if((click.equals(ClickType.LEFT)) && (slot == 30) && (name.equals(ChatColor.BLUE + "上一頁"))){
	    	
	    	p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	    		
	    	try{
	    			
	    		String s = gui.getItem(31).getItemMeta().getDisplayName().replace("§9-第", "").replace("頁-", "");
	    		int page_num = Integer.parseInt(s);
	    		
	    		if (page_num - 1 > 0){
	    			Global.addTurnPage(p);
	    			list.List(p, page_num - 1);
	    		}
	    			
	    	}catch(NumberFormatException ex){
	    		ex.printStackTrace();
	    		WarningGen.Warn("在獲取目前所在頁數時出了問題");
	    	}
	    }
	    	
	    //下一頁
	    if((click.equals(ClickType.LEFT)) && (slot == 32) && (name.equals(ChatColor.BLUE + "下一頁"))){
	    	
	    	p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	    		
	    	try{
	    			
	    		String s = gui.getItem(31).getItemMeta().getDisplayName().replace("§9-第", "").replace("頁-", "");
	    		int page_num = Integer.parseInt(s);
	        
	    		String totalstr = gui.getItem(31).getItemMeta().getLore().get(0).toString().replace("§2 共", "").replace("頁", "");
	    		int total = Integer.parseInt(totalstr);
	    			
	    		if(page_num + 1 <= total){
	    			Global.addTurnPage(p);
	    			list.List(p, page_num + 1);
	    		}
	    			
	    	}catch(NumberFormatException ex){
	    		ex.printStackTrace();
	    		WarningGen.Warn("在獲取目前所在頁數或總頁數時出了問題");
	    	}
	    }
	    	
	    //不是幽靈時，可以編輯自訂復活點
	    if(!Global.isGhost(p) && (slot <= 26)){
	    		
	    	//重設復活點
	    	if(click.equals(ClickType.SHIFT_RIGHT)){
	    		
	    		Location loc = p.getLocation();
	    		
	    		if(spawns.getConfig().isSet("spawns")){
	    			  
					spawns.set("spawns." + id + ".location", loc);
					
					nms.sendLocation(p, name, loc);
					
				}

	    		ItemMeta meta = item.getItemMeta();
	    		
	    		lore.set(2, ChatColor.BLUE + "X座標:" + loc.getX());
	    		lore.set(3, ChatColor.BLUE + "Y座標:" + loc.getY());
	    		lore.set(4, ChatColor.BLUE + "Z座標:" + loc.getZ());
	    		
	    		meta.setLore(lore);
	    		
	    		item.setItemMeta(meta);
	    		
	    		e.setCurrentItem(item);
	    		
	    		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
	    	}
	    		
	    	//移除復活點
	    	if(click.equals(ClickType.SHIFT_LEFT)){
	    	    
	    		spawns.set("spawns." + id, null);
	    		
	    		p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
	    			
	    		try{
	    			String s = gui.getItem(31).getItemMeta().getDisplayName().replace("§9-第", "").replace("頁-", "");
	    			int page_num = Integer.parseInt(s);
	    				
	    			if ((page_num > 1) && (slot == 0) && (gui.getItem(1) == null)) {
	    				page_num--;
	    			}
	    				
	    			list.List(p, page_num);
	    		}catch(NumberFormatException ex){
	    			ex.printStackTrace();
	    			WarningGen.Warn("在獲取目前所在頁數時出了問題");
	    		}
	    		p.sendMessage(Global.Header + ChatColor.DARK_RED + "已將復活點 §f§l[§r" + name + "§f§l] " + ChatColor.RESET + ChatColor.DARK_RED + "移除!");
	    	}
	    		
	    	//更改圖示
	    	if(e.getAction() == InventoryAction.SWAP_WITH_CURSOR){
	    		
	    		ItemStack icon = e.getCursor();
	    		ItemMeta meta = icon.getItemMeta();
	    		boolean glow = false;
	    		
	    		p.setItemOnCursor(new ItemStack(Material.AIR));
	    		
	    		spawns.set("spawns." + id + ".icon.type", icon.getType().toString());
	    		spawns.set("spawns." + id + ".icon.data", icon.getDurability());
	    		
	    		if(icon.getItemMeta().hasEnchants()){
	    			glow = true;
	    		}
	    		spawns.set("spawns." + id + ".icon.glowing", glow);
	    		
	    		meta.setDisplayName(item.getItemMeta().getDisplayName());
	    		meta.setLore(item.getItemMeta().getLore());
	    		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
	    		
	    		icon.setItemMeta(meta);
	    		icon.setAmount(1);
	    		
	    		e.setCurrentItem(icon);

	    		p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "已更新復活點圖示");
	    		
	    		return;
	    		
	    	}
	    	
	    	//重新命名復活點
	    	if(click.equals(ClickType.RIGHT)){
	    	    
	    		List<String> info = new ArrayList<String>();
	    		
	    		info.add(ChatColor.GOLD + "更新前名稱:");
	    		info.add(name);
	    		info.add("");
	    		info.add(ChatColor.DARK_RED + "備註: 若要在名稱中顯示&，請使用/&");
	    		info.add("");
	    		info.add(ChatColor.BLUE + "點擊左方的格子來回復原本名稱");
	    		info.add(ChatColor.AQUA + "點擊中間的格子來預覽結果");
	    		info.add(ChatColor.GREEN + "點擊右方的格子來確認更改");
	    		info.add(ChatColor.DARK_GRAY + "ID:" + id);
	        
	    		anvil.openAnvil(p, name, info);
	    		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1);
	    	}
	    }
	    
	    //傳送至復活點
	    if((click.equals(ClickType.LEFT)) && (slot <= 26)){
	    	
	    	if(Global.didNotChoose(p)){
	    		pfunc.removeNameTag(p);
	    		Global.removeNoChoose(p);
	    	}
	    	
	    	Location loc = (Location) spawns.getConfig().get("spawns." + id + ".location");
	    	
	    	//先讓名條被刪除再傳送
	    	Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	    		
	    		@Override
	    		public void run(){
	    	    	p.teleport(loc);
	    	    	
	    	    	if(Global.isGhost(p)){
	    	    		pfunc.TurnBack(p);
	    	    	}
	    	    	
	    	    	p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
	    		}
	    		
	    	}, 2);
	    	
	   	}
	}
		
	//改名稱系統
	@EventHandler
	public void onCloseAnvil(InventoryCloseEvent e){
		
	    Inventory inv = e.getInventory();
	    Player p = (Player) e.getPlayer();
	    
	    if(!inv.getType().equals(InventoryType.ANVIL)){
	    	return;
	    }
	    if(inv.getItem(0) == null){
	    	return;
	    }
	    
	    ItemStack item = inv.getItem(0);
	    
	    if(!item.getItemMeta().hasLore()){
	    	return;
	    }
	    
	    if(item.getItemMeta().getLore().get(0).toString().equals(ChatColor.GOLD + "更新前名稱:")){
	    	inv.setItem(0, null);
	    	inv.setItem(1, null);
	    	p.sendMessage(Global.Header + ChatColor.RED + "已取消重新命名復活點");
	    	p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1, 1);
	    }
	    
	}
	@EventHandler
	public void onAnvilClick(InventoryClickEvent e){
		
		if(e.getClickedInventory() == null){
			return;
		}
		if(!(e.getWhoClicked() instanceof Player)){
	    	return;
	    }
	    if(!e.getClickedInventory().getType().equals(InventoryType.ANVIL)){
	    	return;
	    }
	    if(e.getInventory().getItem(0) == null){
	    	return;
	    }
	    
	    ItemStack item0 = e.getClickedInventory().getItem(0);
	    if(!item0.getItemMeta().hasLore()){
	    	return;
	    }
	    
	    List<String> lore0 = item0.getItemMeta().getLore();
	    
	    if(!lore0.get(0).toString().equals(ChatColor.GOLD + "更新前名稱:")){
	    	return;
	    }
	    
	    e.setCancelled(true);
	    
	    Player p = (Player) e.getWhoClicked();
	    InventoryView view = e.getView();
	    int rawSlot = e.getRawSlot();
	    
	    if(rawSlot == view.convertSlot(rawSlot)){
	    	
	    	if(rawSlot == 0){
	    		
	    		if(e.getInventory().getItem(1) != null){
	    			e.getClickedInventory().setItem(1, null);
	    		}
	    		
	    		p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	        
	    	}else if(rawSlot == 1){

	    		if(e.getInventory().getItem(2) == null){
	    			
	    			//再次點擊關閉預覽
	    			if(e.getInventory().getItem(1) != null){
		    			e.getClickedInventory().setItem(1, null);
		    			
		    			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
		    		}
	    			
	    			return;
	    			
	    		}
	    		
	    		ItemMeta meta2 = e.getClickedInventory().getItem(2).getItemMeta();
	    		
	    		String name0 = item0.getItemMeta().getDisplayName().replace("&", "§").replace("/§", "&");
	    		
	    		String name2 = meta2.getDisplayName().replace("&", "§").replace("/§", "&");
	    		
	    		List<String> lore1 = new ArrayList<String>();
	    		
	    		lore1.add(ChatColor.GOLD + "更新前名稱:");
	    		lore1.add(name0);
	    		lore1.add("");
	    		lore1.add(ChatColor.GOLD + "更新後名稱:");
	    		lore1.add(name2);
	    		lore1.add("");
	    		lore1.add(ChatColor.GREEN + "再次點擊此格關閉預覽");
	    		
	    		e.getClickedInventory().setItem(1, im.createItem(Material.ENCHANTED_BOOK, 0, ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "名稱預覽", lore1, false));
	    		
	    		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
	    		
	    	}else if(rawSlot == 2){
	    		
	    		ItemStack item2 = e.getInventory().getItem(2);
	    		
	    		if(item2 == null){
	    			return;
	    		}
	    		
	    		String newname = item2.getItemMeta().getDisplayName().replace("&", "§").replace("/§", "&");
	    		
	    		String id = lore0.get(8).replace(ChatColor.DARK_GRAY + "ID:", "");
	    		
	    		if(ChatColor.stripColor(newname).startsWith(" ") || ChatColor.stripColor(newname).endsWith(" ")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	          
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "開頭和結尾不能有空格!");
	    			return;
	    		}
	    		
	    		//將多個連續的空格變成一個
	    		if(newname.contains(" ")){
	    			String[] l = newname.split(" ");
	    			newname = "";
	    		  
	    			for(int i = 0; i < l.length; i++){
	    			  
	    				if(newname != ""){
	    					if(!l[i].isEmpty()){
	    						newname += " " + l[i];
	    					}
	    				}else{
	    					newname = l[i];
	    				}
	    			}
	    		}
	    	  
	    		for(String temp_id : spawns.getConfig().getConfigurationSection("spawns").getKeys(false)){
	          
	    			if(spawns.getConfig().getString("spawns." + temp_id + ".name").equals(newname)){
	    				p.sendMessage(Global.Header + ChatColor.DARK_RED + "這個名字已經用過了!");
	    				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    				return;
	    			}
	    		}
	        
	    		if(ChatColor.stripColor(newname).equals("")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "你似乎沒有輸入文字或只輸入格式碼!");
	    			return;
	    		}
	    		if(ChatColor.stripColor(newname).contains("§")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "要在名稱中顯示&，請使用/&");
	    			return;
	    		}
	    		
	    		spawns.set("spawns." + id + ".name", newname);
	    		
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "已將復活點名稱改為 " + ChatColor.WHITE + ChatColor.BOLD + "[" + ChatColor.RESET + newname + ChatColor.WHITE + ChatColor.BOLD + "]");
	    		p.getOpenInventory().setItem(0, null);
	    		p.getOpenInventory().setItem(1, null);
	    		p.closeInventory();
	    		p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
	      	}
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
    					
	    				if(Global.isGhost(entp) && (!Global.isInTargetEntity(entp))){
	    					
	    					if(Global.didNotChoose(entp)){
	    						Global.removeNoChoose(entp);
	    						entp.closeInventory();
	    					}
	    					if(Global.hasIds(entp)){
	    						core.getServer().getScheduler().cancelTask(((Integer) Global.getIds(entp)).intValue());
	    						Global.removeIds(entp);
	    					}
	    					
	    					pfunc.removeNameTag(entp);
	    					entp.teleport(entp.getLocation().add(0.0, 0.2, 0.0));
	    					pfunc.TurnBack(entp);
	              
	    					nms.sendTitle(entp, "", 0, 0, 0);
	    					nms.sendSubTitle(entp, 
	    							ChatColor.DARK_GREEN + p.getName() + "用" + ChatColor.RESET + assistant_respawn_item.getItemMeta().getDisplayName() + ChatColor.DARK_GREEN + "讓你在這裡復活",
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
