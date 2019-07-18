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

	//�����٨S��_���I�N�����e��
	@EventHandler
	public void onCloseInventory(InventoryCloseEvent e){
		
	    final Player p = (Player) e.getPlayer();
	    Inventory gui = e.getInventory();
	    
	    if(!Global.hasTurnedPage(p)){
	    	
	    	if(gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "�_���I�ؿ�") && Global.didNotChoose(p)){
	    	    
	    		try{
	    			
	    			String s = gui.getItem(31).getItemMeta().getDisplayName().replace("��9-��", "").replace("��-", "");
	    			final int page_num = Integer.parseInt(s);
	    			
	    			new BukkitRunnable() {

	    				@Override
	    				public void run(){
	    					pfunc.openSpawnList(p, page_num);
	    				}
	    				
	    			}.runTaskLater(core, 1);
	    			
	    		}catch(NumberFormatException ex){
	    			
	    			ex.printStackTrace();
	    			WarningGen.Warn("�b����ثe�Ҧb���ƮɥX�F���D");
	    			
	    		}
	    		
	    	}
	    	
	    }else{
	    	Global.removeTurnPage(p);
	    }
	}
	
	//����ưʩ�m�D���_���I�ؿ�
	@EventHandler
	public void onDragItem(InventoryDragEvent e) {
		
		Inventory gui = e.getInventory();
		
		Set<Integer> rawslots = e.getRawSlots();
				
		if(gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "�_���I�ؿ�")) {
			
			for(Integer ele : rawslots) {
				
				if(ele <= 35) {
					e.setCancelled(true);
					break;
				}
				
			}
			
	    }
		
	}
	
	//�_���I�ؿ������s
	@EventHandler
	public void onClickButton(InventoryClickEvent e){
				
	    Inventory gui = e.getInventory();
	    
	    if(!gui.getTitle().equals(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "�_���I�ؿ�")) {
	    	return;
	    }
	    
	    Player p = (Player) e.getWhoClicked();
	    ItemStack item = e.getCurrentItem();
	    ClickType click = e.getClick();
	    InventoryAction action = e.getAction();
	    int slot = e.getRawSlot();
	    
	    //�����I���Ū����s�Ω�D���Ů�
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
			    id = lore.get(0).toString().replace("��bID:", "");	
		    }
		    
	    }
	    
	    
	    //�_���I�ؿ������s�����ಾ��
	    if(slot >= 0 && slot <= 35)
	    	e.setCancelled(true);
	    
	    
	    //�����D��M����������ϥܥ洫
	    if(slot > 26) {

		    if(action == InventoryAction.SWAP_WITH_CURSOR) {
			    return;
		    }
		    
	    }
	    
	    //�����D���_���I�ؿ�
	    if(slot > 35) {
	    	
	    	if(click.equals(ClickType.SHIFT_LEFT) || click.equals(ClickType.SHIFT_RIGHT)) {
	    		e.setCancelled(true);
	    		return;
	    	}
	    	
	    }
	    
	    //�^��۵M�����I
	    if((click.equals(ClickType.LEFT)) && (slot == 27) && (name.equals(ChatColor.DARK_GREEN + "�۵M�����I"))){
		    
	    	if(Global.didNotChoose(p)){
		    	pfunc.removeNameTag(p);
	    		Global.removeNoChoose(p);
	    	}

	    	//�����W���Q�R���A�ǰe
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
	    
	    //�D��
	    if(click.equals(ClickType.LEFT) && slot == 29 && name.equals(ChatColor.BOLD + "" + ChatColor.DARK_RED + "�D��")) {
	    	pfunc.yell(p);
	    }
	    
	    //�W�@��
	    if((click.equals(ClickType.LEFT)) && (slot == 30) && (name.equals(ChatColor.BLUE + "�W�@��"))){

	    	p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	    		
	    	try{
	    			
	    		String s = gui.getItem(31).getItemMeta().getDisplayName().replace("��9-��", "").replace("��-", "");
	    		int page_num = Integer.parseInt(s);
	    		
	    		if (page_num - 1 > 0){
	    			if(Global.isGhost(p)) Global.addTurnPage(p);
	    			pfunc.openSpawnList(p, page_num - 1);
	    		}
	    			
	    	}catch(NumberFormatException ex){
	    		ex.printStackTrace();
	    		WarningGen.Warn("�b����ثe�Ҧb���ƮɥX�F���D");
	    	}
	    }
	    	
	    //�U�@��
	    if((click.equals(ClickType.LEFT)) && (slot == 32) && (name.equals(ChatColor.BLUE + "�U�@��"))){

	    	p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
	    		
	    	try{
	    			
	    		String s_page_num = gui.getItem(31).getItemMeta().getDisplayName().replace("��9-��", "").replace("��-", "");
	    		int page_num = Integer.parseInt(s_page_num);
	        
	    		String s_total = gui.getItem(31).getItemMeta().getLore().get(0).toString().replace("��2 �@", "").replace("��", "");
	    		int total = Integer.parseInt(s_total);
	    		
	    		if(page_num + 1 <= total){
	    			if(Global.isGhost(p)) Global.addTurnPage(p);
	    			pfunc.openSpawnList(p, page_num + 1);
	    		}
	    			
	    	}catch(NumberFormatException ex){
	    		ex.printStackTrace();
	    		WarningGen.Warn("�b����ثe�Ҧb���Ʃ��`���ƮɥX�F���D");
	    	}
	    }
	    
	    //���O���F�ɡA�i�H�s��ۭq�_���I
	    if(!Global.isGhost(p) && (slot <= 26)){
	    	
	    	//���ϥ�
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
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�w��s�_���I�ϥ�");
	    		
	    		return;
	    		
	    	}
	    		
	    	//���]�_���I
	    	if(click.equals(ClickType.SHIFT_RIGHT)){

	    		Location loc = p.getLocation();
	    		
	    		if(spawns.getConfig().isSet("spawns")){
	    			  
					spawns.set("spawns." + id + ".location", loc);
					
					nms.sendLocation(p, name, loc);
					
				}

	    		ItemMeta meta = item.getItemMeta();
	    		
	    		lore.set(2, ChatColor.BLUE + "X�y��:" + loc.getX());
	    		lore.set(3, ChatColor.BLUE + "Y�y��:" + loc.getY());
	    		lore.set(4, ChatColor.BLUE + "Z�y��:" + loc.getZ());
	    		
	    		meta.setLore(lore);
	    		
	    		item.setItemMeta(meta);
	    		
	    		e.setCurrentItem(item);
	    		
	    		p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
	    	}
	    		
	    	//�����_���I
	    	if(click.equals(ClickType.SHIFT_LEFT)){

	    		spawns.set("spawns." + id, null);
	    		
	    		p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
	    			
	    		try{
	    			String s = gui.getItem(31).getItemMeta().getDisplayName().replace("��9-��", "").replace("��-", "");
	    			int page_num = Integer.parseInt(s);
	    			
	    			//�p�G�R�����_���I��n�O�s�@�����Ĥ@�ӦӥB�᭱�S����L�_���I
	    			if ((page_num > 1) && (slot == 0) && (gui.getItem(1) == null)) {
	    				page_num--;
	    			}
	    			
	    			pfunc.openSpawnList(p, page_num);
	    			
	    		}catch(NumberFormatException ex){
	    			ex.printStackTrace();
	    			WarningGen.Warn("�b����ثe�Ҧb���ƮɥX�F���D");
	    		}
	    		p.sendMessage(Global.Header + ChatColor.DARK_RED + "�w�N�_���I ��f��l[��r" + name + "��f��l] " + ChatColor.RESET + ChatColor.DARK_RED + "����!");
	    	}
	    	
	    	//���s�R�W�_���I
	    	if(click.equals(ClickType.RIGHT)){

	    		List<String> info = new ArrayList<String>();
	    		
	    		info.add(ChatColor.GOLD + "��s�e�W��:");
	    		info.add(name);
	    		info.add("");
	    		info.add(ChatColor.DARK_RED + "�Ƶ�: �Y�n�b�W�٤����&�A�Шϥ�/&");
	    		info.add("");
	    		info.add(ChatColor.BLUE + "�I�����誺��l�Ӧ^�_�쥻�W��");
	    		info.add(ChatColor.AQUA + "�I����������l�ӹw�����G");
	    		info.add(ChatColor.GREEN + "�I���k�誺��l�ӽT�{���");
	    		info.add(ChatColor.DARK_GRAY + "ID:" + id);
	        
	    		anvil.openAnvil(p, name, info);
	    		p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1, 1);
	    	}
	    }
	    
	    //�ǰe�ܴ_���I
	    if((click.equals(ClickType.LEFT)) && (slot <= 26)){

	    	if(Global.didNotChoose(p)){
	    		pfunc.removeNameTag(p);
	    		Global.removeNoChoose(p);
	    	}
	    	
	    	Location loc = (Location) spawns.getConfig().get("spawns." + id + ".location");
	    	
	    	//�����W���Q�R���A�ǰe
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
		
	//��W�٨t��
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
	    
	    if(item.getItemMeta().getLore().get(0).toString().equals(ChatColor.GOLD + "��s�e�W��:")){
	    	inv.clear();
	    	p.sendMessage(Global.Header + ChatColor.RED + "�w�������s�R�W�_���I");
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
	    
	    if(!lore0.get(0).toString().equals(ChatColor.GOLD + "��s�e�W��:")){
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
	    			
	    			//�A���I�������w��
	    			if(e.getInventory().getItem(1) != null){
		    			e.getClickedInventory().setItem(1, null);
		    			
		    			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
		    		}
	    			
	    			return;
	    			
	    		}
	    		
	    		ItemMeta meta2 = e.getClickedInventory().getItem(2).getItemMeta();
	    		
	    		String name0 = item0.getItemMeta().getDisplayName().replace("&", "��").replace("/��", "&");
	    		
	    		String name2 = meta2.getDisplayName().replace("&", "��").replace("/��", "&");
	    		
	    		List<String> lore1 = new ArrayList<String>();
	    		
	    		lore1.add(ChatColor.GOLD + "��s�e�W��:");
	    		lore1.add(name0);
	    		lore1.add("");
	    		lore1.add(ChatColor.GOLD + "��s��W��:");
	    		lore1.add(name2);
	    		lore1.add("");
	    		lore1.add(ChatColor.GREEN + "�A���I�����������w��");
	    		
	    		e.getClickedInventory().setItem(1, im.createItem(Material.ENCHANTED_BOOK, 0, ChatColor.YELLOW + "" + ChatColor.UNDERLINE + "�W�ٹw��", lore1, false));
	    		
	    		p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
	    		
	    	}else if(rawSlot == 2){
	    		
	    		ItemStack item2 = e.getInventory().getItem(2);
	    		
	    		if(item2 == null){
	    			return;
	    		}
	    		
	    		String newname = item2.getItemMeta().getDisplayName().replace("&", "��").replace("/��", "&");
	    		
	    		String id = lore0.get(8).replace(ChatColor.DARK_GRAY + "ID:", "");
	    		
	    		if(ChatColor.stripColor(newname).startsWith(" ") || ChatColor.stripColor(newname).endsWith(" ")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	          
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "�}�Y�M�������঳�Ů�!");
	    			return;
	    		}
	    		
	    		//�N�h�ӳs�򪺪Ů��ܦ��@��
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
	    				p.sendMessage(Global.Header + ChatColor.DARK_RED + "�o�ӦW�r�w�g�ιL�F!");
	    				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    				return;
	    			}
	    		}
	        
	    		if(ChatColor.stripColor(newname).equals("")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "�A���G�S����J��r�Υu��J�榡�X!");
	    			return;
	    		}
	    		if(ChatColor.stripColor(newname).contains("��")){
	    			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	    			p.sendMessage(Global.Header + ChatColor.DARK_RED + "�n�b�W�٤����&�A�Шϥ�/&");
	    			return;
	    		}
	    		
	    		spawns.set("spawns." + id + ".name", newname);
	    		
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�w�N�_���I�W�٧אּ " + ChatColor.WHITE + ChatColor.BOLD + "[" + ChatColor.RESET + newname + ChatColor.WHITE + ChatColor.BOLD + "]");
	    		e.getClickedInventory().clear();
	    		p.closeInventory();
	    		p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
	      	}
	    }
	}
	
}
