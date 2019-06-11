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
	
	public ListSpawns(Core core, PlayerFunctions pfunc){
		
		this.core = core;
		
		config = core.getConfigClass();
		spawns = core.getSpawnsClass();
		this.pfunc = pfunc;
		im = new ItemMaker();
		
	}
	
	public void List(final Player p, int page_num){
		
	    Inventory gui = Bukkit.createInventory(null, 36, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "�Ҧ��_���I");
	    boolean display_button = config.getConfig().getBoolean("config.display button of default respawn point");
	    
	    //�p�⭶��
	    int pages = 1;
	    if(spawns.getConfig().isSet("spawns")){
	    	
	    	int spawnpoints = 0;
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
	    	}else{
	    		
	    		//�p�G���S���ۭq�_���I�A�j��Ұʦ۵M�����I���s
	    		if(spawnpoints == 0){
	    			display_button = true;
	    		}
	    	}
	    	
	    }else{
	    	
	    	//�p�G���S���ۭq�_���I�θ�ƿ򥢡A�j��Ұʦ۵M�����I���s
	    	display_button = true;
	    }
	    
	    if(page_num > 1){
	    	ItemStack back = im.createItem(Material.SKULL_ITEM, 3, ChatColor.BLUE + "�W�@��", null, false);
	    	SkullMeta backmeta = (SkullMeta)back.getItemMeta();
	    	backmeta.setOwner("MHF_ArrowLeft");
	    	back.setItemMeta(backmeta);
	    	gui.setItem(30, back);
	    }
	    
	    List<String> total = new ArrayList<String>();
	    total.add(ChatColor.DARK_GREEN + " �@" + pages + "��");
	    
	    gui.setItem(31, im.createItem(Material.PAPER, 0, ChatColor.BLUE + "-��" + page_num + "��-", total, false));
	    
	    if(page_num < pages){
	    	ItemStack next = im.createItem(Material.SKULL_ITEM, 3, ChatColor.BLUE + "�U�@��", null, false);
	    	SkullMeta nextmeta = (SkullMeta)next.getItemMeta();
	    	nextmeta.setOwner("MHF_ArrowRight");
	    	next.setItemMeta(nextmeta);
	    	gui.setItem(32, next);
	    }
	    
	    if(display_button){
	    	List<String> lore = new ArrayList<String>();
	    	lore.add("&b�u������:");
	    	
	    	if(Global.hasEssentials){
		    	lore.add("&bEssentials���a(�̦��]�w��) ��");
	    	}
	    	
	    	lore.add("&b�Χ��I ��");
	    	lore.add("&b�@�ɭ����I");
	    	
	    	ItemStack normal = im.createItem(Material.EMERALD, 0, ChatColor.DARK_GREEN + "�۵M�����I", lore, true);
	      
	    	gui.setItem(27, normal);
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
	    		
	    		Location loc = (Location) spawns.getConfig().get("spawns." + id + ".location");
	    		String name = spawns.getConfig().getString("spawns." + id + ".name");
	    		List<String> lore = new ArrayList<String>();
	    			
	    		lore.add(0, ChatColor.AQUA + "ID:" + id);
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
	    	    
	    int time_limit = config.getConfig().getInt("config.time limit of browsing the list");
	    
	    if((time_limit > 0) && !Global.haveChoosingCountingDownId(p) && Global.isGhost(p)){
	    	
	    	final List<String> lore = new ArrayList<String>();
	    	lore.add(ChatColor.RED + "�Y������ܷ|�Ǩ�۵M�����I");
	    	
	    	int countdown = Bukkit.getScheduler().scheduleSyncRepeatingTask(core, new Runnable(){
	    		
	    		int temp = time_limit;
	    		
	    		@Override
	    		public void run(){
	    			
	    			Location loc = p.getLocation();
	    			
	    			//�p�G�٨S��_���I�A��ܭ˼�
	    			if(Global.didNotChoose(p)){
	    				
	    				ItemStack watch = im.createItem(Material.WATCH, 0, ChatColor.DARK_RED + "�A�ٳ�" + temp + "�����", lore, false);
						
	    				//�p�G�Ѿl��� <= 64 �N�P�ɥH�ƶq���
	    				if(temp <= 64){
	    					watch.setAmount(temp);
	    				}else{
	    					watch.setAmount(64);
	    				}
	    				
	    				p.getOpenInventory().setItem(34, watch);
	    					    				
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
    					    
    					}else if(temp <= 5 || temp >= 1){
    						
    						p.playSound(loc, Sound.BLOCK_NOTE_HARP, 1, 1);
    						
    					}else if(temp == 0){
    						Bukkit.getScheduler().cancelTask(Global.getChoosingCountingDownId(p));
	    					Global.removeChoosingCountingDownId(p);
	    					Global.removeNoChoose(p);
	    					pfunc.tpNormalSpawnPoint(p);
	    					pfunc.TurnBack(p);
    					}
	    				
	    				temp--;
	    				
	    			}else{
	    				//�p�G��F�_���I�A�פ�˼�
	    				Bukkit.getScheduler().cancelTask(Global.getChoosingCountingDownId(p));
	    				Global.removeChoosingCountingDownId(p);
	    			}
	    			
	    		}
	    		
	    	}, 0L, 20L);
	    	Global.giveChoosingCountingDownId(p, countdown);
	    }
	}
}