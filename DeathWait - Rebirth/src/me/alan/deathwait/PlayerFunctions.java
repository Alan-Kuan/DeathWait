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
	private ErrorGen err;
	private Globalvar GV;
	
	public PlayerFunctions(Core core){
		
		this.core = core;

		config = core.getConfigClass();
		spawns = core.getSpawnsClass();
		nms = core.getNMSClass();
		list = new ListSpawns(core, this);
		err = new ErrorGen(core);
		GV = core.getGlobalvarClass();
		
	}
		
	//�����a�_��
	public void Respawn(final Player p){
				
		boolean CL = config.getConfig().getBoolean("config.custom location");
		boolean button = config.getConfig().getBoolean("config.default respawn button");
				
	    if(CL){

    		//�p�G ���v�� �B ���_���I�i�D
	    	if(p.hasPermission("dw.gui") && (button || (!spawns.getConfig().getConfigurationSection("spawns").getKeys(true).isEmpty()))){
	    	  
	    		GV.addNoChoose(p);
	          
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
	    	    			err.error("�A�b" + p.getName() + "���v���]�w�W�X�{dw.respawn." + idstr + "������");
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
		    			err.error(p.getName() + "���v�� dw.respawn." + idstr + "��ID���s�b");
		    		}else if(loc == null){
		    			tpNormalSpawnPoint(p);
		    			err.error(p.getName() + "���v�� dw.respawn." + idstr + "��ID�������y�Фw�򥢩Τ��s�b");
		    		}else if(core.getServer().getWorld(loc.getWorld().getName()) == null){
		    			tpNormalSpawnPoint(p);
		    			err.error(p.getName() + "���v��  dw.respawn." + idstr + "��ID�������@�ɦb�����A���w�򥢩Τ��s�b");
		    		}else{
						removeNameTag(p);

		    	    	//�����W���Q�R���A�ǰe
		    	    	Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
		    	    		
		    	    		@Override
		    	    		public void run(){
		    	    	    	p.teleport(loc);
		    	    	    	
		    	    		    if(!GV.didNotChoose(p)){
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
			
		if(GV.isInTargetEntity(p)){

			nms.setSpectate(p, p);

			for(Entity target: GV.getTargetEntities()){

				if(GV.getPlayerInTargetEntity(target).equals(p)){
					GV.removeTargetEntity(target, p);
				}
			}
		}

		p.setGameMode(GV.getGameMode(p));
		GV.removeGameMode(p);
		GV.removeGhost(p);
		GV.removeKilled(p);
		p.setFlySpeed(0.1f);
		p.setFlying(false);

	}
		
	//�Ǩ�۵M�����I
	public void tpNormalSpawnPoint(Player p){
			
		if(GV.hasEssentials){
			
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
						err.error("�bŪ��" + p.getName() + "�֦����a�ɥX�F���D");
						e.printStackTrace();
					}
					
					break;
				}
					
				if(GV.isGhost(p)){
					removeNameTag(p);
				}

				Location loc = new Location(w, x, y, z, yaw, pitch);
					
			    //�����W���Q�R���A�ǰe
			    Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
			    		
			    	@Override
			    	public void run(){
						p.teleport(loc);
						
					    if(!GV.didNotChoose(p)){
					    	TurnBack(p);
					    }
					    
			    	}
			    		
			    }, 2);
			    	
				return;
			}
		}
					
		//�Χ��I
		if(p.getBedSpawnLocation() != null){
			
			if(GV.isGhost(p)){
				removeNameTag(p);
			}
						
			//�����W���Q�R���A�ǰe
	    	Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	    		
	    		@Override
	    		public void run(){
	    			p.teleport(p.getBedSpawnLocation());
					
				    if(!GV.didNotChoose(p)){
				    	TurnBack(p);
				    }
	    		}
	    		
	    	}, 2);
	    	
	    //�@�ɭ����I
		}else{			
			double x = p.getWorld().getSpawnLocation().getX();
			double y = p.getWorld().getSpawnLocation().getY();
			double z = p.getWorld().getSpawnLocation().getZ();
			
			if(GV.isGhost(p)){
				removeNameTag(p);
			}
			
			//�����W���Q�R���A�ǰe
	    	Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
	    		
	    		@Override
	    		public void run(){
	    			p.teleport(new Location(p.getWorld(), x, y, z));
					
				    if(!GV.didNotChoose(p)){
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
