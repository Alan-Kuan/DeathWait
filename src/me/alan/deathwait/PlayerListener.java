package me.alan.deathwait;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import me.alan.deathwait.files.Config;
import me.alan.deathwait.files.Data;
import me.alan.deathwait.nms.NMS;

public class PlayerListener implements Listener{

	private Core core;
	
	private Config config;
	private Data data;
	
	private PlayerFunctions pfunc;
	
	private ItemMaker im;
	
	private NMS nms;
	
	public PlayerListener(Core core){
		
		this.core = core;
		
		config = core.getConfigClass();
		data = core.getDataClass();
		
		pfunc = core.getPlayerFunctionsClass();
		
		im = new ItemMaker();

		nms = core.getNMSClass();
		
	}
	
	//�p�G�ӥͪ��Otarget entity�A����̭����F��
	public void targetEntityCheck(Entity ent) {

	    if(Global.isTargetEntity(ent)){
	    	Player victim = (Player) Global.getPlayerInTargetEntity(ent);
	    	
	    	nms.setSpectate(victim, victim);
	    	//victim.setSpectatorTarget(victim);
	    	Global.removeTargetEntity(ent, victim);
	    	pfunc.setNameTag(victim);
	    }
	    
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e){
		
		Player p = (Player) e.getEntity();

	    PlayerInventory inv = p.getInventory();

    	DamageCause cause = p.getLastDamageCause().getCause();
    	
	    //�T�{���a�O�_�����������
    	if(inv.getItemInMainHand().getType().equals(Material.TOTEM) || inv.getItemInOffHand().getType().equals(Material.TOTEM)) {
    		
    		//�������˵L�k���^�쪩/kill�M���J��Ū����p�A�P�˦a�AEssentials��/kill�B/suicide����L�k���^
    		if(!(cause.equals(DamageCause.VOID) || cause.equals(DamageCause.CUSTOM) || cause.equals(DamageCause.SUICIDE)))
    			return;
    		
    	}
    	
	    p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
	    p.setVelocity(new Vector(0, 0, 0));

	    //�p�G���a�w�g�O���F�A�N���n���i�@�B���B�z
	    if(Global.isGhost(p)) return;
	    
		//�T�{���a�O�_��target entity
		targetEntityCheck(p);
		
	    String keepinv = p.getWorld().getGameRuleValue("keepInventory");
	    boolean remove_effect = config.getConfig().getBoolean("config.remove effects after death");
	    boolean lose_xp = config.getConfig().getBoolean("config.lose XP after death");
	    boolean enable_killer_view = config.getConfig().getBoolean("config.enable killer view");
	    boolean allow_moving = config.getConfig().getBoolean("config.allow moving in ghost mode");
	    boolean reset_food_level = config.getConfig().getBoolean("config.reset food level after death");
	    boolean have_to_wait = true;
	    
	    Global.setGameMode(p, p.getGameMode());
	    Global.addGhost(p);
	    	    
	    //��U�M�b�Ӫ��a���W���ͪ�
	    pfunc.kickPassenger(p);
	    
	    //�����M��
	    p.teleport(p.getLocation());
	    
	    //�Y���i���
	    if(cause.equals(DamageCause.VOID) && (p.getLocation().getY() < 0.0)){
	    	World w = p.getWorld();
	    	double x = p.getLocation().getX();
	    	double z = p.getLocation().getZ();
	    	
	    	p.teleport(new Location(w, x, -2.0, z));
	    }
	    
	    //�M���Ĥ��ĪG
	    if(remove_effect){
	    	for(PotionEffect ef : p.getActivePotionEffects()){
	    		p.removePotionEffect(ef.getType());
	    	}
	    }
	    
	    //�����g���
	    if(lose_xp){
	    	int exp = p.getLevel() * 7;
	      
	    	if(exp > 0){
	    		ExperienceOrb orb = (ExperienceOrb) p.getWorld().spawnEntity(p.getLocation(), EntityType.EXPERIENCE_ORB);
	      
	    		if(exp > 100){
	    			exp = 100;
	    		}
	    		orb.setExperience(exp);
	    		p.setLevel(0);
	    		p.setExp(0.0f);
	    	}
	    }

	    //�̷�Gamerule�ӨM�w�n���n�����D��
	    if(keepinv.equals("false")){
	    	ItemStack[] items = inv.getContents();
	    	int length = items.length;
	    	
	    	for(int slot = 0; slot < length; slot++){
	    		ItemStack item = items[slot];
	    		
	    		if(item != null){
	    			p.getWorld().dropItem(p.getLocation(), item);
	    		}
	    	}
	    	inv.clear();
	    }
	    
	    //���\����
	    if(allow_moving){
	    	p.setFlySpeed(0.1f);
	    }else{
	    	p.setFlySpeed(0);
	    }
	    
	    //�^�_������
	    if(reset_food_level){
	    	p.setFoodLevel(20);
	    }
	    
	    if(p.hasPermission("dw.bypass")){
	    	
	    	have_to_wait = false;
	    	
	    //�p�G�S���v���~�|�������_���B��
	    }else{
	    	
	    	int amount = 0;
	    	
		    if(data.getConfig().isSet("players." + p.getUniqueId() + ".quota")){
		    	amount = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
		    }
		    
		    if(amount > 0){
		    	have_to_wait = false;
		    	data.set("players." + p.getUniqueId(), Integer.valueOf(amount - 1));
		    }
		    
	    }
	    
	    p.setGameMode(GameMode.SPECTATOR);
	    
	    if(enable_killer_view && (p.getLastDamageCause() instanceof EntityDamageByEntityEvent)){
	    	
	    	EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) p.getLastDamageCause();
	    	
	    	Entity killer = Global.getKiller(edbee.getDamager());
	    	
	    	//�p�G����s�b �B ���a�ۤv���O���� �B ���⤣�O�z������θ��F
	    	if(killer != null && !p.equals(killer) && !Global.isExplosiveEntity(killer)) {
	    		nms.setSpectate(p, killer);
	    		//p.setSpectatorTarget(killer);
	    		Global.addTargetEntity(killer, p);
	    	}
	    		    	
	    }
	    
	    //��ܦW��
	    pfunc.setNameTag(p);
	    
	    //�ݭn����
	    if(have_to_wait){
	    	
	    	int wait = config.getConfig().getInt("config.waiting seconds");
	    	
	    	nms.sendTitle(p, ChatColor.RED + "�A�w�g���F", 0, wait*20, 0);
	    	
	    	BukkitTask countdwon_task = new BukkitRunnable(){

		    	int temp = wait;
		    	
		    	@Override
		    	public void run(){
		    		
		    		Global.setLeftWaitingTime(p, temp);
		    		
		    		if(temp > 0){
		    				
		    			String show = temp + "����_��";
		            
		    			nms.sendSubTitle(p, ChatColor.GOLD + show, 0, 25, 0);
		    				
		    			temp--;
		    			
		    		}else{
		    			cancel();
		    			pfunc.Respawn(p);
		    		}
		    		
		    	}
		    	
	    	}.runTaskTimer(core, 0, 20);
	    	
	    	Global.addCountdownTask(p, countdwon_task);
	    	
	    }else{
	    	
	    	pfunc.Respawn(p);
	    	
	    }
	    
	}
	
	//���̭����F��ͪ����`�A�F��Q����
	@EventHandler
	public void onTargetEntityDeath(EntityDeathEvent e){
		
	    Entity ent = e.getEntity();
	    
	    targetEntityCheck(ent);
	    
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e){
		
		Player p = e.getPlayer();
		
		//�p�G�h�X�����a�̭�����L���a���F��A�F��Q����
		targetEntityCheck(p);
	    
	    //�p�G�h�X�����a�O���F
	    if(Global.isGhost(p)){
	    	
	    	pfunc.removeNameTag(p);
	    	
	    	data.set("players." + p.getUniqueId() + ".is ghost", true);
	    	
	    	data.set("players." + p.getUniqueId() + ".gamemode", Global.getGameMode(p).toString());
	    	
	    	if(Global.hasLeftWaitingTime(p)){
	    		
		    	Global.cancelCountdownTask(p);
		    	
		    	data.set("players." + p.getUniqueId() + ".left waiting time", Global.getLeftWaitingTime(p));
		    	
		    	Global.removeLeftWaitingTime(p);
	    	}

	    	Global.removeGameMode(p);
			Global.removeGhost(p);
			
			if(Global.isInTargetEntity(p)){
				Entity target = Global.getTargetEntity(p);
				data.set("players." + p.getUniqueId() + ".target entity", target.getUniqueId().toString());
				Global.removeTargetEntity(target, p);
			}
			
			//�p�G���b��ܴ_���I
			if(Global.didNotChoose(p)) {
				data.set("players." + p.getUniqueId() + ".is browsing spawn list", true);
				Global.removeNoChoose(p);
			}else {
				data.set("players." + p.getUniqueId() + ".is browsing spawn list", false);
			}
			
	    }
	    
	}
	//�p�G�b���ݮɵn�X�A�U���n�J�ɭn�~�򵥫�
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
				
		Player p = e.getPlayer();
		
		boolean is_ghost = data.getConfig().isSet("players." + p.getUniqueId() + ".is ghost");
		
		boolean is_browsing = data.getConfig().getBoolean("players." + p.getUniqueId() + ".is browsing spawn list");
		
		if(is_ghost){
			
			boolean allow_moving = config.getConfig().getBoolean("config.allow moving in ghost mode");

			Global.addGhost(p);
			
			data.set("players." + p.getUniqueId() + ".is ghost", null);
			
			Global.setGameMode(p, GameMode.valueOf(data.getConfig().getString("players." + p.getUniqueId() + ".gamemode")));
			
			data.set("players." + p.getUniqueId() + ".gamemode", null);

			new BukkitRunnable(){

		    	@Override
		    	public void run(){
		    		
				    if(allow_moving){
						p.setFlySpeed(0.1f);
					}else{
						p.setFlySpeed(0);
					}

					String uuid_of_target_entity = "";
					
					if(data.getConfig().isSet("players." + p.getUniqueId() + ".target entity")) {
					
						uuid_of_target_entity = data.getConfig().getString("players." + p.getUniqueId() + ".target entity");
					
						//�p�G���a�ݪ�target entity�٦b����A�N�⪱�a��^�h
						for(Entity ent : p.getNearbyEntities(10.0, 10.0, 10.0)) {
							
							if(ent.getUniqueId().toString().equals(uuid_of_target_entity)) {
								nms.setSpectate(p, ent);
								Global.addTargetEntity(ent, p);
								data.set("players." + p.getUniqueId() + ".target entity", null);
								break;
							}
							
						}
						
					}
					
					//��ܦW��
					if(!Global.isInTargetEntity(p))
					    pfunc.setNameTag(p);
		    		
		    	}
		    	
			}.runTaskLater(core, 2);
    		
			//�p�G���}�ɥ��b�s���_���I�ؿ�
			if(is_browsing) {

	    	    int time_limit = config.getConfig().getInt("config.time limit of browsing the list");
	    		
	    		Global.addNoChoose(p);
	    		
	    		new BukkitRunnable() {

	    			@Override
	    			public void run(){
	    				
	    				pfunc.openSpawnList(p, 1);
	    				
	    			}
	    			
	    		}.runTaskLater(core, 1);
	    		
				if(time_limit > 0) {
					pfunc.choosingCountDown(p, time_limit);
				}
				
				data.set("players." + p.getUniqueId() + ".is browsing spawn list", null);
				
			}else {

				p.sendMessage(Global.Header + ChatColor.DARK_RED + "�W�����}�ɤ��b���ݴ_��");
				
				int left = data.getConfig().getInt("players." + p.getUniqueId() + ".left waiting time");
				
				nms.sendTitle(p, ChatColor.RED + "�A�w�g���F", 0, left*20, 0);
				
				BukkitTask countdown_task = new BukkitRunnable(){

					int temp = left;
					
			    	@Override
			    	public void run(){

			    		Global.setLeftWaitingTime(p, temp);
			    		
			    		if(temp > 0){
			    			
			    			String show = temp + "����_��";
			    				
			    			nms.sendSubTitle(p, ChatColor.GOLD + show, 0, 25, 0);
			    				
			    			temp--;
			    			
			    		}else{
			    			cancel();
			    			data.set("players." + p.getUniqueId() + ".left waiting time", null);
			    			pfunc.Respawn(p);
			    		}
			    		
			    	}
			    	
				}.runTaskTimer(core, 0, 20);
				
				Global.addCountdownTask(p, countdown_task);
				
			}
		}
			
	}
	
	//�����a�ϥ��ۤU�ӨD��
	@EventHandler
	public void onShiftYelling(PlayerToggleSneakEvent e) {
		
		Player p = e.getPlayer();
		
		if(p.isSneaking() && Global.isGhost(p) && p.hasPermission("dw.yell")) {
			pfunc.yell(p);
		}
		
	}
	
	//���F�Ҧ��T�Ϋ��O
	@EventHandler
	public void onCommandPreprocess(PlayerCommandPreprocessEvent e){
		
	    Player p = e.getPlayer();
	    
		if(Global.isGhost(p)){
			e.setCancelled(true);
	    }
		
	}
	
	//�ϥιD��
	@EventHandler
	public void onUseItem(PlayerInteractEvent e){
			    
	    Player p = e.getPlayer();
	    PlayerInventory inv = p.getInventory();
	    Action action = e.getAction();
	    ItemStack item;
	    
		if(e.getHand() == EquipmentSlot.HAND){
			item = inv.getItemInMainHand();
	    }else if(e.getHand() == EquipmentSlot.OFF_HAND){
	    	item = inv.getItemInOffHand();
	    }else{
	    	return;
	    }
		
		if(item.getType() == Material.AIR){
			return;
		}
		
	    ItemMeta meta = item.getItemMeta();
	    
	    ItemStack instant_respawn_item = im.createItem(Material.getMaterial(config.getConfig().getString("config.instant respawn item.type")),
				config.getConfig().getInt("config.instant respawn item.damage"),
				config.getConfig().getString("config.instant respawn item.name"),
				config.getConfig().getStringList("config.instant respawn item.lore"), true);
		ItemStack assistant_respawn_item = im.createItem(Material.getMaterial(config.getConfig().getString("config.assistant respawn item.type")),
				config.getConfig().getInt("config.assistant respawn item.damage"),
				config.getConfig().getString("config.assistant respawn item.name"),
				config.getConfig().getStringList("config.assistant respawn item.lore"), true);
	    
	    if(!meta.hasLore()){
	    	return;
	    }
	    if(!meta.hasDisplayName()){
	    	return;
	    }
	    
	    if((action.equals(Action.RIGHT_CLICK_AIR)) || (action.equals(Action.RIGHT_CLICK_BLOCK))){
	    	
	    	//�����_���D��
	    	if((meta.getDisplayName().equals(instant_respawn_item.getItemMeta().getDisplayName())) && (meta.getLore().equals(instant_respawn_item.getItemMeta().getLore()))){
	    		e.setCancelled(true);
	        
	    		int amount = 0;
	    		if(data.getConfig().isSet("players." + p.getUniqueId() + ".quota")){
	    			amount = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
	    		}
	    		data.set("players." + p.getUniqueId() + ".quota", Integer.valueOf(amount + 1));
	    		
	    		if(item.getAmount() > 1){
	    			item.setAmount(item.getAmount() - 1);
	    		}else{
	    			
	    			if(e.getHand() == EquipmentSlot.HAND){
	    				inv.setItemInMainHand(null);
	    			}else{
	    				inv.setItemInOffHand(null);
	    			}

	    		}
	    		
	    		int quota = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
	    		
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�w�W�[�@���K���B��");
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�Ѿl" + quota + "���K���B��");
	    	}
	    	
	    	//��a�_���D��
	    	if((meta.getDisplayName().equals(assistant_respawn_item.getItemMeta().getDisplayName())) && (meta.getLore().equals(assistant_respawn_item.getItemMeta().getLore()))){
	    		e.setCancelled(true);
	    		
	    		for(Entity ent : p.getNearbyEntities(1.0, 0.0, 1.0)){
	    			if((ent instanceof Player)){

    					Player entp = (Player) ent;
    					
	    				if(Global.isGhost(entp) && !Global.isInTargetEntity(entp)){
	    					
	    					if(Global.didNotChoose(entp)){
	    						Global.removeNoChoose(entp);
	    						Global.cancelTimeLimitTask(entp);
	    						entp.closeInventory();
	    					}
	    					
	    					if(Global.hasCountdownTask(entp)){
		    					Global.cancelCountdownTask(entp);
	    					}
	    					
	    					pfunc.removeNameTag(entp);
	    					entp.teleport(entp.getLocation().add(0.0, 0.2, 0.0));
	    					pfunc.TurnBack(entp);
	              
	    					nms.sendTitle(entp, "", 0, 40, 0);
	    					nms.sendSubTitle(entp, 
	    							ChatColor.DARK_GREEN + p.getName() + "�ϥ�" + ChatColor.RESET + assistant_respawn_item.getItemMeta().getDisplayName() + ChatColor.DARK_GREEN + "���A��a�_��",
	    							0, 40, 10);
	    					entp.getWorld().spawnEntity(entp.getLocation(), EntityType.FIREWORK);
	    					if(item.getAmount() > 1){
	    						item.setAmount(item.getAmount() - 1);
	    						break;
	    					}

	    	    			if(e.getHand() == EquipmentSlot.HAND){
	    	    				inv.setItemInMainHand(null);
	    	    			}else{
	    	    				inv.setItemInOffHand(null);
	    	    			}
	              
	    					break;
	    				}
	    			}
	    		}
	    	}
	    }
	    if((action.equals(Action.LEFT_CLICK_AIR)) || (action.equals(Action.LEFT_CLICK_BLOCK))){
	    	if((meta.getDisplayName().equals(instant_respawn_item.getItemMeta().getDisplayName())) && (meta.getLore().equals(instant_respawn_item.getItemMeta().getLore()))){
	    		e.setCancelled(true);
	    		
	    		int quota = data.getConfig().getInt("players." + p.getUniqueId() + ".quota");
	    		p.sendMessage(Global.Header + ChatColor.GREEN + "�Ѿl" + quota + "���K���B��");
	    	}
	    }
	}
	
}