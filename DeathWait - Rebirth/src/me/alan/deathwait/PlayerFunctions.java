package me.alan.deathwait;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import me.alan.deathwait.files.Config;
import me.alan.deathwait.files.Spawns;
import me.alan.deathwait.nms.NMS;

public class PlayerFunctions {

	private Core core;
	
	private Config config;
	private Spawns spawns;
	
	private ItemMaker im;
	
	private NMS nms;
	
	public PlayerFunctions(Core core){
		
		this.core = core;

		config = core.getConfigClass();
		spawns = core.getSpawnsClass();

		im = new ItemMaker();
		
		nms = core.getNMSClass();
		
	}
		
	//�����a�_��
	public void Respawn(final Player p){
				
		boolean enable_custom_location = config.getConfig().getBoolean("config.enable custom location");
		boolean enable_default_respawn_button = config.getConfig().getBoolean("config.display button of default respawn point");

		Location loc = getNormalSpawnPoint(p);
		
	    if(enable_custom_location){
	    	
	    	boolean custom_spawn_points_exist = false;
	    	
	    	//�p�G���ۭq�_���I
	    	if(spawns.getConfig().isSet("spawns")) {
	    		custom_spawn_points_exist = !spawns.getConfig().getConfigurationSection("spawns").getKeys(true).isEmpty();
	    	}
	    	
    		//�p�G ���v�� �B ���_���I�i�D
	    	if(p.hasPermission("dw.gui") && (enable_default_respawn_button || custom_spawn_points_exist)){

	    	    int time_limit = config.getConfig().getInt("config.time limit of browsing the list");
	    		
	    		Global.addNoChoose(p);

	    		new BukkitRunnable() {

	    			@Override
	    			public void run(){
	    				
	    				openSpawnList(p, 1);
	    				
	    			}
	    			
	    		}.runTaskLater(core, 1);

				if(time_limit > 0) {
					choosingCountDown(p, time_limit);
				}
				
	    		return;
	    		
	    	}else{
	    	
	    		String id_str = "no permission";
	    		int id = 0;
	    		
	    		for(PermissionAttachmentInfo perm : p.getEffectivePermissions()){
	    			
	    			if(perm.getPermission().startsWith("dw.respawn.")){
	    				id_str = perm.getPermission().toString().replace("dw.respawn.", "");
	    				
	    				try{
	    	    			id = Integer.parseInt(id_str);
	    	    		}catch(NumberFormatException ex){
	    	    			ex.printStackTrace();
	    	    			WarningGen.Warn("�A�b" + p.getName() + "���v���]�w�W�X�{dw.respawn." + id_str + "������");
	    	    			continue;
	    	    		}
	    				
	    				break;
	    			}

	    			id_str = "no permission";
	    				    			
	    		}
	    		
	    		//���Y�Ӵ_���I���v��
	    		if(!id_str.equals("no permission")){
	    			
	    			loc = (Location) spawns.getConfig().get("spawns." + id_str + ".location");
	    			
	    			if(spawns.getConfig().getInt("last ID") < id){
		    			loc = getNormalSpawnPoint(p);
		    			WarningGen.Warn(p.getName() + "���v�� dw.respawn." + id_str + "��ID���s�b");
		    		}else if(loc == null){
		    			loc = getNormalSpawnPoint(p);
		    			WarningGen.Warn(p.getName() + "���v�� dw.respawn." + id_str + "��ID�������y�Фw�򥢩Τ��s�b");
		    		}else if(core.getServer().getWorld(loc.getWorld().getName()) == null){
		    			loc = getNormalSpawnPoint(p);
		    			WarningGen.Warn(p.getName() + "���v��  dw.respawn." + id_str + "��ID�������@�ɦb�����A���w�򥢩Τ��s�b");
		    		}
	    			
	    		}

	    	}
	      
	    }
	    
	    removeNameTag(p);
	    
	    final Location temp_loc = loc;
	    
	    new BukkitRunnable() {
	    	
	    	@Override
	    	public void run() {
	    	    p.teleport(temp_loc);
	    		
	    	    TurnBack(p);
	    	}
	    	
	    }.runTaskLater(core, 2);
	    
	}
	
	//��_�쪬
	public void TurnBack(final Player p){

		new BukkitRunnable() {
			
			@Override
			public void run() {
				p.setFireTicks(0);
			}
			
		}.runTaskLater(core, 1);
		
		if(Global.isInTargetEntity(p)){

			//nms.setSpectate(p, p);
			p.setSpectatorTarget(p);
			
			for(Entity target: Global.getTargetEntities()){

				if(Global.getPlayerInTargetEntity(target).equals(p)){
					Global.removeTargetEntity(target, p);
				}
			}
		}

		p.setGameMode(Global.getGameMode(p));
		Global.removeGameMode(p);
		Global.removeGhost(p);
		p.setFlySpeed(0.1f);
		p.setFlying(false);

	}
		
	//���o�۵M�����I
	public Location getNormalSpawnPoint(Player p){
				
		if(Global.hasEssentials){
			
			Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
			User user = ess.getUser(p);
			
			//Essentials���a
			if(!user.getHomes().isEmpty()){
				
				World w = null;
				double x = 0;
				double y = 0;
				double z = 0;
				float yaw = 0;
				float pitch = 0;
				
				for(String home : user.getHomes()){
					
					try{
						w = user.getHome(home).getWorld();
						x = user.getHome(home).getX();
						y = user.getHome(home).getY();
						z = user.getHome(home).getZ();
						yaw = user.getHome(home).getYaw();
						pitch = user.getHome(home).getPitch();

						Location loc = new Location(w, x, y, z, yaw, pitch);
						
						return loc;
						
					}catch(Exception e){
						
						WarningGen.Warn("�bŪ��" + p.getName() + "�֦����a�ɥX�F���D");
						e.printStackTrace();

						break;
					}
					
				}
				
			}
			
		}
		
		
		//�Χ��I
		if(p.getBedSpawnLocation() != null){
			
			Location loc = p.getBedSpawnLocation();

			return loc;
			
		//�@�ɭ����I
		}else{

			Location loc = p.getWorld().getSpawnLocation();

			return loc;
			
		}
		
	}
	
	//���ݴ_���ɡA�j�ۨӨD��
	public void yell(Player p) {
		
		//�����ª��S���
		if(!config.getConfig().isSet("config.yelling cooldown"))
			config.set("config.yelling cooldown", 10);
		
		if(!config.getConfig().isSet("config.yelling range"))
			config.set("config.yelling range", 200.0);

		if(!config.getConfig().isSet("config.yelling message")) {
			List<String> msg_list = new ArrayList<String>();
			
			msg_list.add("��6{p} ��r�b ��a{loc} ��r�V�A��c�D��!");
			
			config.set("config.yelling message", msg_list);
		}
		
		
		long cooldown = config.getConfig().getLong("config.yelling cooldown");
		
		Date d = new Date();
		
		long now = d.getTime();
		
		long last_time = 0;
		
		boolean in_cooldown = true;
		
		try {
			last_time = Global.getLastYellingTimeStamp(p);
		}catch(NullPointerException ex){
			in_cooldown = false;
			Global.resetLastYellingTimeStamp(p);
		}
		
		if(now - last_time >= cooldown*1000) {
			in_cooldown = false;
			Global.resetLastYellingTimeStamp(p);
		}
		
		if(in_cooldown) {
			
			p.sendMessage(Global.Header + ChatColor.RED + "�D�ϧN�o���A�Щ�" + (cooldown - (now - last_time)/1000) + "���A�D��");
			
			return;
			
		}
		
		double range = config.getConfig().getDouble("config.yelling range");

		List<String> msg_list = config.getConfig().getStringList("config.yelling message");
		
		int idx = (int) (Math.random()*msg_list.size());
		
		String msg = msg_list.get(idx);
		
		int x = (int) p.getLocation().getX();
		int y = (int) p.getLocation().getY();
		int z = (int) p.getLocation().getZ();
		
		String loc = "(" + x + ", " + y + ", " + z + ")";
		
		msg = msg.replace("{p}", p.getName()).replace("{loc}", loc);
		
		boolean found = false;

		
		p.playSound(p.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1, 1);
				
		for(Entity ent : p.getNearbyEntities(range, range, range)) {
			
			if(ent instanceof Player) {
				
				Player hearer = (Player) ent;
				
				if(Global.isGhost(hearer)) continue;

				found = true;
				
				hearer.sendMessage(msg);
				
				nms.sendTitle(hearer, "��4HELP!", 0, 20, 5);
				
				hearer.playSound(hearer.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1, 1);
				
			}
			
		}

		if(!found)
			p.sendMessage(Global.Header + ChatColor.YELLOW + "���ާA�p��ĤO�a�j�q�A��Ѫ��O�P��" + range + "�椺�S���Hť�������I��");
		
	}
	
	@SuppressWarnings("deprecation")
	public double getMaxHealth(Player p){
		
		if(Global.version.equals("v1_10_R1")){
			return p.getMaxHealth();
		}else{
			return p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public void kickPassenger(Player p){
		
		if(Global.version.equals("v1_10_R1")){
			p.getPassenger().teleport(p.getPassenger().getLocation());
		}else{
						
			for(Entity passenger : p.getPassengers()){
				
				passenger.teleport(passenger.getLocation());
				
			}
			
		}
		
	}

	@SuppressWarnings("deprecation")
	public void setNameTag(Player p){

		if(!Global.isInTargetEntity(p)) {
			
			ArmorStand nametag = (ArmorStand) p.getWorld().spawnEntity(p.getLocation().add(0, 1, 0), EntityType.ARMOR_STAND);
		    
		    nametag.setCustomName(p.getName());
		    nametag.setCustomNameVisible(true);
		    nametag.setVisible(false);
		    nametag.setSmall(true);
		    nametag.setMarker(true);
		    nametag.addScoreboardTag("dw_nametag");
		    
			if(Global.version.equals("v1_10_R1")){
				p.setPassenger(nametag);
			}else{
				p.addPassenger(nametag);
			}
			
	    }
	    		
	}
	
	public void removeNameTag(Player p){

		if(!Global.isInTargetEntity(p)) {
			
			for(Entity ent : p.getNearbyEntities(0.5, 1, 0.5)) {
				
				if(ent.getCustomName() == null || ent.getScoreboardTags().isEmpty())
					continue;
				
				if(ent.getCustomName().equals(p.getName()) && ent.getScoreboardTags().contains("dw_nametag")) {
					ent.remove();
					break;
				}
				
			}
			
		}
		
	}
	
	//�}�Ҵ_���I�ؿ�
	@SuppressWarnings("deprecation")
	public void openSpawnList(final Player p, int page_num){
		
	    Inventory gui = Bukkit.createInventory(null, 36, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "�Ҧ��_���I");
	    boolean enable_default_respawn_button = config.getConfig().getBoolean("config.display button of default respawn point");
	    
	    //�p�⭶��
	    int pages = 1;
    	int spawnpoints = 0;
	    if(spawns.getConfig().isSet("spawns")){
	    	
	    	for(String id : spawns.getConfig().getConfigurationSection("spawns").getKeys(false)){

	    		//�p�G���a��dw.gui.own�����u�⦳�v�����_���I�ƶq
	    		if(Global.isGhost(p) && p.hasPermission("dw.gui.own")){
	    			
	    			if(p.hasPermission("dw.respawn." + id)){
	    				spawnpoints++;
	    			}
	    			
	    		}else{

		    		spawnpoints++;
		    		
	    		}
	    		
	    	}
	    	
	    	if(spawnpoints > 27){
	    		int remainder = spawnpoints % 27;
	    		pages = spawnpoints / 27;
	    		
	    		if(remainder > 0){
	    			pages++;
	    		}
	    	}
	    	
	    }

    	//�p�G�S������_���I�ΨS���֦�����_���I�v��
    	if(spawnpoints == 0 && !enable_default_respawn_button && Global.isGhost(p)) {
    		
    	    removeNameTag(p);
    	    
    	    Global.cancelTimeLimitTask(p);
    	    
    	    final Location loc = getNormalSpawnPoint(p);
    	    
    	    new BukkitRunnable() {
    	    	
    	    	@Override
    	    	public void run() {
    	    	    p.teleport(loc);
    	    		
    	    	    TurnBack(p);
    	    	}
    	    	
    	    }.runTaskLater(core, 2);
    	    
    	    return;
    	    
    	}
    	
	    if(page_num > 1){
	    	ItemStack previous = im.createItem(Material.SKULL_ITEM, 3, ChatColor.BLUE + "�W�@��", null, false);
	    	SkullMeta previous_meta = (SkullMeta) previous.getItemMeta();
	    	
	    	//���ӭn��令��UUID
	    	previous_meta.setOwner("MHF_ArrowLeft");
	    	
	    	previous.setItemMeta(previous_meta);
	    	gui.setItem(30, previous);
	    }
	    
	    List<String> total = new ArrayList<String>();
	    total.add(ChatColor.DARK_GREEN + " �@" + pages + "��");
	    
	    gui.setItem(31, im.createItem(Material.PAPER, 0, ChatColor.BLUE + "-��" + page_num + "��-", total, false));
	    
	    if(page_num < pages){
	    	ItemStack next = im.createItem(Material.SKULL_ITEM, 3, ChatColor.BLUE + "�U�@��", null, false);
	    	SkullMeta next_meta = (SkullMeta)next.getItemMeta();
	    	
	    	//���ӭn��令��UUID
	    	next_meta.setOwner("MHF_ArrowRight");
	    	
	    	next.setItemMeta(next_meta);
	    	gui.setItem(32, next);
	    }
	    
	    if(enable_default_respawn_button){
	    	
	    	List<String> lore = new ArrayList<String>();
	    	lore.add("&b�u������:");
	    	
	    	if(Global.hasEssentials){
		    	lore.add("&bEssentials���a(�̦��]�w��) ��");
	    	}
	    	
	    	lore.add("&b�Χ��I ��");
	    	lore.add("&b�@�ɭ����I");
	    	
	    	ItemStack respawn_button = im.createItem(Material.EMERALD, 0, ChatColor.DARK_GREEN + "�۵M�����I", lore, true);
	      
	    	gui.setItem(27, respawn_button);
	    }
	    
	    if(Global.isGhost(p) && p.hasPermission("dw.yell")) {
	    	
	    	List<String> lore = new ArrayList<String>();
	    	
	    	double range = config.getConfig().getDouble("config.yelling range");
	    	
	    	lore.add(ChatColor.YELLOW + "�V�P��" + range + "�檺���a�D��");
	    	
	    	ItemStack yelling_button = im.createItem(Material.JACK_O_LANTERN, 0, ChatColor.BOLD + "" + ChatColor.DARK_RED + "�D��", lore, false);
	    	
	    	gui.setItem(29, yelling_button);
	    }
	    
	    if(spawns.getConfig().isSet("spawns")){
	    	
	    	int start = (page_num - 1) * 27;
	    	int stop = page_num * 27;
	    	int i = 0;
	    	
	    	for(String id : spawns.getConfig().getConfigurationSection("spawns").getKeys(false)){
	    		
	    		if(i == stop){
	    			break;
	    		}
	    		
	    		//�p�G���a��dw.gui.own�������L��ܨS���v�����_���I
	    		if(Global.isGhost(p) && p.hasPermission("dw.gui.own")){
	    			if(!p.hasPermission("dw.respawn." + id)){
	    				continue;
	    			}
	    		}
	    		
	    		if(i < start) {
	    			i++;
	    			continue;
	    		}
	    		
	    		Location loc = (Location) spawns.getConfig().get("spawns." + id + ".location");
	    		String name = spawns.getConfig().getString("spawns." + id + ".name");
	    		List<String> lore = new ArrayList<String>();
	    		
	    		lore.add(ChatColor.AQUA + "ID:" + id);
	    		lore.add(ChatColor.GOLD + "�ҳB�@��:" + loc.getWorld().getName());
	    		lore.add(ChatColor.BLUE + "X�y��:" + loc.getX());
	    		lore.add(ChatColor.BLUE + "Y�y��:" + loc.getY());
	    		lore.add(ChatColor.BLUE + "Z�y��:" + loc.getZ());
	    		if(!Global.isGhost(p)){
	    			lore.add(ChatColor.GREEN + "�m����n�ǰe�ܴ_���I");
	    			lore.add(ChatColor.GREEN + "�m�k��n���s�R�W�_���I");
	    			lore.add(ChatColor.GREEN + "�m��������D��n�N���_���I���ϥܧ令�A�񪺹D��");
	    			lore.add(ChatColor.RED + "�mShift+�k��n�N�_���I�y�г]���{�b��m");
	    			lore.add(ChatColor.RED + "�mShift+����n�N���_���I�y�в���");
	    		}
	    			
	    		//�����ª��S�����
	    		if(spawns.getConfig().get("spawns." + id + ".icon") == null){
	        	  	spawns.set("spawns." + id + ".icon.type", Material.GRASS.toString());
	        	  	spawns.set("spawns." + id + ".icon.data", 0);
	        	  	spawns.set("spawns." + id + ".icon.glowing", false);
	    		}

	    		Material icon_type = Material.getMaterial(spawns.getConfig().getString("spawns." + id + ".icon.type"));
	    		int icon_data = spawns.getConfig().getInt("spawns." + id + ".icon.data");
	    		boolean icon_glowing = spawns.getConfig().getBoolean("spawns." + id + ".icon.glowing");
	    			
	    		ItemStack icon = im.createItem(icon_type, icon_data, name, lore, false);
	    			
	    		if(icon_glowing){
	    			ItemMeta meta = icon.getItemMeta();
	    			meta.addEnchant(Enchantment.DURABILITY, 1, false);
	    			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
	    			icon.setItemMeta(meta);
	    		}
	    			
	    		gui.setItem(i - start, icon);

		    	i++;
	    	}
	    }

	    p.openInventory(gui);
	    
	}
	
	//�D��_���I���ɶ�����
	public void choosingCountDown(Player p, int time_limit) {
		
	    List<String> lore = new ArrayList<String>();
	    lore.add(ChatColor.RED + "�Y������ܷ|�Ǩ�۵M�����I");
	    
		Location loc = p.getLocation();
		
		BukkitTask time_limit_task = new BukkitRunnable() {
			
			int temp = time_limit;
				
			@Override
	    	public void run(){
	    		
	    		//�p�G�٨S��_���I�A��ܭ˼�
	    		if(Global.didNotChoose(p)){
	    			
	    			ItemStack watch = im.createItem(Material.WATCH, 0, ChatColor.DARK_RED + "�A�ٳѤU" + temp + "����", lore, false);
					
	    			//�p�G�Ѿl��� <= 64 �N�P�ɥH�ƶq���
	    			if(temp <= 64){
	    				watch.setAmount(temp);
	    			}else{
	    				watch.setAmount(64);
	    			}
	    			
	    			InventoryView inv = p.getOpenInventory();
	    			
	    			if(inv.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "�Ҧ��_���I")) {
	    				inv.setItem(34, watch);
	    			}
	    				    				
	    			if(temp == 20){
	    				
    					p.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    					
    				}else if(temp == 10){
    					
    					p.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    				    
    					new BukkitRunnable() {
    						
    						@Override
    						public void run() {
    							p.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    						}
    						
    					}.runTaskLater(core, 5);
    					
    				}else if(temp >= 1 && temp <= 5){
    					
    					p.playSound(loc, Sound.BLOCK_NOTE_HARP, 1, 1);
    					
    				}else if(temp == 0){
    					
    					cancel();
    					
	    				Global.removeNoChoose(p);
	    				
	    				removeNameTag(p);
	    				
	    				new BukkitRunnable() {
	    					
	    					@Override
	    					public void run() {
	    	    				p.teleport(getNormalSpawnPoint(p));
	    	    				
	    	    				TurnBack(p);
	    					}
	    					
	    				}.runTaskLater(core, 2);
	    				
    				}
	    			
	    			temp--;
	    			
	    		}else{
	    			//�p�G��F�_���I�A�פ�˼�
	    			cancel();
	    		}
	    		
	    	}
				
		}.runTaskTimer(core, 0, 20);
		
		Global.addTimeLimitTask(p, time_limit_task);
		
	}
	
}
