package me.alan.deathwait;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import me.alan.deathwait.files.Config;
import me.alan.deathwait.files.Spawns;

public class ListSpawns {
	
	private Core core;
	private Config config;
	private Spawns spawns;
	private PlayerFunctions pfunc;
	private ItemMaker im;
	private Globalvar GV;
	
	public ListSpawns(Core core, PlayerFunctions pfunc){
		
		this.core = core;
		
		config = core.getConfigClass();
		spawns = core.getSpawnsClass();
		this.pfunc = pfunc;
		im = new ItemMaker();
		GV = core.getGlobalvarClass();
		
	}
	
	public void List(final Player p, int nowat){
		
	    Inventory gui = Bukkit.createInventory(null, 36, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "所有復活點");
	    boolean button = config.getConfig().getBoolean("config.default respawn button");
	    
	    //計算頁數
	    int pages;
	    if(spawns.getConfig().isSet("spawns")){
	    	
	    	int spawnpoints = 0;
	    	for(String id : spawns.getConfig().getConfigurationSection("spawns").getKeys(false)){

	    		//如果玩家有dw.gui.own必須只算有權限的復活點數量
	    		if(GV.isGhost(p) && p.hasPermission("dw.gui.own")){
	    			
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
	    	}else{
	    		pages = 1;
	    		
	    		if(spawnpoints == 0){
	    			button = true;
	    		}
	    	}
	    	
	    }else{
	    	pages = 1;
	    	
	    	button = true;
	    }
	    
	    if(nowat > 1){
	    	ItemStack back = im.createItem(Material.SKULL_ITEM, 3, ChatColor.BLUE + "上一頁", null, false);
	    	SkullMeta backmeta = (SkullMeta)back.getItemMeta();
	    	backmeta.setOwner("MHF_ArrowLeft");
	    	back.setItemMeta(backmeta);
	    	gui.setItem(30, back);
	    }
	    
	    List<String> total = new ArrayList<String>();
	    total.add(ChatColor.DARK_GREEN + " 共" + pages + "頁");
	    
	    gui.setItem(31, im.createItem(Material.PAPER, 0, ChatColor.BLUE + "-第" + nowat + "頁-", total, false));
	    
	    if(nowat < pages){
	    	ItemStack next = im.createItem(Material.SKULL_ITEM, 3, ChatColor.BLUE + "下一頁", null, false);
	    	SkullMeta nextmeta = (SkullMeta)next.getItemMeta();
	    	nextmeta.setOwner("MHF_ArrowRight");
	    	next.setItemMeta(nextmeta);
	    	gui.setItem(32, next);
	    }
	    
	    if(button){
	    	List<String> lore = new ArrayList<String>();
	    	lore.add("&b優先順序:");
	    	
	    	if(GV.hasEssentials){
		    	lore.add("&bEssentials的家(最早設定的) →");
	    	}
	    	
	    	lore.add("&b睡床點 → 世界重生點");
	    	ItemStack normal = im.createItem(Material.EMERALD, 0, ChatColor.DARK_GREEN + "自然重生點", lore, true);
	      
	    	gui.setItem(27, normal);
	    }
	    if(spawns.getConfig().isSet("spawns")){
	    	int stop = nowat * 27;
	    	int start = (nowat - 1) * 27;
	    	int i = 0;
	    	for(String id : spawns.getConfig().getConfigurationSection("spawns").getKeys(false)){
	    		
	    		if(i == stop){
	    			break;
	    		}
	    		
	    		//如果玩家有dw.gui.own必須跳過顯示沒有權限的復活點
	    		if(GV.isGhost(p) && p.hasPermission("dw.gui.own")){
	    			if(!p.hasPermission("dw.respawn." + id)){
	    				continue;
	    			}
	    		}
	    		
	    		Location loc = (Location) spawns.getConfig().get("spawns." + id + ".location");
	    		String name = spawns.getConfig().getString("spawns." + id + ".name");
	    		List<String> lore = new ArrayList<String>();
	    			
	    		lore.add(0, ChatColor.AQUA + "ID:" + id);
	    		lore.add(ChatColor.GOLD + "所處世界:" + loc.getWorld().getName());
	    		lore.add(ChatColor.BLUE + "X座標:" + loc.getX());
	    		lore.add(ChatColor.BLUE + "Y座標:" + loc.getY());
	    		lore.add(ChatColor.BLUE + "Z座標:" + loc.getZ());
	    		if(!GV.isGhost(p)){
	    			lore.add(ChatColor.GREEN + "《左鍵》傳送至復活點");
	    			lore.add(ChatColor.GREEN + "《右鍵》重新命名復活點");
	    			lore.add(ChatColor.GREEN + "《換掉此格道具》將此復活點的圖示改成你放的道具");
	    			lore.add(ChatColor.RED + "《Shift+右鍵》將復活點座標設為現在位置");
	    			lore.add(ChatColor.RED + "《Shift+左鍵》將此復活點座標移除");
	    		}
	    			
	    		//防止舊版沒有資料
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
	    	    
	    int timelimit = config.getConfig().getInt("config.choosing time limit");
	    
	    if((timelimit > 0) && !GV.haveChoosingCountingDownId(p) && GV.isGhost(p)){
	    		    	
	    	final List<String> lore = new ArrayList<String>();
	    	lore.add(ChatColor.RED + "若不做選擇會傳到自然重生點");
	    	
	    	int time = Bukkit.getScheduler().scheduleSyncRepeatingTask(core, new Runnable(){
	    		
	    		int wait = timelimit;
	    		
	    		@Override
	    		public void run(){
	    			
	    			Location loc = p.getLocation();
	    			
	    			//如果還沒選復活點，顯示倒數
	    			if(GV.isNoChoose(p)){
	    				
	    				ItemStack watch = im.createItem(Material.WATCH, 0, ChatColor.DARK_RED + "你還剩" + wait + "秒做選擇", lore, false);
						
	    				//如果剩餘秒數 <= 64 就同時以數量顯示
	    				if(wait <= 64){
	    					watch.setAmount(wait);
	    				}else{
	    					watch.setAmount(64);
	    				}
	    				
	    				p.getOpenInventory().setItem(34, watch);
	    					    				
	    				if(wait == 20){
	    					
    						p.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    						
    					}else if(wait == 10){
    						
    						p.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    					    
    					    Bukkit.getScheduler().scheduleSyncDelayedTask(core, new Runnable(){
    					    	
    					    	@Override
    					    	public void run(){
    					    		p.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    					    	}
    					    	
    					    }, 5L);
    					    
    					}else if((wait == 5) || (wait == 4) || (wait == 3) || (wait == 2) || (wait == 1)){
    						
    						p.playSound(loc, Sound.BLOCK_NOTE_HARP, 1, 1);
    						
    					}else if(wait == 0){
    						Bukkit.getScheduler().cancelTask(GV.getChoosingCountingDownId(p));
	    					GV.removeChoosingCountingDownId(p);
	    					GV.removeNoChoose(p);
	    					pfunc.tpNormalSpawnPoint(p);
	    					pfunc.TurnBack(p);
    					}
	    				
	    				wait--;
	    				
	    			}else{
	    			//如果選了復活點，終止倒數
	    				Bukkit.getScheduler().cancelTask(GV.getChoosingCountingDownId(p));
	    				GV.removeChoosingCountingDownId(p);
	    			}
	    			
	    		}
	    		
	    	}, 0L, 20L);
	    	GV.giveChoosingCountingDownId(p, time);
	    }
	}
}