package me.alan.deathwait;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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

	�Ƶ�: �p�G���Essentials�����ͨt�μv�T��A�i�bEssentials�Nrespawn-listener-priority:�]��lowest
	      
	�Ƨѿ�:     ess�۱��ۮe��(�ثe��1.11.2�S���D)
			��dw.yell�i�D��
			�qtarget entity�X�ӫ�A�W���b�˼Ƶ����ɦ��ɤ��|����
			�btarget entity�̡A�n�X��A�n�J�A�W���b�˼Ƶ����ɦ��ɤ��|����
			���a�_�u�i���B�z
			��ؿ����a�����n�B�z
			���A��Ū�i�����ݪ����a���B�z

*************************/

public class Core extends JavaPlugin {

	private Config config;
	private Data data;
	private Spawns spawns;
	private PlayerFunctions pfunc;
	
	private AnvilGUI anvil;
	private NMS nms;
		
	public String version;
	
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
		
		if(version.equals("v1_10_R1")){
			anvil = new AnvilGUI_v1_10_R1();
			nms = new v1_10_R1();
		}else if(version.equals("v1_11_R1")){
			anvil = new AnvilGUI_v1_11_R1();
			nms = new v1_11_R1();
		}else if(version.equals("v1_12_R1")){
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

		//�T�{�O�_����Essentials
		if(Bukkit.getPluginManager().isPluginEnabled("Essentials")){
			Global.hasEssentials = true;
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[DeathWait] �����즹���A�w��Essentials! �w�Ұ��B�~�\��!");
		}
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[DeathWait] v" + getDescription().getVersion() + "�w�Ұ�! " + ChatColor.DARK_AQUA + "by�p��AlanKuan");
	    
		//����F��
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			
			@Override
			public void run(){

				boolean show = config.getConfig().getBoolean("config.display soul of players");
								
				for(Player p : getServer().getOnlinePlayers()){
					if(show && Global.isGhost(p) && !Global.isInTargetEntity(p)){
						p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0.0, 1.0, 0.0), 50, 0.1, 0.1, 0.1, 0.01);
					}
				}
			}
			
		}, 0L, 20L);
	   
	}
	  
	private boolean IsSetup(){
		String ver = Bukkit.getVersion();
		
	    if(!ver.contains("Spigot")){
	    	return false;
	    }
	    
	    try{
	    	version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
	    }catch (ArrayIndexOutOfBoundsException e){
	    	WarningGen.Warn("�b���o���A�������ɥX�F���D");
	    	return false;
	    }
	    
	    if(!version.equals("v1_10_R1") && !version.equals("v1_11_R1") && !version.equals("v1_12_R1")){
	    	return false;
	    }
	    
	    return true;
	}
		
	public void onDisable(){
		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[DeathWait] v" + getDescription().getVersion() + "�w����");
	}
	
	public PlayerFunctions getPlayerFunctionsClass(){
		return pfunc;
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
	
	public AnvilGUI getAnvilGUIClass(){
		return anvil;
	}
	
	public NMS getNMSClass(){
		return nms;
	}
	
}
