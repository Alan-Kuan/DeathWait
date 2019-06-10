package me.alan.deathwait;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.permissions.PermissionAttachmentInfo;

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
	
	public PlayerFunctions(Core core){
		
		this.core = core;

		config = core.getConfigClass();
		spawns = core.getSpawnsClass();
		nms = core.getNMSClass();
		list = new ListSpawns(core, this);
		
	}
		
	//讓玩家復活
	public void Respawn(final Player p){
				
		boolean enable_custom_location = config.getConfig().getBoolean("config.enable custom location");
		boolean enable_default_respawn_button = config.getConfig().getBoolean("config.display button of default respawn point");
				
	    if(enable_custom_location){

    		//如果 有權限 且 有復活點可挑
	    	if(p.hasPermission("dw.gui") && (enable_default_respawn_button || (!spawns.getConfig().getConfigurationSection("spawns").getKeys(true).isEmpty()))){
	    	  
	    		Global.addNoChoose(p);
	          
	    		Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	           
	    			@Override
	    			public void run(){
	    				list.List(p, 1);
	    			}
	    			
	    		}, 1L);
	    	  
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
	    	    			WarningGen.Warn("你在" + p.getName() + "的權限設定上出現dw.respawn." + idstr + "的情形");
	    	    			continue;
	    	    		}
	    				
	    				break;
	    			}
	    			
	    		}
	    		
	    		if(idstr == ""){
	    			tpNormalSpawnPoint(p);
	    		}else{
	    			Location loc = (Location) spawns.getConfig().get("spawns." + idstr + ".location");
	    			
	    			if(spawns.getConfig().getInt("last ID") < id){
		    			tpNormalSpawnPoint(p);
		    			WarningGen.Warn(p.getName() + "的權限 dw.respawn." + idstr + "的ID不存在");
		    		}else if(loc == null){
		    			tpNormalSpawnPoint(p);
		    			WarningGen.Warn(p.getName() + "的權限 dw.respawn." + idstr + "的ID對應之座標已遺失或不存在");
		    		}else if(core.getServer().getWorld(loc.getWorld().getName()) == null){
		    			tpNormalSpawnPoint(p);
		    			WarningGen.Warn(p.getName() + "的權限  dw.respawn." + idstr + "的ID對應之世界在此伺服中已遺失或不存在");
		    		}else{
						removeNameTag(p);

		    	    	//先讓名條被刪除再傳送
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
	
	//恢復原狀
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
		
	//傳到自然重生點
	public void tpNormalSpawnPoint(Player p){
			
		if(Global.hasEssentials){
			
			Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
			User user = ess.getUser(p);
			
			//Essentials的家
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
						WarningGen.Warn("在讀取" + p.getName() + "擁有的家時出了問題");
						e.printStackTrace();
					}
					
					break;
				}
					
				if(Global.isGhost(p)){
					removeNameTag(p);
				}

				Location loc = new Location(w, x, y, z, yaw, pitch);
					
			    //先讓名條被刪除再傳送
			    Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
			    		
			    	@Override
			    	public void run(){
						p.teleport(loc);
						
					    if(!Global.didNotChoose(p)){
					    	TurnBack(p);
					    }
					    
			    	}
			    		
			    }, 2);
			    	
				return;
			}
		}
					
		//睡床點
		if(p.getBedSpawnLocation() != null){
			
			if(Global.isGhost(p)){
				removeNameTag(p);
			}
						
			//先讓名條被刪除再傳送
	    	Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	    		
	    		@Override
	    		public void run(){
	    			p.teleport(p.getBedSpawnLocation());
					
				    if(!Global.didNotChoose(p)){
				    	TurnBack(p);
				    }
	    		}
	    		
	    	}, 2);
	    	
	    //世界重生點
		}else{			
			double x = p.getWorld().getSpawnLocation().getX();
			double y = p.getWorld().getSpawnLocation().getY();
			double z = p.getWorld().getSpawnLocation().getZ();
			
			if(Global.isGhost(p)){
				removeNameTag(p);
			}
			
			//先讓名條被刪除再傳送
	    	Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	    		
	    		@Override
	    		public void run(){
	    			p.teleport(new Location(p.getWorld(), x, y, z));
					
				    if(!Global.didNotChoose(p)){
				    	TurnBack(p);
				    }
	    		}
	    		
	    	}, 2);
	    	
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
	    
		if(core.version.equals("v1_10_R1")){
			p.setPassenger(nametag);
		}else{
			p.addPassenger(nametag);
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public void removeNameTag(Player p){
		
		if(core.version.equals("v1_10_R1")){
			p.getPassenger().remove();
		}else{

			for(Entity passenger : p.getPassengers()){
				
				if(passenger.getCustomName().equals(p.getName())){
					passenger.remove();
				}
				
			}
			
		}
		
	}
	
}
