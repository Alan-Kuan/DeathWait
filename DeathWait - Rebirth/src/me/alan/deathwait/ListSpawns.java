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
		
	    Inventory gui = Bukkit.createInventory(null, 36, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "�Ҧ��_���I");
	    boolean button = config.getConfig().getBoolean("config.default respawn button");
	    
	    //�p�⭶��
	    int pages;
	    if(spawns.getConfig().isSet("spawns")){
	    	
	    	int spawnpoints = 0;
	    	for(String id : spawns.getConfig().getConfigurationSection("spawns").getKeys(false)){

	    		//�p�G���a��dw.gui.own�����u�⦳�v�����_���I�ƶq
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
	    	ItemStack back = im.createItem(Material.SKULL_ITEM, 3, ChatColor.BLUE + "�W�@��", null, false);
	    	SkullMeta backmeta = (SkullMeta)back.getItemMeta();
	    	backmeta.setOwner("MHF_ArrowLeft");
	    	back.setItemMeta(backmeta);
	    	gui.setItem(30, back);
	    }
	    
	    List<String> total = new ArrayList<String>();
	    total.add(ChatColor.DARK_GREEN + " �@" + pages + "��");
	    
	    gui.setItem(31, im.createItem(Material.PAPER, 0, ChatColor.BLUE + "-��" + nowat + "��-", total, false));
	    
	    if(nowat < pages){
	    	ItemStack next = im.createItem(Material.SKULL_ITEM, 3, ChatColor.BLUE + "�U�@��", null, false);
	    	SkullMeta nextmeta = (SkullMeta)next.getItemMeta();
	    	nextmeta.setOwner("MHF_ArrowRight");
	    	next.setItemMeta(nextmeta);
	    	gui.setItem(32, next);
	    }
	    
	    if(button){
	    	List<String> lore = new ArrayList<String>();
	    	lore.add("&b�u������:");
	    	
	    	if(GV.hasEssentials){
		    	lore.add("&bEssentials���a(�̦��]�w��) ��");
	    	}
	    	
	    	lore.add("&b�Χ��I �� �@�ɭ����I");
	    	ItemStack normal = im.createItem(Material.EMERALD, 0, ChatColor.DARK_GREEN + "�۵M�����I", lore, true);
	      
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
	    		
	    		//�p�G���a��dw.gui.own�������L��ܨS���v�����_���I
	    		if(GV.isGhost(p) && p.hasPermission("dw.gui.own")){
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
	    		if(!GV.isGhost(p)){
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
	    	    
	    int timelimit = config.getConfig().getInt("config.choosing time limit");
	    
	    if((timelimit > 0) && !GV.haveChoosingCountingDownId(p) && GV.isGhost(p)){
	    		    	
	    	final List<String> lore = new ArrayList<String>();
	    	lore.add(ChatColor.RED + "�Y������ܷ|�Ǩ�۵M�����I");
	    	
	    	int time = Bukkit.getScheduler().scheduleSyncRepeatingTask(core, new Runnable(){
	    		
	    		int wait = timelimit;
	    		
	    		@Override
	    		public void run(){
	    			
	    			Location loc = p.getLocation();
	    			
	    			//�p�G�٨S��_���I�A��ܭ˼�
	    			if(GV.isNoChoose(p)){
	    				
	    				ItemStack watch = im.createItem(Material.WATCH, 0, ChatColor.DARK_RED + "�A�ٳ�" + wait + "�����", lore, false);
						
	    				//�p�G�Ѿl��� <= 64 �N�P�ɥH�ƶq���
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
	    			//�p�G��F�_���I�A�פ�˼�
	    				Bukkit.getScheduler().cancelTask(GV.getChoosingCountingDownId(p));
	    				GV.removeChoosingCountingDownId(p);
	    			}
	    			
	    		}
	    		
	    	}, 0L, 20L);
	    	GV.giveChoosingCountingDownId(p, time);
	    }
	}
}