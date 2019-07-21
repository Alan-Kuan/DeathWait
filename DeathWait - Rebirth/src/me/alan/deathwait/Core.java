package me.alan.deathwait;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import me.alan.deathwait.anvilgui.AnvilGUI;
import me.alan.deathwait.anvilgui.AnvilGUI_v1_10_R1;
import me.alan.deathwait.anvilgui.AnvilGUI_v1_11_R1;
import me.alan.deathwait.anvilgui.AnvilGUI_v1_12_R1;
import me.alan.deathwait.files.Config;
import me.alan.deathwait.files.Data;
import me.alan.deathwait.files.Spawns;
import me.alan.deathwait.nms.NMS;
import me.alan.deathwait.nms.v1_10_R1;
import me.alan.deathwait.nms.v1_11_R1;
import me.alan.deathwait.nms.v1_12_R1;

/*************************

	�Ƶ�: �p�G���Essentials�����ͨt�μv�T��A�i�bEssentials�Nrespawn-listener-priority:�]��lowest��none

*************************/

public class Core extends JavaPlugin {

	private Config config;
	private Data data;
	private Spawns spawns;
	
	private AnvilGUI anvil;
	private NMS nms;
	
	private PlayerFunctions pfunc;
	
	public void onEnable(){
		
		if(!IsSetup()){
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[DeathWait]" + "���A�������ζ}�A�{���PDeathWait���ۮe!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		try{
			if(!getDataFolder().exists()){
				getDataFolder().mkdir();
			}
		}catch (SecurityException e){
			e.printStackTrace();
	        WarningGen.Warn("�b�Ұʴ���ɬ��F����DeathWait��Ƨ��ӥX�F���D");
		}
		
		if(Global.version.equals("v1_10_R1")){
			anvil = new AnvilGUI_v1_10_R1();
			nms = new v1_10_R1();
		}else if(Global.version.equals("v1_11_R1")){
			anvil = new AnvilGUI_v1_11_R1();
			nms = new v1_11_R1();
		}else if(Global.version.equals("v1_12_R1")){
			anvil = new AnvilGUI_v1_12_R1();
			nms = new v1_12_R1();
		}
		
		config = new Config(this);
		data = new Data(this);
		spawns = new Spawns(this);
		
		pfunc = new PlayerFunctions(this);
		
		getCommand("dw").setExecutor(new PlayerCommands(this));
		getCommand("dw").setTabCompleter(new TabCompleteCommand());
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
		getServer().getPluginManager().registerEvents(new MenuListener(this), this);

		//�T�{�O�_����Essentials
		if(Bukkit.getPluginManager().isPluginEnabled("Essentials")){
			Global.hasEssentials = true;
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[DeathWait] �����즹���A�w��Essentials! �w�Ұ��B�~�\��!");
		}
		
		//���A����Ū���B�z
		for(Player p : getServer().getOnlinePlayers()) {
			
			boolean is_ghost = data.getConfig().isSet("players." + p.getUniqueId() + ".is ghost");

			boolean is_browsing = data.getConfig().getBoolean("players." + p.getUniqueId() + ".is browsing spawn list");
			
			if(is_ghost) {
				
				Global.addGhost(p);
				
				data.set("players." + p.getUniqueId() + ".is ghost", null);
				
				GameMode gamemode = GameMode.valueOf(data.getConfig().getString("players." + p.getUniqueId() + ".gamemode"));
				
				Global.setGameMode(p, gamemode);
				
				data.set("players." + p.getUniqueId() + ".gamemode", null);
				
				String uuid_of_target_entity = "";
				
				if(data.getConfig().isSet("players." + p.getUniqueId() + ".target entity")) {
				
					uuid_of_target_entity = data.getConfig().getString("players." + p.getUniqueId() + ".target entity");
				
					//�p�G���a�ݪ�target entity�٦b����A�N�⪱�a��^�h
					for(Entity ent : p.getNearbyEntities(10, 10, 10)) {
						
						if(ent.getUniqueId().toString().equals(uuid_of_target_entity)) {
							nms.setSpectate(p, ent);
							Global.addTargetEntity(ent, p);
							data.set("players." + p.getUniqueId() + ".target entity", null);
							break;
						}
						
					}
					
				}

				pfunc.setNameTag(p);
				
				//�p�G��Ū�ɥ��b�s���_���I�ؿ�
				if(is_browsing) {
					
		    	    int time_limit = config.getConfig().getInt("config.time limit of browsing the list");
		    		
		    		Global.addNoChoose(p);
		    		
		    		new BukkitRunnable() {

		    			@Override
		    			public void run(){
		    				
		    				pfunc.openSpawnList(p, 1);
		    				
		    			}
		    			
		    		}.runTaskLater(this, 1);
		    		
					if(time_limit > 0) {
						pfunc.choosingCountDown(p, time_limit);
					}
					
					data.set("players." + p.getUniqueId() + ".is browsing spawn list", null);
					
				}else {

					int left = data.getConfig().getInt("players." + p.getUniqueId() + ".left waiting time");

			    	nms.sendTitle(p, ChatColor.RED + "�A�w�g���F", 0, left*20, 0);
			    	
					BukkitTask countdown_task = new BukkitRunnable(){

						int temp = left;
						
				    	@Override
				    	public void run(){

				    		Global.setLeftWaitingTime(p, temp);
				    		
				    		if(temp > 0){
				    			
				    			String show = temp + "���_��";
				    				
				    			nms.sendSubTitle(p, ChatColor.GOLD + show, 0, 25, 0);
				    				
				    			temp--;
				    			
				    		}else{
				    			cancel();
				    			data.set("players." + p.getUniqueId() + ".left waiting time", null);
				    			pfunc.Respawn(p);
				    		}
				    		
				    	}
				    	
					}.runTaskTimer(this, 0, 20);
					
					Global.addCountdownTask(p, countdown_task);
					
				}
				
			}
			
		}
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[DeathWait] v" + getDescription().getVersion() + "�w�Ұ�! " + ChatColor.DARK_AQUA + "by�p��AlanKuan");
	    
		//����F��
		new BukkitRunnable(){
			
			boolean display_soul = config.getConfig().getBoolean("config.display soul of players");
			
			@Override
			public void run(){
				
				for(Player p : getServer().getOnlinePlayers()){
					if(display_soul && Global.isGhost(p) && !Global.isInTargetEntity(p)){
						p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0.0, 1.0, 0.0), 50, 0.1, 0.1, 0.1, 0.01);
					}
				}
				
			}
			
		}.runTaskTimer(this, 0, 20);
	   
	}
	  
	private boolean IsSetup(){
		String ver = Bukkit.getVersion();
		
	    if(!ver.contains("Spigot")){
	    	return false;
	    }
	    
	    try{
	    	Global.version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	    }catch (ArrayIndexOutOfBoundsException e){
	    	WarningGen.Warn("�b���o���A�������ɥX�F���D");
	    	return false;
	    }
	    
	    if(!Global.version.equals("v1_10_R1") && !Global.version.equals("v1_11_R1") && !Global.version.equals("v1_12_R1")){
	    	return false;
	    }
	    
	    return true;
	}
		
	public void onDisable(){
		
		//���A����Ū���B�z
		for(Player p : getServer().getOnlinePlayers()) {
			
			if(Global.isGhost(p)) {
				
				p.sendMessage(Global.Header + ChatColor.DARK_RED + "�ѩ���A����Ū���A�]���|���u�ȩ���!");
				
		    	pfunc.removeNameTag(p);
		    	
		    	data.set("players." + p.getUniqueId() + ".is ghost", true);
		    	
		    	data.set("players." + p.getUniqueId() + ".gamemode", Global.getGameMode(p).toString());

		    	if(Global.hasLeftWaitingTime(p)){
		    		
			    	data.set("players." + p.getUniqueId() + ".left waiting time", Global.getLeftWaitingTime(p));
			    	
		    	}
		    	
		    	if(Global.isInTargetEntity(p)) {
		    		
		    		nms.setSpectate(p, p);
		    		
		    		data.set("players." + p.getUniqueId() + ".target entity", Global.getTargetEntity(p).getUniqueId().toString());
		    		
		    	}
		    	
				//�p�G���b��ܴ_���I
				if(Global.didNotChoose(p)) {
					data.set("players." + p.getUniqueId() + ".is browsing spawn list", true);
					p.closeInventory();
				}else {
					data.set("players." + p.getUniqueId() + ".is browsing spawn list", false);
				}
				
			}
			
		}
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[DeathWait] v" + getDescription().getVersion() + "�w����");
	}
	
	public Config getConfigClass(){
		return config;
	}
	
	public Spawns getSpawnsClass(){
		return spawns;
	}
	
	public Data getDataClass(){
		return data;
	}

	public PlayerFunctions getPlayerFunctionsClass() {
		return pfunc;
	}
	
	public AnvilGUI getAnvilGUIClass(){
		return anvil;
	}
	
	public NMS getNMSClass(){
		return nms;
	}
	
}
