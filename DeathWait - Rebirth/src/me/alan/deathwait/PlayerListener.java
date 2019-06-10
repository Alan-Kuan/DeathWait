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

	String Header = ChatColor.GOLD + "[DeathWait]";
	
	private Core core;
	private WarningGen warn;
	private Config config;
	private Spawns spawns;
	private Data data;
	private ListSpawns list;
	private PlayerFunctions pfunc;
	private AnvilGUI anvil;
	private NMS nms;
	private Globalvar GV;
	private ItemMaker im;
	
	public PlayerListener(Core core){
		
		this.core = core;
		
		warn = new WarningGen(core);
		config = core.getConfigClass();
		spawns = core.getSpawnsClass();
		data = core.getDataClass();
		pfunc = core.getPlayerFunctionsClass();
		list = new ListSpawns(core, pfunc);
		anvil = core.getAnvilGUIClass();
		nms = core.getNMSClass();
		GV = core.getGlobalvarClass();
		im = new ItemMaker();
		
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e){
		
	    if(!(e.getEntity() instanceof Player)){
	    	return;
	    }
	    
	    final Player p = e.getEntity();
	    
	    String keepinv = p.getWorld().getGameRuleValue("keepInventory");
	    boolean effect = config.getConfig().getBoolean("config.remove effects");
	    boolean xp = config.getConfig().getBoolean("config.lose XP");
	    boolean view = config.getConfig().getBoolean("config.killer view");
	    boolean canmove = config.getConfig().getBoolean("config.can move");
	    boolean food = config.getConfig().getBoolean("config.reset food level");
	    
	    GV.setGameMode(p, p.getGameMode());;
	    GV.addGhost(p);
	    p.setHealth(pfunc.getMaxHealth(p));
	    p.setVelocity(new Vector(0, 0, 0));
	    e.getDrops().clear();

	    //踢下騎在該玩家身上的生物
	    pfunc.kickPassenger(p);
	    
	    //取消騎乘
	    p.teleport(p.getLocation());
	    
	    //自殺
	    if(GV.isKilled(p)){
	    	e.setDeathMessage(p.getName() + " 已死亡");
	    	GV.removeKilled(p);
	    }
	    
	    //若掉進虛空
	    double x;
	    if((p.getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.VOID)) && (p.getLocation().getY() < 0.0D)){
	    	World w = p.getWorld();
	    	x = p.getLocation().getX();
	    	double z = p.getLocation().getZ();
	    	
	    	p.teleport(new Location(w, x, -2.0D, z));
	    }
	    
	    //清除藥水效果
	    if(effect){
	    	for(PotionEffect ef : p.getActivePotionEffects()){
	    		p.removePotionEffect(ef.getType());
	    	}
	    }
	    
	    if(xp){
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
	    
	    if(view && (p.getLastDamageCause() instanceof EntityDamageByEntityEvent)){
	    	
	    	EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) p.getLastDamageCause();
	    	Entity killer = edbee.getDamager();
	    	Entity shooter = pfunc.getShooter(killer);
	    	
	    	if((shooter != null) && (!killer.equals(shooter)) && (!p.equals(shooter))){
	    		nms.setSpectate(p, shooter);
	    		GV.addTargetEntity(shooter, p);
	    	}else if((killer.equals(shooter)) && (!(killer instanceof Creeper)) && (!(killer instanceof EnderCrystal)) && (!(killer instanceof FallingBlock)) && (!(killer instanceof TNTPrimed)) && (!(killer instanceof ExplosiveMinecart))){
	    		nms.setSpectate(p, killer);
	    		GV.addTargetEntity(shooter, p);
	    	}
	    	
	    }
	    
	    if(canmove){
	    	p.setFlySpeed(0.1f);
	    }else{
	    	p.setFlySpeed(0);
	    }
	    
	    if(food){
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
	    pfunc.setNameTag(p);
	    
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
	    	
		    int countdown = core.getServer().getScheduler().scheduleSyncRepeatingTask(core, new Runnable(){
		     
		    	int wait = config.getConfig().getInt("config.wait seconds") + 1;
		    	
		    	@Override
		    	public void run(){
		    		
		    		GV.setLeftWaitingTimes(p, wait);
		    		
		    		if(wait > 0){
		    			wait--;

		    			if(wait != 0){
		    				String show = wait + "秒後復活";
		            
		    				nms.sendSubTitle(p, ChatColor.GOLD + show);
		    				nms.sendTitle(p, ChatColor.RED + "你已經死了");
		    			}
		    		}else{
		    			core.getServer().getScheduler().cancelTask(GV.getIds(p));
		    			GV.removeIds(p);
		    			pfunc.Respawn(p);
		    		}
		    		
		    	}
		    	
		    }, 0L, 20L);
		    GV.setIds(p, countdown);
		    
	    }else{
	    	
	    	pfunc.Respawn(p);
	    	
	    }
	    
	}
	
	//當裡面有靈魂的生物死亡，靈魂被釋放
	@EventHandler
	public void onTargetEntityDeath(EntityDeathEvent e){
		
	    Entity ent = e.getEntity();
	    
	    if(GV.isTargetEntity(ent)){
	    	Player p = (Player) GV.getPlayerInTargetEntity(ent);
	    	nms.setSpectate(p, p);
	    	GV.removeTargetEntity(ent, p);
	    }
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		
		Player p = e.getPlayer();
				
		//如果退出的玩家裡面有其他玩家的靈魂，靈魂被釋放
	    if(GV.isTargetEntity(p)){
	    	Player victim = (Player) GV.getPlayerInTargetEntity(p);
	    	
	    	nms.setSpectate(victim, victim);
	    	GV.removeTargetEntity(p, victim);
	    }
	    
	    //如果退出的玩家是幽靈
	    if(GV.isGhost(p)){

			//pfunc.removeNameTag(p);
	    	
	    	data.set("players." + p.getUniqueId() + ".gamemode", GV.getGameMode(p).toString());
	    	
	    	if(GV.hasLeftWaitingTimes(p)){
		    	core.getServer().getScheduler().cancelTask(GV.getIds(p));
		    	data.set("players." + p.getUniqueId() + ".left waiting times", GV.getLeftWaitingTimes(p));
		    	
		    	GV.removeLeftWaitingTimes(p);
		    	GV.removeIds(p);
	    	}

	    	GV.removeGameMode(p);
			GV.removeGhost(p);
			GV.removeKilled(p);
			
			if(GV.isInTargetEntity(p)){
				for(Entity target: GV.getTargetEntities()){

					if(GV.getPlayerInTargetEntity(target).equals(p)){
						GV.removeTargetEntity(target, p);
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
			
			boolean canmove = config.getConfig().getBoolean("config.can move");
			
			GV.addGhost(p);
			GV.setGameMode(p, GameMode.valueOf(data.getConfig().getString("players." + p.getUniqueId() + ".gamemode")));
						
			data.set("players." + p.getUniqueId() + ".gamemode", null);
			
			//顯示名條		    
		    pfunc.setNameTag(p);
		    
			int countdown = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(core, new Runnable(){
			    
				int left = data.getConfig().getInt("players." + p.getUniqueId() + ".left waiting times");
				
		    	@Override
		    	public void run(){
		    		
		    		if(canmove){
						p.setFlySpeed(0.1f);
					}else{
						p.setFlySpeed(0);
					}
		    		
		    		GV.setLeftWaitingTimes(p, left);
		    		
		    		if(left > 0){
		    			
		    			left--;

		    			if(left != 0){
		    				String show = left + "秒後復活";
		    				
		    				nms.sendSubTitle(p, ChatColor.GOLD + show);
		    				nms.sendTitle(p, ChatColor.RED + "由於上次在等待時登出，所以必須繼續等待");
		    				
		    			}
		    			
		    		}else{
		    			
		    			core.getServer().getScheduler().cancelTask(GV.getIds(p));
		    			GV.removeIds(p);
		    			data.set("players." + p.getUniqueId() + ".left waiting times", null);
		    			pfunc.Respawn(p);
		    			
		    		}
		    		
		    	}
		    	
		    }, 0L, 20L);
		    GV.setIds(p, countdown);		    
		}
		
	}
	
	//幽靈模式禁用指令
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent e){
		
	    Player p = e.getPlayer();
	    
		if(GV.isGhost(p)){
			e.setCancelled(true);
	    }
		
	}
	
	//防止還沒選復活點就關閉畫面
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent e){
		
	    final Player p = (Player) e.getPlayer();
	    Inventory gui = e.getInventory();
	    
	    if(!GV.hasTurnedPage(p)){
	    	
	    	if(gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "所有復活點") && GV.didNotChoose(p)){
	    	    
	    		try{
	    			
	    			String s = gui.getItem(31).getItemMeta().getDisplayName().replace("§9-第", "").replace("頁-", "");
	    			final int nowat = Integer.parseInt(s);
	    			
	    			Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	          
	    				@Override
	    				public void run(){
	    					list.List(p, nowat);
	    				}
	    				
	    			}, 1L);
	    			
	    		}catch(NumberFormatException ex){
	    			
	    			ex.printStackTrace();
	    			warn.Warn("在獲取目前所在頁數時出了問題");
	    			
	    		}
	    		
	    	}
	    	
	    }else{
	    	GV.removeTurnPage(p);
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
	    
	    e.setCancelled(true);
	    
	    //回到自然重生點
	    if((click.equals(ClickType.LEFT)) && (slot == 27) && (name.equals(ChatColor.DARK_GREEN + "自然重生點"))){
		    
	    	if(GV.didNotChoose(p)){
		    	pfunc.removeNameTag(p);
	    		GV.removeNoChoose(p);
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
	    		int nowat = Integer.parseInt(s);
	    		
	    		if (nowat - 1 > 0){
	    			GV.addTurnPage(p);
	    			list.List(p, nowat - 1);
	    		}
	    			
	    	}catch(NumberFormatException ex){
	    		ex.printStackTrace();
	    		warn.Warn("在獲取目前所在頁數時出了問題");
	    	}
	    }
	    	
	    //下一頁
	    if((click.equals(ClickType.LEFT)) && (slot == 32) && (name.equals(ChatColor.BLUE + "下一頁"))){
	    	
	    	p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	    		
	    	try{
	    			
	    		String s = gui.getItem(31).getItemMeta().getDisplayName().replace("§9-第", "").replace("頁-", "");
	    		int nowat = Integer.parseInt(s);
	        
	    		String totalstr = gui.getItem(31).getItemMeta().getLore().get(0).toString().replace("§2 共", "").replace("頁", "");
	    		int total = Integer.parseInt(totalstr);
	    			
	    		if(nowat + 1 <= total){
	    			GV.addTurnPage(p);
	    			list.List(p, nowat + 1);
	    		}
	    			
	    	}catch(NumberFormatException ex){
	    		ex.printStackTrace();
	    		warn.Warn("在獲取目前所在頁數或總頁數時出了問題");
	    	}
	    }
	    	
	    if((lore.size() == 10) && (slot <= 26)){
	    		
	    	//重設復活點
	    	if(click.equals(ClickType.SHIFT_RIGHT)){
	    	    
	    		GV.setSame(name);
	    		p.performCommand("dw set " + name.replace("&", "/&"));
	    		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
	    	}
	    		
	    	//移除復活點
	    	if(click.equals(ClickType.SHIFT_LEFT)){
	    	    
	    		String id = lore.get(0).toString().replace("§bID:", "");
	    		spawns.set("spawns." + id, null);
	    		
	    		p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
	    			
	    		try{
	    			String s = gui.getItem(31).getItemMeta().getDisplayName().replace("§9-第", "").replace("頁-", "");
	    			int nowat = Integer.parseInt(s);
	    				
	    			if ((nowat > 1) && (slot == 0) && (gui.getItem(1) == null)) {
	    				nowat--;
	    			}
	    				
	    			list.List(p, nowat);
	    		}catch(NumberFormatException ex){
	    			ex.printStackTrace();
	    			warn.Warn("在獲取目前所在頁數時出了問題");
	    		}
	    		p.sendMessage(Header + ChatColor.DARK_RED + "已將復活點 §f§l[§r" + name + "§f§l] " + ChatColor.RESET + ChatColor.DARK_RED + "移除!");
	    	}
	    		
	    	//更改圖示
	    	if(e.getAction() == InventoryAction.SWAP_WITH_CURSOR){
	    		
	    		ItemStack icon = e.getCursor();
	    		ItemMeta meta = icon.getItemMeta();
	    		String id = lore.get(0).toString().replace("§bID:", "");
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
	    		p.sendMessage(Header + ChatColor.GREEN + "已更新復活點圖示");
	    		
	    		return;
	    		
	    	}
	    	
	    	//重新命名復活點
	    	if(click.equals(ClickType.RIGHT)){
	    	    
	    		List<String> info = new ArrayList<String>();
	    		
	    		info.add(ChatColor.GOLD + "更新前名稱:");
	    		info.add(name);
	    		info.add("");
	    		info.add(ChatColor.GOLD + "更新後名稱:");
	    		info.add(name);
	    		info.add("");
	    		info.add(ChatColor.DARK_RED + "備註: 若要在名稱中顯示'&'，請打'/&'");
	        
	    		anvil.openAnvil(p, name, info);
	    		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1);
	    	}
	    }
	    
	    //傳送至復活點
	    if((click.equals(ClickType.LEFT)) && (slot <= 26)){
	    	
	    	if(GV.didNotChoose(p)){
	    		pfunc.removeNameTag(p);
	    		GV.removeNoChoose(p);
	    	}
	    	
	    	String id = lore.get(0).toString().replace("§bID:", "");
	    	Location loc = (Location) spawns.getConfig().get("spawns." + id + ".location");
	    	
	    	//先讓名條被刪除再傳送
	    	Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	    		
	    		@Override
	    		public void run(){
	    	    	p.teleport(loc);
	    	    	
	    	    	if(GV.isGhost(p)){
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
	    	p.sendMessage(Header + ChatColor.RED + "已取消重新命名復活點");
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
	    
	    ItemStack item = e.getClickedInventory().getItem(0);
	    if(!item.getItemMeta().hasLore()){
	    	return;
	    }
	    
	    if(!item.getItemMeta().getLore().get(0).toString().equals(ChatColor.GOLD + "更新前名稱:")){
	    	return;
	    }
	    
	    e.setCancelled(true);
	    
	    Player p = (Player) e.getWhoClicked();
	    InventoryView view = e.getView();
	    int rawSlot = e.getRawSlot();
	    
	    if(rawSlot == view.convertSlot(rawSlot)){
	    	
	    	if(rawSlot == 0){
	    	  
	    		p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	        
	    	}else if(rawSlot == 1){
	    	  
	    		p.sendMessage(Header + ChatColor.RED + "已取消重新命名復活點");
	    		p.getOpenInventory().setItem(0, null);
	    		p.closeInventory();
	    		p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1, 1);
	        
	    	}else if(rawSlot == 2){
	    	  
	    		if(e.getInventory().getItem(2) == null){
	    			return;
	    		}
	    	  
	    		ItemStack button = e.getCurrentItem();
	    		String name = "";
	    		
	    		if(button.getItemMeta().hasDisplayName()){
	    			name = button.getItemMeta().getDisplayName().replace('&', '§').replace("/§", "&");
	    		}
	    	    
	    		List<String> lore = new ArrayList<String>();
	    	  
	    		if(button.getItemMeta().hasLore()) {
	    			lore = button.getItemMeta().getLore();
	    		}
	    	  
	    		if(name.endsWith(" ")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	          
	    			p.sendMessage(Header + ChatColor.DARK_RED + "結尾不能有空格!");
	    			return;
	    		}
	    		
	    		//將多個連續的空格變成一個
	    		int i;
	    		if(name.contains(" ")){
	    			String[] l = name.split(" ");
	    			name = "";
	    		  
	    			for(i = 0; i < l.length; i++){
	    			  
	    				if(name != ""){
	    					if(!l[i].isEmpty()){
	    						name = name + " " + l[i];
	    					}
	    				}else{
	    					name = l[i];
	    				}
	    			}
	    		}
	    	  
	    		for(String id : spawns.getConfig().getConfigurationSection("spawns").getKeys(false)){
	          
	    			if(spawns.getConfig().getString("spawns." + id + ".name").equals(name)){
	    				p.sendMessage(Header + ChatColor.DARK_RED + "這個名字已經用過了!");
	    				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    				return;
	    			}
	    		}
	        
	    		if(ChatColor.stripColor(name).equals("")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(this.Header + ChatColor.DARK_RED + "你似乎沒有輸入文字或只輸入格式碼!");
	    			return;
	    		}
	    		if(ChatColor.stripColor(name).contains("§")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(this.Header + ChatColor.DARK_RED + "名稱中不可以含有不具意義格式碼!");
	    			return;
	    		}
	    		
	    		String oldname = lore.get(1).toString();
	    		String rightid = "";
	    		for(String id : spawns.getConfig().getConfigurationSection("spawns").getKeys(false)){
	        	
	    			if(spawns.getConfig().getString("spawns." + id + ".name").equals(oldname)){
	    				rightid = id;
	    				break;
	    			}
	    		}
	    		
	    		spawns.set("spawns." + rightid + ".name", name);

	    		p.sendMessage(this.Header + ChatColor.GREEN + "已將復活點名稱改為 " + ChatColor.WHITE + ChatColor.BOLD + "[" + ChatColor.RESET + name + ChatColor.WHITE + ChatColor.BOLD + "]");
	    		p.getOpenInventory().setItem(0, null);
	    		p.closeInventory();
	    		p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
	      	}
	    }
	}
	
	//使用道具
	@EventHandler
	public void onUseItem(PlayerInteractEvent e){
			    
	    Player p = e.getPlayer();
	    PlayerInventory pi = p.getInventory();
	    Action action = e.getAction();
	    ItemStack item;
	    
		if(e.getHand() == EquipmentSlot.HAND){
			item = pi.getItemInMainHand();
	    }else if(e.getHand() == EquipmentSlot.OFF_HAND){
	    	item = pi.getItemInOffHand();
	    }else{
	    	return;
	    }
		
		if(item.getType() == Material.AIR){
			return;
		}
		
	    ItemMeta meta = item.getItemMeta();
	    
	    ItemStack Respawn = im.createItem(Material.getMaterial(config.getConfig().getString("config.respawn item.type")),
				config.getConfig().getInt("config.respawn item.damage"),
				config.getConfig().getString("config.respawn item.name"),
				config.getConfig().getStringList("config.respawn item.lore"), true);
		ItemStack Here = im.createItem(Material.getMaterial(config.getConfig().getString("config.respawn right here item.type")),
				config.getConfig().getInt("config.respawn right here item.damage"),
				config.getConfig().getString("config.respawn right here item.name"),
				config.getConfig().getStringList("config.respawn right here item.lore"), true);
	    
	    if(!meta.hasLore()){
	    	return;
	    }
	    if(!meta.hasDisplayName()){
	    	return;
	    }
	    
	    if((action.equals(Action.RIGHT_CLICK_AIR)) || (action.equals(Action.RIGHT_CLICK_BLOCK))){
	    	
	    	//直接復活道具
	    	if((meta.getDisplayName().equals(Respawn.getItemMeta().getDisplayName())) && (meta.getLore().equals(Respawn.getItemMeta().getLore()))){
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
	    				pi.setItemInMainHand(null);
	    			}else{
	    				pi.setItemInOffHand(null);
	    			}

	    		}
	    		
	    		int quota = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
	    		
	    		p.sendMessage(Header + ChatColor.GREEN + "已增加一次免等額度");
	    		p.sendMessage(Header + ChatColor.GREEN + "剩餘" + quota + "次免等額度");
	    	}
	    	
	    	//原地復活道具
	    	if((meta.getDisplayName().equals(Here.getItemMeta().getDisplayName())) && (meta.getLore().equals(Here.getItemMeta().getLore()))){
	    		e.setCancelled(true);
	    		
	    		for(Entity ent : p.getNearbyEntities(1.0D, 0.0D, 1.0D)){
	    			if((ent instanceof Player)){

    					Player entp = (Player) ent;
    					
	    				if(GV.isGhost(entp) && (!GV.isInTargetEntity(entp))){
	    					
	    					if(GV.didNotChoose(entp)){
	    						GV.removeNoChoose(entp);
	    						entp.closeInventory();
	    					}
	    					if(GV.hasIds(entp)){
	    						core.getServer().getScheduler().cancelTask(((Integer) GV.getIds(entp)).intValue());
	    						GV.removeIds(entp);
	    					}
	    					
	    					pfunc.removeNameTag(entp);
	    					entp.teleport(entp.getLocation().add(0.0D, 0.2D, 0.0D));
	    					pfunc.TurnBack(entp);
	              
	    					nms.sendTitle(entp, "");
	    					nms.sendSubTitle(entp, ChatColor.DARK_GREEN + p.getName() + "用" + ChatColor.RESET + config.getConfig().getString("config.respawn right here item.name").replace('&', '§').replace("/§", "&") + ChatColor.DARK_GREEN + "讓你在這裡復活");
	    					entp.getWorld().spawnEntity(entp.getLocation(), EntityType.FIREWORK);
	    					if(item.getAmount() > 1){
	    						item.setAmount(item.getAmount() - 1);
	    						break;
	    					}

	    	    			if(e.getHand() == EquipmentSlot.HAND){
	    	    				pi.setItemInMainHand(null);
	    	    			}else{
	    	    				pi.setItemInOffHand(null);
	    	    			}
	              
	    					break;
	    				}
	    			}
	    		}
	    	}
	    }
	    if((action.equals(Action.LEFT_CLICK_AIR)) || (action.equals(Action.LEFT_CLICK_BLOCK))){
	    	if((meta.getDisplayName().equals(Respawn.getItemMeta().getDisplayName())) && (meta.getLore().equals(Respawn.getItemMeta().getLore()))){
	    		e.setCancelled(true);
	    		
	    		int quota = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
	    		p.sendMessage(Header + ChatColor.GREEN + "剩餘" + quota + "次免等額度");
	    	}
	    }
	}
	
}
