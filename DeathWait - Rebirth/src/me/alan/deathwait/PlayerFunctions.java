package me.alan.deathwait;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.WitherSkull;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
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
	private NMS nms;
	private ListSpawns list;
	private ItemMaker im;
	
	public PlayerFunctions(Core core){
		
		this.core = core;

		config = core.getConfigClass();
		spawns = core.getSpawnsClass();
		nms = core.getNMSClass();
		list = new ListSpawns(core);
		im = new ItemMaker();
		
	}
		
	//�����a�_��
	public void Respawn(final Player p){
				
		boolean enable_custom_location = config.getConfig().getBoolean("config.enable custom location");
		boolean enable_default_respawn_button = config.getConfig().getBoolean("config.display button of default respawn point");

	    if(enable_custom_location){

    		//�p�G ���v�� �B ���_���I�i�D
	    	if(p.hasPermission("dw.gui") && (enable_default_respawn_button || (!spawns.getConfig().getConfigurationSection("spawns").getKeys(true).isEmpty()))){

	    	    int time_limit = config.getConfig().getInt("config.time limit of browsing the list");
	    		
	    		Global.addNoChoose(p);
	          
	    		new BukkitRunnable() {

	    			@Override
	    			public void run(){
	    				
	    				list.List(p, 1);
	    				
	    				if(time_limit > 0) {
	    					choosingCountDown(p, time_limit);
	    				}
	    				
	    			}
	    			
	    		}.runTaskLater(core, 1);
	    		
	    	}else{
	    	  
	    		String idstr = "";
	    		int id = 0;
	        
	    		for(PermissionAttachmentInfo perm : p.getEffectivePermissions()){
	    			
	    			idstr = "";
	    			
	    			if(perm.getPermission().startsWith("dw.respawn.")){
	    				idstr = perm.getPermission().toString().replace("dw.respawn.", "");
	    				
	    				try{
	    	    			id = Integer.parseInt(idstr);
	    	    		}catch(NumberFormatException ex){
	    	    			ex.printStackTrace();
	    	    			WarningGen.Warn("�A�b" + p.getName() + "���v���]�w�W�X�{dw.respawn." + idstr + "������");
	    	    			continue;
	    	    		}
	    				
	    				break;
	    			}
	    			
	    		}
	    		
	    		//�S������_���I���v��
	    		if(idstr == ""){
	    			tpNormalSpawnPoint(p);
	    		}else{
	    			Location loc = (Location) spawns.getConfig().get("spawns." + idstr + ".location");
	    			
	    			if(spawns.getConfig().getInt("last ID") < id){
		    			tpNormalSpawnPoint(p);
		    			WarningGen.Warn(p.getName() + "���v�� dw.respawn." + idstr + "��ID���s�b");
		    		}else if(loc == null){
		    			tpNormalSpawnPoint(p);
		    			WarningGen.Warn(p.getName() + "���v�� dw.respawn." + idstr + "��ID�������y�Фw�򥢩Τ��s�b");
		    		}else if(core.getServer().getWorld(loc.getWorld().getName()) == null){
		    			tpNormalSpawnPoint(p);
		    			WarningGen.Warn(p.getName() + "���v��  dw.respawn." + idstr + "��ID�������@�ɦb�����A���w�򥢩Τ��s�b");
		    		}else{
						removeNameTag(p);

		    	    	//�����W���Q�R���A�ǰe
		    	    	Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
		    	    		
		    	    		@Override
		    	    		public void run(){
		    	    	    	p.teleport(loc);
		    	    	    	
		    	    		    if(!Global.didNotChoose(p)){
		    	    		    	TurnBack(p);
		    	    		    }
		    	    		}
		    	    		
		    	    	}, 2);
		    		}
	    		}

	    	}
	      
	    }else{
	    	tpNormalSpawnPoint(p);
	    }
	    
	}
	
	//��_�쪬
	public void TurnBack(final Player p){
				
		Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
    		
    		@Override
    		public void run(){
    	    	p.setFireTicks(0);
    		}
    		
    	}, 1);
		
		if(Global.isInTargetEntity(p)){

			nms.setSpectate(p, p);

			for(Entity target: Global.getTargetEntities()){

				if(Global.getPlayerInTargetEntity(target).equals(p)){
					Global.removeTargetEntity(target, p);
				}
			}
		}

		p.setGameMode(Global.getGameMode(p));
		Global.removeGameMode(p);
		Global.removeGhost(p);
		Global.removeKilled(p);
		p.setFlySpeed(0.1f);
		p.setFlying(false);

	}
		
	//�Ǩ�۵M�����I
	public void tpNormalSpawnPoint(Player p){
				
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
					}catch(Exception e){
						WarningGen.Warn("�bŪ��" + p.getName() + "�֦����a�ɥX�F���D");
						e.printStackTrace();
					}
					
					break;
				}
				
				if(Global.isGhost(p)){
					removeNameTag(p);
				}
				
				Location loc = new Location(w, x, y, z, yaw, pitch);
				
				new BukkitRunnable() {
					
					@Override
					public void run() {

						p.teleport(loc);
						
					    if(!Global.didNotChoose(p)){
					    	TurnBack(p);
					    }
					    
					}
					
				}.runTaskLater(core, 2);
				
				return;
				
			}
			
		}
		
		
		//�Χ��I
		if(p.getBedSpawnLocation() != null){
			
			if(Global.isGhost(p)){
				removeNameTag(p);
			}
			
			Location loc = p.getBedSpawnLocation();

			new BukkitRunnable() {
				
				@Override
				public void run() {

					p.teleport(loc);
					
				    if(!Global.didNotChoose(p)){
				    	TurnBack(p);
				    }
				    
				}
				
			}.runTaskLater(core, 2);
			
		//�@�ɭ����I
		}else {

			if(Global.isGhost(p)){
				removeNameTag(p);
			}
			
			Location loc = p.getWorld().getSpawnLocation();

			new BukkitRunnable() {
				
				@Override
				public void run() {

					p.teleport(loc);
					
				    if(!Global.didNotChoose(p)){
				    	TurnBack(p);
				    }
				    
				}
				
			}.runTaskLater(core, 2);
			
		}
		
	}
	
	public Entity getShooter(Entity killer){
		Entity shooter = killer;
		if(((killer instanceof Arrow)) || ((killer instanceof TippedArrow)) || ((killer instanceof SpectralArrow)) || ((killer instanceof SplashPotion)) || 
				((killer instanceof ShulkerBullet)) || ((killer instanceof WitherSkull)) || ((killer instanceof Fireball)) || ((killer instanceof Snowball)) || ((killer instanceof Egg))){
			
			try{
				Projectile projectile = (Projectile)killer;
				shooter = (Entity)projectile.getShooter();
			}catch(Exception e){
				return null;
			}
			
		}
		
		return shooter;
	}
	

	@SuppressWarnings("deprecation")
	public double getMaxHealth(Player p){
		
		if(core.version.equals("v1_10_R1")){
			return p.getMaxHealth();
		}else{
			return p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public void kickPassenger(Player p){
		
		if(core.version.equals("v1_10_R1")){
			p.getPassenger().teleport(p.getPassenger().getLocation());
		}else{
						
			for(Entity passenger : p.getPassengers()){
				
				passenger.teleport(passenger.getLocation());
				
			}
			
		}
		
	}

	@SuppressWarnings("deprecation")
	public void setNameTag(Player p){

	    ArmorStand nametag = (ArmorStand) p.getWorld().spawnEntity(p.getLocation().add(0, 1, 0), EntityType.ARMOR_STAND);
	    
	    nametag.setCustomName(p.getName());
	    nametag.setCustomNameVisible(true);
	    nametag.setVisible(false);
	    nametag.setSmall(true);
	    nametag.setMarker(true);
	    nametag.addScoreboardTag("dw_nametag");
	    
		if(core.version.equals("v1_10_R1")){
			p.setPassenger(nametag);
		}else{
			p.addPassenger(nametag);
		}
		
	}
	
	public void removeNameTag(Player p){

		if(!Global.isInTargetEntity(p)) {
			
			for(Entity ent : p.getNearbyEntities(1, 1, 1)) {
				
				if(ent.getCustomName().equals(p.getName()) && ent.getScoreboardTags().contains("dw_nametag")) {
					ent.remove();
					break;
				}
				
			}
			
		}
		
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
    				    
    				    Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
    					   	
    					   	@Override
    				    	public void run(){
    				    		p.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    				    	}
    				    	
    				    }, 5);
    				    
    				}else if(temp >= 1 && temp <= 5){
    					
    					p.playSound(loc, Sound.BLOCK_NOTE_HARP, 1, 1);
    					
    				}else if(temp == 0){
    					cancel();
	    				Global.removeNoChoose(p);
	    				tpNormalSpawnPoint(p);
	    				TurnBack(p);
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
