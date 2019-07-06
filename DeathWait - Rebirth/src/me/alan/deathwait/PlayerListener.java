package me.alan.deathwait;

import java.util.ArrayList;
import java.util.List;

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
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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
	
	private PlayerFunctions pfunc;
	
	private ItemMaker im;
	
	private AnvilGUI anvil;
	private NMS nms;
	
	public PlayerListener(Core core){
		
		this.core = core;
		
		config = core.getConfigClass();
		spawns = core.getSpawnsClass();
		data = core.getDataClass();
		
		pfunc = core.getPlayerFunctionsClass();
		
		im = new ItemMaker();

		anvil = core.getAnvilGUIClass();
		nms = core.getNMSClass();
		
	}
	
	//�p�G�ӥͪ��Otarget entity�A����̭����F��
	public void targetEntityCheck(Entity ent) {

	    if(Global.isTargetEntity(ent)){
	    	Player victim = (Player) Global.getPlayerInTargetEntity(ent);
	    	
	    	nms.setSpectate(victim, victim);
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
		
		//�p�G���a�̫�@���Ϫ��a���`
		if(p.getHealth() - e.getFinalDamage() <= 0.0) {
			e.setCancelled(true);
		}else {
			return;
		}
		
		p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);
		
		//�T�{���a�O�_��target entity
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
	    	    
	    //��U�M�b�Ӫ��a���W���ͪ�
	    pfunc.kickPassenger(p);
	    
	    //�����M��
	    p.teleport(p.getLocation());
	    
	    //�Y���i���
	    if(e.getCause().equals(DamageCause.VOID) && (p.getLocation().getY() < 0.0)){
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
	    
	    //�����g���
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

	    //�̷�Gamerule�ӨM�w�n���n�����D��
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
	    
	    //���\����
	    if(allow_moving){
	    	p.setFlySpeed(0.1f);
	    }else{
	    	p.setFlySpeed(0);
	    }
	    
	    //�^�_������
	    if(reset_food_level){
	    	p.setFoodLevel(20);
	    }
	    
	    if(p.hasPermission("dw.bypass")){
	    	
	    	have_to_wait = false;
	    	
	    //�p�G�S���v���~�|�������_���B��
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
	    	
	    	//�p�G����s�b �B ���a���O���� �B ���⤣�O�z������θ��F
	    	if(killer != null && !p.equals(killer) && !Global.isExplosiveEntity(killer)) {
	    		nms.setSpectate(p, killer);
	    		Global.addTargetEntity(killer, p);
	    	}
	    		    	
	    }
	    
	    //��ܦW��
	    pfunc.setNameTag(p);
	    
	    //�ݭn����
	    if(have_to_wait){
	    	
	    	int wait = config.getConfig().getInt("config.waiting seconds");
	    	
	    	nms.sendTitle(p, ChatColor.RED + "�A�w�g���F", 0, wait*20, 0);
	    	
	    	BukkitTask countdwon_task = new BukkitRunnable(){

		    	int temp = wait;
		    	
		    	@Override
		    	public void run(){
		    		
		    		Global.setLeftWaitingTimes(p, temp);
		    		
		    		if(temp > 0){
		    				
		    			String show = temp + "���_��";
		            
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
	
	//��̭����F��ͪ����`�A�F��Q����
	@EventHandler
	public void onTargetEntityDeath(EntityDeathEvent e){
		
	    Entity ent = e.getEntity();
	    
	    targetEntityCheck(ent);
	    
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		
		Player p = e.getPlayer();
				
		//�p�G�h�X�����a�̭�����L���a���F��A�F��Q����
		targetEntityCheck(p);
	    
	    //�p�G�h�X�����a�O���F
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
			
			new BukkitRunnable(){

		    	@Override
		    	public void run(){
		    		
					//��ܦW��		    
				    pfunc.setNameTag(p);

	    		if(allow_moving){
						p.setFlySpeed(0.1f);
					}else{
						p.setFlySpeed(0);
					}
		    		
		    	}
		    	
			}.runTaskLater(core, 2);
    		
			int left = data.getConfig().getInt("players." + p.getUniqueId() + ".left waiting times");
			
			nms.sendTitle(p, ChatColor.RED + "�W���n�X�ɤ��b���ݴ_��", 0, left*20, 0);
			
			BukkitTask countdown_task = new BukkitRunnable(){

				int temp = left;
				
		    	@Override
		    	public void run(){

		    		Global.setLeftWaitingTimes(p, temp);
		    		
		    		if(temp > 0){
		    			
		    			String show = temp + "���_��";
		    				
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
	
	//���a�ϥ��ۤU�ӨD��
	@EventHandler
	public void onShiftYelling(PlayerToggleSneakEvent e) {
		
		Player p = e.getPlayer();
		
		if(p.isSneaking() && Global.isGhost(p) && p.hasPermission("dw.yell")) {
			pfunc.yell(p);
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
	    			final int page_num = Integer.parseInt(s);
	    			
	    			new BukkitRunnable() {

	    				@Override
	    				public void run(){
	    					pfunc.openSpawnList(p, page_num);
	    				}
	    				
	    			}.runTaskLater(core, 1);
	    			
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
	public void onClickButton(InventoryClickEvent e){
				
	    Inventory gui = e.getClickedInventory();

	    if(gui == null){
	    	return;
	    }
	    
	    if(!(e.getWhoClicked() instanceof Player)){
	    	return;
	    }
	    
	    if(!gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "�Ҧ��_���I")) {
	    	return;
	    }
	    
	    e.setCancelled(true);
	    
	    Player p = (Player) e.getWhoClicked();
	    ItemStack item = e.getCurrentItem();
	    ClickType click = e.getClick();
	    InventoryAction action = e.getAction();
	    int slot = e.getSlot();
	    
	    if(item.getType() == Material.AIR){
	    	return;
	    }
	    
	    String name = item.getItemMeta().getDisplayName();
	    
	    List<String> lore = new ArrayList<String>();
	    
	    String id = "";
	    
	    if(item.getItemMeta().hasLore()){
	    	lore = item.getItemMeta().getLore();

		    if(slot <= 26) {
			    id = lore.get(0).toString().replace("��bID:", "");	
		    }
		    
	    }
	    
	    //�����D��M����������ϥܥ洫
	    if(slot > 26) {

		    if(action == InventoryAction.SWAP_WITH_CURSOR) return;
		    
	    }
	    
	    //�^��۵M�����I
	    if((click.equals(ClickType.LEFT)) && (slot == 27) && (name.equals(ChatColor.DARK_GREEN + "�۵M�����I"))){
		    
	    	if(Global.didNotChoose(p)){
		    	pfunc.removeNameTag(p);
	    		Global.removeNoChoose(p);
	    	}

	    	//�����W���Q�R���A�ǰe
	    	new BukkitRunnable() {

	    		@Override
	    		public void run(){
	    			
	    	    	p.teleport(pfunc.getNormalSpawnPoint(p));

	    	    	if(Global.isGhost(p)){
	    	    		pfunc.TurnBack(p);
	    	    	}
	    	    	
	    	    	p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
	    		}
	    		
	    	}.runTaskLater(core, 2);
	    	
	    }
	    
	    //�D��
	    if(click.equals(ClickType.LEFT) && slot == 29 && name.equals(ChatColor.BOLD + "" + ChatColor.DARK_RED + "�D��")) {
	    	pfunc.yell(p);
	    }
	    
	    //�W�@��
	    if((click.equals(ClickType.LEFT)) && (slot == 30) && (name.equals(ChatColor.BLUE + "�W�@��"))){
	    	
	    	p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	    		
	    	try{
	    			
	    		String s = gui.getItem(31).getItemMeta().getDisplayName().replace("��9-��", "").replace("��-", "");
	    		int page_num = Integer.parseInt(s);
	    		
	    		if (page_num - 1 > 0){
	    			Global.addTurnPage(p);
	    			pfunc.openSpawnList(p, page_num - 1);
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
	    		int page_num = Integer.parseInt(s);
	        
	    		String totalstr = gui.getItem(31).getItemMeta().getLore().get(0).toString().replace("��2 �@", "").replace("��", "");
	    		int total = Integer.parseInt(totalstr);
	    			
	    		if(page_num + 1 <= total){
	    			Global.addTurnPage(p);
	    			pfunc.openSpawnList(p, page_num + 1);
	    		}
	    			
	    	}catch(NumberFormatException ex){
	    		ex.printStackTrace();
	    		WarningGen.Warn("�b����ثe�Ҧb���Ʃ��`���ƮɥX�F���D");
	    	}
	    }
	    	
	    //���O���F�ɡA�i�H�s��ۭq�_���I
	    if(!Global.isGhost(p) && (slot <= 26)){

	    	//���ϥ�
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
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�w��s�_���I�ϥ�");
	    		
	    		return;
	    		
	    	}
	    		
	    	//���]�_���I
	    	if(click.equals(ClickType.SHIFT_RIGHT)){
	    		
	    		Location loc = p.getLocation();
	    		
	    		if(spawns.getConfig().isSet("spawns")){
	    			  
					spawns.set("spawns." + id + ".location", loc);
					
					nms.sendLocation(p, name, loc);
					
				}

	    		ItemMeta meta = item.getItemMeta();
	    		
	    		lore.set(2, ChatColor.BLUE + "X�y��:" + loc.getX());
	    		lore.set(3, ChatColor.BLUE + "Y�y��:" + loc.getY());
	    		lore.set(4, ChatColor.BLUE + "Z�y��:" + loc.getZ());
	    		
	    		meta.setLore(lore);
	    		
	    		item.setItemMeta(meta);
	    		
	    		e.setCurrentItem(item);
	    		
	    		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
	    	}
	    		
	    	//�����_���I
	    	if(click.equals(ClickType.SHIFT_LEFT)){
	    	    
	    		spawns.set("spawns." + id, null);
	    		
	    		p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
	    			
	    		try{
	    			String s = gui.getItem(31).getItemMeta().getDisplayName().replace("��9-��", "").replace("��-", "");
	    			int page_num = Integer.parseInt(s);
	    				
	    			if ((page_num > 1) && (slot == 0) && (gui.getItem(1) == null)) {
	    				page_num--;
	    			}
	    				
	    			pfunc.openSpawnList(p, page_num);
	    		}catch(NumberFormatException ex){
	    			ex.printStackTrace();
	    			WarningGen.Warn("�b����ثe�Ҧb���ƮɥX�F���D");
	    		}
	    		p.sendMessage(Global.Header + ChatColor.DARK_RED + "�w�N�_���I ��f��l[��r" + name + "��f��l] " + ChatColor.RESET + ChatColor.DARK_RED + "����!");
	    	}
	    	
	    	//���s�R�W�_���I
	    	if(click.equals(ClickType.RIGHT)){
	    	    
	    		List<String> info = new ArrayList<String>();
	    		
	    		info.add(ChatColor.GOLD + "��s�e�W��:");
	    		info.add(name);
	    		info.add("");
	    		info.add(ChatColor.DARK_RED + "�Ƶ�: �Y�n�b�W�٤����&�A�Шϥ�/&");
	    		info.add("");
	    		info.add(ChatColor.BLUE + "�I�����誺��l�Ӧ^�_�쥻�W��");
	    		info.add(ChatColor.AQUA + "�I����������l�ӹw�����G");
	    		info.add(ChatColor.GREEN + "�I���k�誺��l�ӽT�{���");
	    		info.add(ChatColor.DARK_GRAY + "ID:" + id);
	        
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
	    	
	    	Location loc = (Location) spawns.getConfig().get("spawns." + id + ".location");
	    	
	    	//�����W���Q�R���A�ǰe
	    	new BukkitRunnable() {

	    		@Override
	    		public void run(){
	    			
	    	    	p.teleport(loc);
	    	    	
	    	    	if(Global.isGhost(p)){
	    	    		pfunc.TurnBack(p);
	    	    	}
	    	    	
	    	    	p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
	    		}
	    		
	    	}.runTaskLater(core, 2);
	    	
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
	    	inv.clear();
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
	    
	    ItemStack item0 = e.getClickedInventory().getItem(0);
	    if(!item0.getItemMeta().hasLore()){
	    	return;
	    }
	    
	    List<String> lore0 = item0.getItemMeta().getLore();
	    
	    if(!lore0.get(0).toString().equals(ChatColor.GOLD + "��s�e�W��:")){
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
	    			
	    			//�A���I�������w��
	    			if(e.getInventory().getItem(1) != null){
		    			e.getClickedInventory().setItem(1, null);
		    			
		    			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
		    		}
	    			
	    			return;
	    			
	    		}
	    		
	    		ItemMeta meta2 = e.getClickedInventory().getItem(2).getItemMeta();
	    		
	    		String name0 = item0.getItemMeta().getDisplayName().replace("&", "��").replace("/��", "&");
	    		
	    		String name2 = meta2.getDisplayName().replace("&", "��").replace("/��", "&");
	    		
	    		List<String> lore1 = new ArrayList<String>();
	    		
	    		lore1.add(ChatColor.GOLD + "��s�e�W��:");
	    		lore1.add(name0);
	    		lore1.add("");
	    		lore1.add(ChatColor.GOLD + "��s��W��:");
	    		lore1.add(name2);
	    		lore1.add("");
	    		lore1.add(ChatColor.GREEN + "�A���I�����������w��");
	    		
	    		e.getClickedInventory().setItem(1, im.createItem(Material.ENCHANTED_BOOK, 0, ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "�W�ٹw��", lore1, false));
	    		
	    		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
	    		
	    	}else if(rawSlot == 2){
	    		
	    		ItemStack item2 = e.getInventory().getItem(2);
	    		
	    		if(item2 == null){
	    			return;
	    		}
	    		
	    		String newname = item2.getItemMeta().getDisplayName().replace("&", "��").replace("/��", "&");
	    		
	    		String id = lore0.get(8).replace(ChatColor.DARK_GRAY + "ID:", "");
	    		
	    		if(ChatColor.stripColor(newname).startsWith(" ") || ChatColor.stripColor(newname).endsWith(" ")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	          
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "�}�Y�M�������঳�Ů�!");
	    			return;
	    		}
	    		
	    		//�N�h�ӳs�򪺪Ů��ܦ��@��
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
	    				p.sendMessage(Global.Header + ChatColor.DARK_RED + "�o�ӦW�r�w�g�ιL�F!");
	    				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    				return;
	    			}
	    		}
	        
	    		if(ChatColor.stripColor(newname).equals("")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "�A���G�S����J��r�Υu��J�榡�X!");
	    			return;
	    		}
	    		if(ChatColor.stripColor(newname).contains("��")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "�n�b�W�٤����&�A�Шϥ�/&");
	    			return;
	    		}
	    		
	    		spawns.set("spawns." + id + ".name", newname);
	    		
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�w�N�_���I�W�٧אּ " + ChatColor.WHITE + ChatColor.BOLD + "[" + ChatColor.RESET + newname + ChatColor.WHITE + ChatColor.BOLD + "]");
	    		e.getClickedInventory().clear();
	    		p.closeInventory();
	    		p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
	      	}
	    }
	}
	
	//�ϥιD��
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
	    				inv.setItemInMainHand(null);
	    			}else{
	    				inv.setItemInOffHand(null);
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
	    							ChatColor.DARK_GREEN + p.getName() + "�ϥ�" + ChatColor.RESET + assistant_respawn_item.getItemMeta().getDisplayName() + ChatColor.DARK_GREEN + "���A��a�_��",
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
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�Ѿl" + quota + "���K���B��");
	    	}
	    }
	}
	
}
