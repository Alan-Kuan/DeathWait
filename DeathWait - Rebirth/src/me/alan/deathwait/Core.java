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

	備註: 如果擔心Essentials的重生系統影響到，可在Essentials將respawn-listener-priority:設為lowest
	      
	備忘錄: ess自殺相容性(目前知1.11.2沒問題)
		         有dw.rescue可求救
		          玩家斷線可做處理
		         選目錄玩家之重登處理
		         退出時名條未消失
		         鐵砧會顯示更新後名稱
    	         伺服重讀可替等待的玩家做處理

*************************/

public class Core extends JavaPlugin {

	private WarningGen warn;
	private Globalvar GV;
	private Config config;
	private Data data;
	private Spawns spawns;
	private PlayerFunctions pfunc;
	
	private AnvilGUI anvil;
	private NMS nms;
		
	public String version;
	
	public void onEnable(){
		
		if(!IsSetup()){
			Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[DeathWait]" + "伺服器版本或開服程式與DeathWait不相容!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		
		try{
			if(!getDataFolder().exists()){
				getDataFolder().mkdir();
			}
		}catch (SecurityException e){
			e.printStackTrace();
	        warn.Warn("在啟動插件時為了產生DeathWait資料夾而出了問題");
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
		
		warn = new WarningGen(this);
		GV = new Globalvar();
		config = new Config(this);
		data = new Data(this);
		spawns = new Spawns(this);
		pfunc = new PlayerFunctions(this);
		
		getCommand("dw").setExecutor(new PlayerCommands(this));
		getCommand("dw").setTabCompleter(new TabCompleteCommand());
		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

		//確認是否有裝Essentials
		if(Bukkit.getPluginManager().isPluginEnabled("Essentials")){
			GV.setEssentials(true);
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[DeathWait] 偵測到此伺服安裝Essentials! 已啟動額外功能!");
		}
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[DeathWait] V" + getDescription().getVersion() + "已啟動! " + ChatColor.DARK_AQUA + "by小恩AlanKuan");
	    
		warn.Warn("Just for testing");
		
		//顯示靈魂
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			
			@Override
			public void run(){

				boolean show = config.getConfig().getBoolean("config.show spirit");
								
				for(Player p : getServer().getOnlinePlayers()){
					if(show && GV.isGhost(p) && !GV.isInTargetEntity(p)){
						p.getWorld().spawnParticle(Particle.FLAME, p.getLocation().add(0.0D, 1.0D, 0.0D), 50, 0.1, 0.1, 0.1, 0.01);
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
	    	warn.Warn("在取得伺服器版本時出了問題");
	    	return false;
	    }
	    
	    if(!version.equals("v1_10_R1") && !version.equals("v1_11_R1") && !version.equals("v1_12_R1")){
	    	return false;
	    }
	    
	    return true;
	}
		
	public void onDisable(){
		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[DeathWait] V" + getDescription().getVersion() + "已關閉");
	}
	
	public Globalvar getGlobalvarClass(){
		return GV;
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
