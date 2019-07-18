package me.alan.deathwait;
//
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import me.alan.deathwait.anvilgui.AnvilGUI;
import me.alan.deathwait.files.Spawns;
import me.alan.deathwait.nms.NMS;

public class MenuListener implements Listener{

	private Core core;
	
	private Spawns spawns;
	
	private PlayerFunctions pfunc;

	private ItemMaker im;
	
	private AnvilGUI anvil;
	private NMS nms;
	
	public MenuListener(Core core) {
		
		this.core = core;
		
		spawns = core.getSpawnsClass();
		
		pfunc = core.getPlayerFunctionsClass();
		
		im = new ItemMaker();
		
		anvil = core.getAnvilGUIClass();
		nms = core.getNMSClass();
		
	}

	//防止還沒選復活點就關閉畫面
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent e){
		
	    final Player p = (Player) e.getPlayer();
	    Inventory gui = e.getInventory();
	    
	    if(!Global.hasTurnedPage(p)){
	    	
	    	if(gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "復活點目錄") && Global.didNotChoose(p)){
	    	    
	    		try{
	    			
	    			String s = gui.getItem(31).getItemMeta().getDisplayName().replace("§9-第", "").replace("頁-", "");
	    			final int page_num = Integer.parseInt(s);
	    			
	    			new BukkitRunnable() {

	    				@Override
	    				public void run(){
	    					pfunc.openSpawnList(p, page_num);
	    				}
	    				
	    			}.runTaskLater(core, 1);
	    			
	    		}catch(NumberFormatException ex){
	    			
	    			ex.printStackTrace();
	    			WarningGen.Warn("在獲取目前所在頁數時出了問題");
	    			
	    		}
	    		
	    	}
	    	
	    }else{
	    	Global.removeTurnPage(p);
	    }
	}
	
	//防止滑動放置道具於復活點目錄
	@EventHandler
	public void onDragItem(InventoryDragEvent e) {
		
		Inventory gui = e.getInventory();
		
		Set<Integer> rawslots = e.getRawSlots();
				
		if(gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "復活點目錄")) {
			
			for(Integer ele : rawslots) {
				
				if(ele <= 35) {
					e.setCancelled(true);
					break;
				}
				
			}
			
	    }
		
	}
	
	//復活點目錄的按鈕
	@EventHandler
	public void onClickButton(InventoryClickEvent e){
				
	    Inventory gui = e.getInventory();
	    
	    if(!gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "復活點目錄")) {
	    	return;
	    }
	    
	    Player p = (Player) e.getWhoClicked();
	    ItemStack item = e.getCurrentItem();
	    ClickType click = e.getClick();
	    InventoryAction action = e.getAction();
	    int slot = e.getRawSlot();
	    
	    //防止點擊空的按鈕或放道具於空格
	    if(item.getType() == Material.AIR){
	    	
	    	if(slot >= 0 && slot <= 35)
	    		e.setCancelled(true);
	    	
	    	return;
	    }
	    
	    String name = item.getItemMeta().getDisplayName();
	    
	    List<String> lore = new ArrayList<String>();
	    
	    String id = "";
	    
	    if(item.getItemMeta().hasLore()){
	    	lore = item.getItemMeta().getLore();

		    if(slot <= 26) {
			    id = lore.get(0).toString().replace("§bID:", "");	
		    }
		    
	    }
	    
	    
	    //復活點目錄的按鈕都不能移動
	    if(slot >= 0 && slot <= 35)
	    	e.setCancelled(true);
	    
	    
	    //防止把道具和不能替換的圖示交換
	    if(slot > 26) {

		    if(action == InventoryAction.SWAP_WITH_CURSOR) {
			    return;
		    }
		    
	    }
	    
	    //防止丟道具到復活點目錄
	    if(slot > 35) {
	    	
	    	if(click.equals(ClickType.SHIFT_LEFT) || click.equals(ClickType.SHIFT_RIGHT)) {
	    		e.setCancelled(true);
	    		return;
	    	}
	    	
	    }
	    
	    //回到自然重生點
	    if((click.equals(ClickType.LEFT)) && (slot == 27) && (name.equals(ChatColor.DARK_GREEN + "自然重生點"))){
		    
	    	if(Global.didNotChoose(p)){
		    	pfunc.removeNameTag(p);
	    		Global.removeNoChoose(p);
	    	}

	    	//先讓名條被刪除再傳送
	    	new BukkitRunnable() {

	    		@Override
	    		public void run(){
	    			
	    	    	p.teleport(pfunc.getNormalSpawnPoint(p));

	    	    	if(Global.isGhost(p)){
	    	    		pfunc.TurnBack(p);
	    	    	}
	    	    	
	    	    	p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
	    		}
	    		
	    	}.runTaskLater(core, 2);
	    	
	    }
	    
	    //求救
	    if(click.equals(ClickType.LEFT) && slot == 29 && name.equals(ChatColor.BOLD + "" + ChatColor.DARK_RED + "求救")) {
	    	pfunc.yell(p);
	    }
	    
	    //上一頁
	    if((click.equals(ClickType.LEFT)) && (slot == 30) && (name.equals(ChatColor.BLUE + "上一頁"))){

	    	p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	    		
	    	try{
	    			
	    		String s = gui.getItem(31).getItemMeta().getDisplayName().replace("§9-第", "").replace("頁-", "");
	    		int page_num = Integer.parseInt(s);
	    		
	    		if (page_num - 1 > 0){
	    			if(Global.isGhost(p)) Global.addTurnPage(p);
	    			pfunc.openSpawnList(p, page_num - 1);
	    		}
	    			
	    	}catch(NumberFormatException ex){
	    		ex.printStackTrace();
	    		WarningGen.Warn("在獲取目前所在頁數時出了問題");
	    	}
	    }
	    	
	    //下一頁
	    if((click.equals(ClickType.LEFT)) && (slot == 32) && (name.equals(ChatColor.BLUE + "下一頁"))){

	    	p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	    		
	    	try{
	    			
	    		String s_page_num = gui.getItem(31).getItemMeta().getDisplayName().replace("§9-第", "").replace("頁-", "");
	    		int page_num = Integer.parseInt(s_page_num);
	        
	    		String s_total = gui.getItem(31).getItemMeta().getLore().get(0).toString().replace("§2 共", "").replace("頁", "");
	    		int total = Integer.parseInt(s_total);
	    		
	    		if(page_num + 1 <= total){
	    			if(Global.isGhost(p)) Global.addTurnPage(p);
	    			pfunc.openSpawnList(p, page_num + 1);
	    		}
	    			
	    	}catch(NumberFormatException ex){
	    		ex.printStackTrace();
	    		WarningGen.Warn("在獲取目前所在頁數或總頁數時出了問題");
	    	}
	    }
	    
	    //不是幽靈時，可以編輯自訂復活點
	    if(!Global.isGhost(p) && (slot <= 26)){
	    	
	    	//更改圖示
	    	if(e.getAction() == InventoryAction.SWAP_WITH_CURSOR){

	    		ItemStack icon = e.getCursor();
	    		ItemMeta meta = icon.getItemMeta();
	    		boolean glow = false;
	    		
	    		p.setItemOnCursor(new ItemStack(Material.AIR));
	    		
	    		spawns.set("spawns." + id + ".icon.type", icon.getType().toString());
	    		spawns.set("spawns." + id + ".icon.data", icon.getDurability());
	    		
	    		if(icon.getItemMeta().hasEnchants()){
	    			glow = true;
	    		}
	    		spawns.set("spawns." + id + ".icon.glowing", glow);
	    		
	    		meta.setDisplayName(item.getItemMeta().getDisplayName());
	    		meta.setLore(item.getItemMeta().getLore());
	    		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
	    		
	    		icon.setItemMeta(meta);
	    		icon.setAmount(1);
	    		
	    		e.setCurrentItem(icon);

	    		p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "已更新復活點圖示");
	    		
	    		return;
	    		
	    	}
	    		
	    	//重設復活點
	    	if(click.equals(ClickType.SHIFT_RIGHT)){

	    		Location loc = p.getLocation();
	    		
	    		if(spawns.getConfig().isSet("spawns")){
	    			  
					spawns.set("spawns." + id + ".location", loc);
					
					nms.sendLocation(p, name, loc);
					
				}

	    		ItemMeta meta = item.getItemMeta();
	    		
	    		lore.set(2, ChatColor.BLUE + "X座標:" + loc.getX());
	    		lore.set(3, ChatColor.BLUE + "Y座標:" + loc.getY());
	    		lore.set(4, ChatColor.BLUE + "Z座標:" + loc.getZ());
	    		
	    		meta.setLore(lore);
	    		
	    		item.setItemMeta(meta);
	    		
	    		e.setCurrentItem(item);
	    		
	    		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
	    	}
	    		
	    	//移除復活點
	    	if(click.equals(ClickType.SHIFT_LEFT)){

	    		spawns.set("spawns." + id, null);
	    		
	    		p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
	    			
	    		try{
	    			String s = gui.getItem(31).getItemMeta().getDisplayName().replace("§9-第", "").replace("頁-", "");
	    			int page_num = Integer.parseInt(s);
	    			
	    			//如果刪除的復活點剛好是新一頁的第一個而且後面沒有其他復活點
	    			if ((page_num > 1) && (slot == 0) && (gui.getItem(1) == null)) {
	    				page_num--;
	    			}
	    			
	    			pfunc.openSpawnList(p, page_num);
	    			
	    		}catch(NumberFormatException ex){
	    			ex.printStackTrace();
	    			WarningGen.Warn("在獲取目前所在頁數時出了問題");
	    		}
	    		p.sendMessage(Global.Header + ChatColor.DARK_RED + "已將復活點 §f§l[§r" + name + "§f§l] " + ChatColor.RESET + ChatColor.DARK_RED + "移除!");
	    	}
	    	
	    	//重新命名復活點
	    	if(click.equals(ClickType.RIGHT)){

	    		List<String> info = new ArrayList<String>();
	    		
	    		info.add(ChatColor.GOLD + "更新前名稱:");
	    		info.add(name);
	    		info.add("");
	    		info.add(ChatColor.DARK_RED + "備註: 若要在名稱中顯示&，請使用/&");
	    		info.add("");
	    		info.add(ChatColor.BLUE + "點擊左方的格子來回復原本名稱");
	    		info.add(ChatColor.AQUA + "點擊中間的格子來預覽結果");
	    		info.add(ChatColor.GREEN + "點擊右方的格子來確認更改");
	    		info.add(ChatColor.DARK_GRAY + "ID:" + id);
	        
	    		anvil.openAnvil(p, name, info);
	    		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1);
	    	}
	    }
	    
	    //傳送至復活點
	    if((click.equals(ClickType.LEFT)) && (slot <= 26)){

	    	if(Global.didNotChoose(p)){
	    		pfunc.removeNameTag(p);
	    		Global.removeNoChoose(p);
	    	}
	    	
	    	Location loc = (Location) spawns.getConfig().get("spawns." + id + ".location");
	    	
	    	//先讓名條被刪除再傳送
	    	new BukkitRunnable() {

	    		@Override
	    		public void run(){
	    			
	    	    	p.teleport(loc);
	    	    	
	    	    	if(Global.isGhost(p)){
	    	    		pfunc.TurnBack(p);
	    	    	}
	    	    	
	    	    	p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
	    		}
	    		
	    	}.runTaskLater(core, 2);
	    	
	   	}
	}
		
	//改名稱系統
	@EventHandler
	public void onCloseAnvil(InventoryCloseEvent e){
		
	    Inventory inv = e.getInventory();
	    Player p = (Player) e.getPlayer();
	    
	    if(!inv.getType().equals(InventoryType.ANVIL)){
	    	return;
	    }
	    if(inv.getItem(0) == null){
	    	return;
	    }
	    
	    ItemStack item = inv.getItem(0);
	    
	    if(!item.getItemMeta().hasLore()){
	    	return;
	    }
	    
	    if(item.getItemMeta().getLore().get(0).toString().equals(ChatColor.GOLD + "更新前名稱:")){
	    	inv.clear();
	    	p.sendMessage(Global.Header + ChatColor.RED + "已取消重新命名復活點");
	    	p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_AMBIENT, 1, 1);
	    }
	    
	}
	@EventHandler
	public void onAnvilClick(InventoryClickEvent e){
		
		if(e.getClickedInventory() == null){
			return;
		}
		if(!(e.getWhoClicked() instanceof Player)){
	    	return;
	    }
	    if(!e.getClickedInventory().getType().equals(InventoryType.ANVIL)){
	    	return;
	    }
	    if(e.getInventory().getItem(0) == null){
	    	return;
	    }
	    
	    ItemStack item0 = e.getClickedInventory().getItem(0);
	    if(!item0.getItemMeta().hasLore()){
	    	return;
	    }
	    
	    List<String> lore0 = item0.getItemMeta().getLore();
	    
	    if(!lore0.get(0).toString().equals(ChatColor.GOLD + "更新前名稱:")){
	    	return;
	    }
	    
	    e.setCancelled(true);
	    
	    Player p = (Player) e.getWhoClicked();
	    InventoryView view = e.getView();
	    int rawSlot = e.getRawSlot();
	    
	    if(rawSlot == view.convertSlot(rawSlot)){
	    	
	    	if(rawSlot == 0){
	    		
	    		if(e.getInventory().getItem(1) != null){
	    			e.getClickedInventory().setItem(1, null);
	    		}
	    		
	    		p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	        
	    	}else if(rawSlot == 1){

	    		if(e.getInventory().getItem(2) == null){
	    			
	    			//再次點擊關閉預覽
	    			if(e.getInventory().getItem(1) != null){
		    			e.getClickedInventory().setItem(1, null);
		    			
		    			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
		    		}
	    			
	    			return;
	    			
	    		}
	    		
	    		ItemMeta meta2 = e.getClickedInventory().getItem(2).getItemMeta();
	    		
	    		String name0 = item0.getItemMeta().getDisplayName().replace("&", "§").replace("/§", "&");
	    		
	    		String name2 = meta2.getDisplayName().replace("&", "§").replace("/§", "&");
	    		
	    		List<String> lore1 = new ArrayList<String>();
	    		
	    		lore1.add(ChatColor.GOLD + "更新前名稱:");
	    		lore1.add(name0);
	    		lore1.add("");
	    		lore1.add(ChatColor.GOLD + "更新後名稱:");
	    		lore1.add(name2);
	    		lore1.add("");
	    		lore1.add(ChatColor.GREEN + "再次點擊此格關閉預覽");
	    		
	    		e.getClickedInventory().setItem(1, im.createItem(Material.ENCHANTED_BOOK, 0, ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "名稱預覽", lore1, false));
	    		
	    		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
	    		
	    	}else if(rawSlot == 2){
	    		
	    		ItemStack item2 = e.getInventory().getItem(2);
	    		
	    		if(item2 == null){
	    			return;
	    		}
	    		
	    		String newname = item2.getItemMeta().getDisplayName().replace("&", "§").replace("/§", "&");
	    		
	    		String id = lore0.get(8).replace(ChatColor.DARK_GRAY + "ID:", "");
	    		
	    		if(ChatColor.stripColor(newname).startsWith(" ") || ChatColor.stripColor(newname).endsWith(" ")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	          
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "開頭和結尾不能有空格!");
	    			return;
	    		}
	    		
	    		//將多個連續的空格變成一個
	    		if(newname.contains(" ")){
	    			String[] l = newname.split(" ");
	    			newname = "";
	    		  
	    			for(int i = 0; i < l.length; i++){
	    			  
	    				if(newname != ""){
	    					if(!l[i].isEmpty()){
	    						newname += " " + l[i];
	    					}
	    				}else{
	    					newname = l[i];
	    				}
	    			}
	    		}
	    	  
	    		for(String temp_id : spawns.getConfig().getConfigurationSection("spawns").getKeys(false)){
	          
	    			if(spawns.getConfig().getString("spawns." + temp_id + ".name").equals(newname)){
	    				p.sendMessage(Global.Header + ChatColor.DARK_RED + "這個名字已經用過了!");
	    				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    				return;
	    			}
	    		}
	        
	    		if(ChatColor.stripColor(newname).equals("")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "你似乎沒有輸入文字或只輸入格式碼!");
	    			return;
	    		}
	    		if(ChatColor.stripColor(newname).contains("§")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "要在名稱中顯示&，請使用/&");
	    			return;
	    		}
	    		
	    		spawns.set("spawns." + id + ".name", newname);
	    		
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "已將復活點名稱改為 " + ChatColor.WHITE + ChatColor.BOLD + "[" + ChatColor.RESET + newname + ChatColor.WHITE + ChatColor.BOLD + "]");
	    		e.getClickedInventory().clear();
	    		p.closeInventory();
	    		p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
	      	}
	    }
	}
	
}
