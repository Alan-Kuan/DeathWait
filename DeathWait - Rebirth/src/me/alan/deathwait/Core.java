package me.alan.deathwait;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

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
	      
	備忘錄:  ess自殺相容性(目前知1.11.2沒問題)
			有dw.yell可求救
			從target entity出來後，名條在倒數結束時有時不會消失
			在target entity裡，登出後再登入，名條在倒數結束時不會消失
			1.12.2的AnvilGUI不能用
			玩家斷線可做處理
			選目錄玩家之重登處理
			伺服重讀可替等待的玩家做處理
			測試兩個玩家死在同一個怪物下
			避免滑行、Shift放置道具於GUI
			/kill 處理

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
	        WarningGen.Warn("在啟動插件時為了產生DeathWait資料夾而出了問題");
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

		//確認是否有裝Essentials
		if(Bukkit.getPluginManager().isPluginEnabled("Essentials")){
			Global.hasEssentials = true;
			Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "[DeathWait] 偵測到此伺服安裝Essentials! 已啟動額外功能!");
		}
		
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "[DeathWait] v" + getDescription().getVersion() + "已啟動! " + ChatColor.DARK_AQUA + "by小恩AlanKuan");
	    
		//顯示靈魂
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
	    	WarningGen.Warn("在取得伺服器版本時出了問題");
	    	return false;
	    }
	    
	    if(!Global.version.equals("v1_10_R1") && !Global.version.equals("v1_11_R1") && !Global.version.equals("v1_12_R1")){
	    	return false;
	    }
	    
	    return true;
	}
		
	public void onDisable(){
		Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "[DeathWait] v" + getDescription().getVersion() + "已關閉");
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
