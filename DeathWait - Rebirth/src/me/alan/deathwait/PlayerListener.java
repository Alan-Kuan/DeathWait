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

	    //��U�M�b�Ӫ��a���W���ͪ�
	    pfunc.kickPassenger(p);
	    
	    //�����M��
	    p.teleport(p.getLocation());
	    
	    //�۱�
	    if(Global.isKilled(p)){
	    	e.setDeathMessage(p.getName() + " �w���`");
	    	Global.removeKilled(p);
	    }
	    
	    //�Y���i���
	    if((p.getLastDamageCause().getCause().equals(EntityDamageEvent.DamageCause.VOID)) && (p.getLocation().getY() < 0.0)){
	    	World w = p.getWorld();
	    	double x = p.getLocation().getX();
	    	double z = p.getLocation().getZ();
	    	
	    	p.teleport(new Location(w, x, -2.0, z));
	    }
	    
	    //�M���Ĥ��ĪG
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
	    
	    //�̷�Gamerule�ӨM�w�n���n�Q�D��
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
	    
	    //��ܦW��
	    pfunc.setNameTag(p);
	    
	    boolean needwait = true;
	    
	    if(p.hasPermission("dw.bypass")){
	    	
	    	needwait = false;
	    	
	    //�p�G�S���v���~�|�������_���B��
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
	    	    
	    //�ݭn����
	    if(needwait){
	    	
		    int countdown = core.getServer().getScheduler().scheduleSyncRepeatingTask(core, new Runnable(){
		     
		    	int wait = config.getConfig().getInt("config.waiting seconds") + 1;
		    	
		    	@Override
		    	public void run(){
		    		
		    		Global.setLeftWaitingTimes(p, wait);
		    		
		    		if(wait > 0){
		    			wait--;

		    			if(wait != 0){
		    				String show = wait + "���_��";
		            
		    				nms.sendSubTitle(p, ChatColor.GOLD + show, 0, 25, 0);
		    				nms.sendTitle(p, ChatColor.RED + "�A�w�g���F");
		    			}
		    		}else{
		    			core.getServer().getScheduler().cancelTask(Global.getIds(p));
		    			Global.removeIds(p);
		    			pfunc.Respawn(p);
		    		}
		    		
		    	}
		    	
		    }, 0L, 20L);
		    Global.setIds(p, countdown);
		    
	    }else{
	    	
	    	pfunc.Respawn(p);
	    	
	    }
	    
	}
	
	//��̭����F��ͪ����`�A�F��Q����
	@EventHandler
	public void onTargetEntityDeath(EntityDeathEvent e){
		
	    Entity ent = e.getEntity();
	    
	    if(Global.isTargetEntity(ent)){
	    	Player p = (Player) Global.getPlayerInTargetEntity(ent);
	    	nms.setSpectate(p, p);
	    	Global.removeTargetEntity(ent, p);
	    }
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		
		Player p = e.getPlayer();
				
		//�p�G�h�X�����a�̭�����L���a���F��A�F��Q����
	    if(Global.isTargetEntity(p)){
	    	Player victim = (Player) Global.getPlayerInTargetEntity(p);
	    	
	    	nms.setSpectate(victim, victim);
	    	Global.removeTargetEntity(p, victim);
	    }
	    
	    //�p�G�h�X�����a�O���F
	    if(Global.isGhost(p)){

			//pfunc.removeNameTag(p);
	    	
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
	//�p�G�b���ݮɵn�X�A�U���n�J�ɭn�~�򵥫�
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
				
		Player p = e.getPlayer();
		
		boolean have_to_wait = data.getConfig().isSet("players." + p.getUniqueId() + ".left waiting times");
		
		if(have_to_wait){
			
			boolean allow_moving = config.getConfig().getBoolean("config.allow moving in ghost mode");
			
			Global.addGhost(p);
			Global.setGameMode(p, GameMode.valueOf(data.getConfig().getString("players." + p.getUniqueId() + ".gamemode")));
						
			data.set("players." + p.getUniqueId() + ".gamemode", null);
			
			//��ܦW��		    
		    pfunc.setNameTag(p);
		    
			int countdown = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(core, new Runnable(){
			    
				int left = data.getConfig().getInt("players." + p.getUniqueId() + ".left waiting times");
				
		    	@Override
		    	public void run(){
		    		
		    		if(allow_moving){
						p.setFlySpeed(0.1f);
					}else{
						p.setFlySpeed(0);
					}
		    		
		    		Global.setLeftWaitingTimes(p, left);
		    		
		    		if(left > 0){
		    			
		    			left--;

		    			if(left != 0){
		    				String show = left + "���_��";
		    				
		    				nms.sendSubTitle(p, ChatColor.GOLD + show, 0, 25, 0);
		    				nms.sendTitle(p, ChatColor.RED + "�ѩ�W���b���ݮɵn�X�A�ҥH�����~�򵥫�");
		    				
		    			}
		    			
		    		}else{
		    			
		    			core.getServer().getScheduler().cancelTask(Global.getIds(p));
		    			Global.removeIds(p);
		    			data.set("players." + p.getUniqueId() + ".left waiting times", null);
		    			pfunc.Respawn(p);
		    			
		    		}
		    		
		    	}
		    	
		    }, 0L, 20L);
		    Global.setIds(p, countdown);		    
		}
		
	}
	
	//���F�Ҧ��T�Ϋ��O
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent e){
		
	    Player p = e.getPlayer();
	    
		if(Global.isGhost(p)){
			e.setCancelled(true);
	    }
		
	}
	
	//�����٨S��_���I�N�����e��
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent e){
		
	    final Player p = (Player) e.getPlayer();
	    Inventory gui = e.getInventory();
	    
	    if(!Global.hasTurnedPage(p)){
	    	
	    	if(gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "�Ҧ��_���I") && Global.didNotChoose(p)){
	    	    
	    		try{
	    			
	    			String s = gui.getItem(31).getItemMeta().getDisplayName().replace("��9-��", "").replace("��-", "");
	    			final int nowat = Integer.parseInt(s);
	    			
	    			Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	          
	    				@Override
	    				public void run(){
	    					list.List(p, nowat);
	    				}
	    				
	    			}, 1L);
	    			
	    		}catch(NumberFormatException ex){
	    			
	    			ex.printStackTrace();
	    			WarningGen.Warn("�b����ثe�Ҧb���ƮɥX�F���D");
	    			
	    		}
	    		
	    	}
	    	
	    }else{
	    	Global.removeTurnPage(p);
	    }
	}
	
	//�_���I�ؿ������s
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
	    
	    if(!gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "�Ҧ��_���I")) {
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
	    
	    //�^��۵M�����I
	    if((click.equals(ClickType.LEFT)) && (slot == 27) && (name.equals(ChatColor.DARK_GREEN + "�۵M�����I"))){
		    
	    	if(Global.didNotChoose(p)){
		    	pfunc.removeNameTag(p);
	    		Global.removeNoChoose(p);
	    	}
	    	
	    	//�����W���Q�R���A�ǰe
	    	Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	    		
	    		@Override
	    		public void run(){
	    	    	pfunc.tpNormalSpawnPoint(p);
	    	    	
	    	    	p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
	    		}
	    		
	    	}, 2);
	    	
	    }
	    
	    //�W�@��
	    if((click.equals(ClickType.LEFT)) && (slot == 30) && (name.equals(ChatColor.BLUE + "�W�@��"))){
	    	
	    	p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	    		
	    	try{
	    			
	    		String s = gui.getItem(31).getItemMeta().getDisplayName().replace("��9-��", "").replace("��-", "");
	    		int nowat = Integer.parseInt(s);
	    		
	    		if (nowat - 1 > 0){
	    			Global.addTurnPage(p);
	    			list.List(p, nowat - 1);
	    		}
	    			
	    	}catch(NumberFormatException ex){
	    		ex.printStackTrace();
	    		WarningGen.Warn("�b����ثe�Ҧb���ƮɥX�F���D");
	    	}
	    }
	    	
	    //�U�@��
	    if((click.equals(ClickType.LEFT)) && (slot == 32) && (name.equals(ChatColor.BLUE + "�U�@��"))){
	    	
	    	p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	    		
	    	try{
	    			
	    		String s = gui.getItem(31).getItemMeta().getDisplayName().replace("��9-��", "").replace("��-", "");
	    		int nowat = Integer.parseInt(s);
	        
	    		String totalstr = gui.getItem(31).getItemMeta().getLore().get(0).toString().replace("��2 �@", "").replace("��", "");
	    		int total = Integer.parseInt(totalstr);
	    			
	    		if(nowat + 1 <= total){
	    			Global.addTurnPage(p);
	    			list.List(p, nowat + 1);
	    		}
	    			
	    	}catch(NumberFormatException ex){
	    		ex.printStackTrace();
	    		WarningGen.Warn("�b����ثe�Ҧb���Ʃ��`���ƮɥX�F���D");
	    	}
	    }
	    	
	    if((lore.size() == 10) && (slot <= 26)){
	    		
	    	//���]�_���I
	    	if(click.equals(ClickType.SHIFT_RIGHT)){
	    	    
	    		Global.setTempName(name);
	    		p.performCommand("dw set " + name.replace("&", "/&"));
	    		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
	    	}
	    		
	    	//�����_���I
	    	if(click.equals(ClickType.SHIFT_LEFT)){
	    	    
	    		String id = lore.get(0).toString().replace("��bID:", "");
	    		spawns.set("spawns." + id, null);
	    		
	    		p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
	    			
	    		try{
	    			String s = gui.getItem(31).getItemMeta().getDisplayName().replace("��9-��", "").replace("��-", "");
	    			int nowat = Integer.parseInt(s);
	    				
	    			if ((nowat > 1) && (slot == 0) && (gui.getItem(1) == null)) {
	    				nowat--;
	    			}
	    				
	    			list.List(p, nowat);
	    		}catch(NumberFormatException ex){
	    			ex.printStackTrace();
	    			WarningGen.Warn("�b����ثe�Ҧb���ƮɥX�F���D");
	    		}
	    		p.sendMessage(Global.Header + ChatColor.DARK_RED + "�w�N�_���I ��f��l[��r" + name + "��f��l] " + ChatColor.RESET + ChatColor.DARK_RED + "����!");
	    	}
	    		
	    	//���ϥ�
	    	if(e.getAction() == InventoryAction.SWAP_WITH_CURSOR){
	    		
	    		ItemStack icon = e.getCursor();
	    		ItemMeta meta = icon.getItemMeta();
	    		String id = lore.get(0).toString().replace("��bID:", "");
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
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�w��s�_���I�ϥ�");
	    		
	    		return;
	    		
	    	}
	    	
	    	//���s�R�W�_���I
	    	if(click.equals(ClickType.RIGHT)){
	    	    
	    		List<String> info = new ArrayList<String>();
	    		
	    		info.add(ChatColor.GOLD + "��s�e�W��:");
	    		info.add(name);
	    		info.add("");
	    		info.add(ChatColor.GOLD + "��s��W��:");
	    		info.add(name);
	    		info.add("");
	    		info.add(ChatColor.DARK_RED + "�Ƶ�: �Y�n�b�W�٤����'&'�A�Х�'/&'");
	        
	    		anvil.openAnvil(p, name, info);
	    		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1);
	    	}
	    }
	    
	    //�ǰe�ܴ_���I
	    if((click.equals(ClickType.LEFT)) && (slot <= 26)){
	    	
	    	if(Global.didNotChoose(p)){
	    		pfunc.removeNameTag(p);
	    		Global.removeNoChoose(p);
	    	}
	    	
	    	String id = lore.get(0).toString().replace("��bID:", "");
	    	Location loc = (Location) spawns.getConfig().get("spawns." + id + ".location");
	    	
	    	//�����W���Q�R���A�ǰe
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
		
	//��W�٨t��
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
	    
	    if(item.getItemMeta().getLore().get(0).toString().equals(ChatColor.GOLD + "��s�e�W��:")){
	    	inv.setItem(0, null);
	    	p.sendMessage(Global.Header + ChatColor.RED + "�w�������s�R�W�_���I");
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
	    
	    if(!item.getItemMeta().getLore().get(0).toString().equals(ChatColor.GOLD + "��s�e�W��:")){
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
	    	  
	    		p.sendMessage(Global.Header + ChatColor.RED + "�w�������s�R�W�_���I");
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
	    			name = button.getItemMeta().getDisplayName().replace('&', '��').replace("/��", "&");
	    		}
	    	    
	    		List<String> lore = new ArrayList<String>();
	    	  
	    		if(button.getItemMeta().hasLore()) {
	    			lore = button.getItemMeta().getLore();
	    		}
	    	  
	    		if(name.endsWith(" ")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	          
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "�������঳�Ů�!");
	    			return;
	    		}
	    		
	    		//�N�h�ӳs�򪺪Ů��ܦ��@��
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
	    				p.sendMessage(Global.Header + ChatColor.DARK_RED + "�o�ӦW�r�w�g�ιL�F!");
	    				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    				return;
	    			}
	    		}
	        
	    		if(ChatColor.stripColor(name).equals("")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "�A���G�S����J��r�Υu��J�榡�X!");
	    			return;
	    		}
	    		if(ChatColor.stripColor(name).contains("��")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "�W�٤����i�H�t������N�q�榡�X!");
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

	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�w�N�_���I�W�٧אּ " + ChatColor.WHITE + ChatColor.BOLD + "[" + ChatColor.RESET + name + ChatColor.WHITE + ChatColor.BOLD + "]");
	    		p.getOpenInventory().setItem(0, null);
	    		p.closeInventory();
	    		p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
	      	}
	    }
	}
	
	//�ϥιD��
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
	    	
	    	//�����_���D��
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
	    				pi.setItemInMainHand(null);
	    			}else{
	    				pi.setItemInOffHand(null);
	    			}

	    		}
	    		
	    		int quota = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
	    		
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�w�W�[�@���K���B��");
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�Ѿl" + quota + "���K���B��");
	    	}
	    	
	    	//��a�_���D��
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
	              
	    					nms.sendTitle(entp, "");
	    					nms.sendSubTitle(entp, 
	    							ChatColor.DARK_GREEN + p.getName() + "��" + ChatColor.RESET + assistant_respawn_item.getItemMeta().getDisplayName() + ChatColor.DARK_GREEN + "���A�b�o�̴_��",
	    							0, 40, 10);
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
	    	if((meta.getDisplayName().equals(instant_respawn_item.getItemMeta().getDisplayName())) && (meta.getLore().equals(instant_respawn_item.getItemMeta().getLore()))){
	    		e.setCancelled(true);
	    		
	    		int quota = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�Ѿl" + quota + "���K���B��");
	    	}
	    }
	}
	
}
