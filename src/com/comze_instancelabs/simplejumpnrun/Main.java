package com.comze_instancelabs.simplejumpnrun;


// JUMPER

/**
 * 
 * @author instancelabs
 *
 */

import java.util.ArrayList;
import java.util.HashMap;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;


public class Main extends JavaPlugin implements Listener{
	
	
	public static Economy econ = null;
	public boolean economy = false;

	public static HashMap<Player, Location> checkpoints = new HashMap<Player, Location>();
	public static HashMap<Player, String> checkpointsarena= new HashMap<Player, String>();
	
	
	static HashMap<Player, String> arenap = new HashMap<Player, String>(); // playername -> arenaname
	static HashMap<Player, String> tpthem = new HashMap<Player, String>(); // playername -> arenaname
	
	
	@Override
	public void onEnable(){
		getServer().getPluginManager().registerEvents(this, this);
		
		getConfig().addDefault("config.use_economy", true);
		getConfig().addDefault("config.moneyreward_amount", 20.0);
		getConfig().addDefault("config.itemid", 264);
		getConfig().addDefault("config.itemamount", 1);
		
		getConfig().addDefault("strings.nopermission", "§4You don't have permission!");
		getConfig().addDefault("strings.createcourse", "§2Course saved. Now create a spawn. :)");
		getConfig().addDefault("strings.help1", "§2Jumper help:");
		getConfig().addDefault("strings.help2", "§2Use '/j createcourse <name>' to create a new course.");
		getConfig().addDefault("strings.help3", "§2Use '/j setmainlobby' to set the main lobby.");
		getConfig().addDefault("strings.help4", "§2Use '/j setspawn <name>' to set a new course spawn.");
		getConfig().addDefault("strings.lobbycreated", "§2Lobby successfully created!");
		getConfig().addDefault("strings.spawn", "§2Spawnpoint registered.");
		getConfig().addDefault("strings.courseremoved", "§4Course removed.");
		getConfig().addDefault("strings.reload", "§2Jumper config successfully reloaded.");
		getConfig().addDefault("strings.nothing", "§4This command action was not found.");
		getConfig().addDefault("strings.ingame", "§cUse /j leave to leave the course. Use /cp to get back to your last checkpoint.");
		getConfig().addDefault("strings.left", "§eYou left the course!");
		
		
		if(getConfig().getBoolean("config.use_economy")){
			economy = true;
			if (!setupEconomy()) {
	            getLogger().severe(String.format("[%s] - No iConomy dependency found! Disabling Economy.", getDescription().getName()));
	            //getServer().getPluginManager().disablePlugin(this);
	            economy = false;
	        }
		}
		
		getConfig().options().copyDefaults(true);
		this.saveConfig();
	}


	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if(cmd.getName().equalsIgnoreCase("j") || cmd.getName().equalsIgnoreCase("jumper")){
    		if(args.length < 1){
    			sender.sendMessage(getConfig().getString("strings.help1"));
    			sender.sendMessage(getConfig().getString("strings.help2"));
    			sender.sendMessage(getConfig().getString("strings.help3"));
    			sender.sendMessage(getConfig().getString("strings.help4"));
    		}else{
    			Player p = (Player)sender;
    			if(args.length > 0){
    				String action = args[0];
    				if(action.equalsIgnoreCase("createcourse") && args.length > 1){
    					// Create arena
    					if(p.hasPermission("jumper.create")){
    						this.getConfig().set(args[1] + ".name", args[1]);
	    	    			this.getConfig().set(args[1] + ".world", p.getWorld().getName());
	    	    			this.saveConfig();
	    	    			String arenaname = args[1];
	    	    			sender.sendMessage(getConfig().getString("strings.createcourse"));
    					}
    				}else if(action.equalsIgnoreCase("setmainlobby")){
    					// setlobby
    					if(p.hasPermission("jumper.setlobby")){
	    		    		Location l = p.getLocation();
	    		    		getConfig().set("lobbyspawn.x", (int)l.getX());
	    		    		getConfig().set("lobbyspawn.y", (int)l.getY());
	    		    		getConfig().set("lobbyspawn.z", (int)l.getZ());
	    		    		getConfig().set("lobbyspawn.world", p.getWorld().getName());
	    		    		this.saveConfig();
	    		    		sender.sendMessage(getConfig().getString("strings.lobbycreated"));
    					}
    				}else if(action.equalsIgnoreCase("setspawn") && args.length > 1){
    					// setspawn
    					if(p.hasPermission("jumper.setspawn")){
    						String arena = args[1];
    			    		Location l = p.getLocation();
    			    		getConfig().set(args[1] + ".spawn.x", (int)l.getX());
    			    		getConfig().set(args[1] + ".spawn.y", (int)l.getY());
    			    		getConfig().set(args[1] + ".spawn.z", (int)l.getZ());
    			    		getConfig().set(args[1] + ".spawn.world", p.getWorld().getName());
    			    		this.saveConfig();
    			    		sender.sendMessage(getConfig().getString("strings.spawn"));
    					}
    				}else if(action.equalsIgnoreCase("removecourse") && args.length > 1){
    					// removearena
    					if(p.hasPermission("jumper.remove")){
    						this.getConfig().set(args[1], null);
	    	    			this.saveConfig();
	    	    			sender.sendMessage(getConfig().getString("strings.courseremoved"));
    					}
    				}else if(action.equalsIgnoreCase("leave")){
    					// leave
    					//if(p.hasPermission("jumper.leave")){
    					if(arenap.containsKey(p)){
    						String arena = arenap.get(p);
    						final Location t = new Location(Bukkit.getWorld(getConfig().getString("lobbyspawn.world")), getConfig().getDouble("lobbyspawn.x"), getConfig().getDouble("lobbyspawn.y"), getConfig().getDouble("lobbyspawn.z"));
                			p.teleport(t);
                			arenap.remove(p);
                			checkpoints.remove(p);
                			checkpointsarena.remove(p);
                			p.sendMessage(getConfig().getString("strings.left"));
    					}else{
    						p.sendMessage("§aYou don't seem to be in a course right now!");
    					}
    					//}
    				}else if(action.equalsIgnoreCase("list")){
    					// list
    					if(p.hasPermission("jumper.list")){
    						ArrayList<String> keys = new ArrayList<String>();
	    			        keys.addAll(getConfig().getKeys(false));
	    			        try{
	    			        	keys.remove("config");
	    			        	keys.remove("strings");
	    			        	keys.remove("lobbyspawn");
	    			        }catch(Exception e){
	    			        	// do nothing
	    			        }
	    			        for(int i = 0; i < keys.size(); i++){
	    			        	if(!keys.get(i).equalsIgnoreCase("config") && !keys.get(i).equalsIgnoreCase("strings")){
	    			        		sender.sendMessage("§2" + keys.get(i));
	    			        	}
	    			        }
    					}
    				}else if(action.equalsIgnoreCase("reload")){
    					if(sender.hasPermission("jumper.reload")){
	    					this.reloadConfig();
	    					sender.sendMessage(getConfig().getString("strings.reload"));
    					}else{
    						sender.sendMessage(getConfig().getString("strings.nopermission"));
    					}
    				}else{
    					sender.sendMessage(getConfig().getString("strings.nothing"));
    				}
    			}
    		}
    		return true;
    	}else if(cmd.getName().equalsIgnoreCase("cp")){
    		if(sender instanceof Player){
    			final Player p = (Player)sender;
    			if(!arenap.containsKey(p)){
    				if(checkpointsarena.containsKey(p)){
    					arenap.put(p, checkpointsarena.get(p));
    				}
    			}
	    		if(checkpoints.containsKey(p)){
	    			Bukkit.getScheduler().runTaskLater(this, new Runnable(){
	    				public void run() {
	    					p.teleport(checkpoints.get(p));
	    					p.sendMessage("§7You were teleported to your checkpoint.");
	    				}
	    			}, 5L);
	    		}else{ 
	    			p.sendMessage("§cI can't find any checkpoints in my memory.. sowwy >.<");
	    		}
    		}
    		return true;
    	}
    	return false;
    }
	
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event){
		Player p = event.getPlayer();
		tpthem.put(p, arenap.get(p));
		arenap.remove(p);
	}
	
	@EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
		Player p = event.getEntity();
		arenap.remove(p);
	}
	
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		if(tpthem.containsKey(event.getPlayer())){
			String arena = tpthem.get(event.getPlayer());
			Player p = event.getPlayer();
			final Location t = new Location(Bukkit.getWorld(getConfig().getString("lobbyspawn.world")), getConfig().getDouble("lobbyspawn.x"), getConfig().getDouble("lobbyspawn.y"), getConfig().getDouble("lobbyspawn.z"));
			p.teleport(t);
		}
		updateScoreboard();
	}
	
	
	@EventHandler
	public void onSignUse(PlayerInteractEvent event)
	{	
	    if (event.hasBlock() && event.getAction() == Action.RIGHT_CLICK_BLOCK)
	    {
	        if (event.getClickedBlock().getType() == Material.SIGN_POST || event.getClickedBlock().getType() == Material.WALL_SIGN)
	        {
	            final Sign s = (Sign) event.getClickedBlock().getState();
	            if(getConfig().contains("player." + event.getPlayer().getName())){
	            	//TODO get date and if 24h cooldown true or false
	            }
                if (s.getLine(0).equalsIgnoreCase("§lJumper"))
                {
                	String arena = s.getLine(1);
                	arena = arena.substring(2);
                	Player p = event.getPlayer();
                	
                	arenap.put(event.getPlayer(), arena);
                	
                	event.getPlayer().sendMessage("§7You have entered " + arena + "!");
                	
                	final Location t = new Location(Bukkit.getWorld(getConfig().getString(arena + ".spawn.world")), getConfig().getDouble(arena + ".spawn.x"), getConfig().getDouble(arena + ".spawn.y"), getConfig().getDouble(arena + ".spawn.z"));
        			event.getPlayer().teleport(t);
                }else if(s.getLine(0).equalsIgnoreCase("§2[reward]")){
                	if(arenap.containsKey(event.getPlayer())){
                		EconomyResponse r = econ.depositPlayer(event.getPlayer().getName(), Double.parseDouble(s.getLine(1)));
            			if(!r.transactionSuccess()) {
            				event.getPlayer().sendMessage(String.format("An error occured: %s", r.errorMessage));
                            //sender.sendMessage(String.format("You were given %s and now have %s", econ.format(r.amount), econ.format(r.balance)));
                        }
	                	event.getPlayer().sendMessage("§7Congratulations you beat the course, here's your reward!");
	                	Player p = event.getPlayer();
	                	String arena = arenap.get(p);
						final Location t = new Location(Bukkit.getWorld(getConfig().getString("lobbyspawn.world")), getConfig().getDouble("lobbyspawn.x"), getConfig().getDouble("lobbyspawn.y"), getConfig().getDouble("lobbyspawn.z"));
            			p.teleport(t);
            			arenap.remove(p);
            			
            			updateScoreboard();
                	}
                }else if(s.getLine(0).equalsIgnoreCase("§6§l[checkpoint]")){
                	if(arenap.containsKey(event.getPlayer())){
                		checkpoints.put(event.getPlayer(), event.getPlayer().getLocation());
                		checkpointsarena.put(event.getPlayer(), arenap.get(event.getPlayer()));
                		event.getPlayer().sendMessage("§aSuccessfully set checkpoint.");
                	}
                }
	        }
	    }
	}
	
	
	@EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player p = event.getPlayer();
        if(event.getLine(0).toLowerCase().contains("[jumper]")){
        	if(event.getPlayer().hasPermission("jumper.sign")){
	        	event.setLine(0, "§lJumper");
	        	if(!event.getLine(1).equalsIgnoreCase("")){
	        		String arena = event.getLine(1);
	        		event.setLine(1, "§5" +  arena);
	        	}
        	}
        }else if(event.getLine(0).toLowerCase().contains("[reward]")){
        	if(event.getPlayer().hasPermission("jumper.sign")){
	        	event.setLine(0, "§2[Reward]");
	        	event.getPlayer().sendMessage("§2You have successfully created a reward sign for jumper!");
        	}
        }else if(event.getLine(0).toLowerCase().contains("[cp]")){
        	if(event.getPlayer().hasPermission("jumper.sign")){
	        	event.setLine(0, "§6§l[Checkpoint]");
	        	event.setLine(1, "Click for");
	        	event.setLine(2, "Checkpoint!");
	        	event.setLine(3, "/cp to use.");
	        	event.getPlayer().sendMessage("§2You have successfully created a checkpoint sign for jumper!");
        	}
        }
	}
	
	// water
	@EventHandler
    public void onmove(PlayerMoveEvent event){
		if(arenap.containsKey(event.getPlayer())){
			int x = event.getFrom().getBlockX();
	        int fromy = event.getFrom().getBlockY();
			int y = fromy;
			int z = event.getFrom().getBlockZ();
			Location loc = new Location(event.getFrom().getWorld(), x, y, z);
			
			Player p = event.getPlayer();
			
			String aren = arenap.get(p);
			
			if (loc.getBlock().isLiquid()){
				Location t = new Location(Bukkit.getWorld(getConfig().getString(aren + ".spawn.world")), getConfig().getDouble(aren + ".spawn.x"), getConfig().getDouble(aren + ".spawn.y"), getConfig().getDouble(aren + ".spawn.z"));
			    event.getPlayer().teleport(t);
			}
		}
	}
	
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
		if(arenap.containsKey(event.getPlayer())){
			// j leave
			if(event.getMessage().equalsIgnoreCase("/j leave") || event.getMessage().equalsIgnoreCase("/jumper leave") || event.getMessage().equalsIgnoreCase("/cp")){
				// nothing
			}else{
				event.setCancelled(true);
				event.getPlayer().sendMessage(getConfig().getString("strings.ingame"));
			}
		}
	}
	
	
	public void updateScoreboard(){
		ScoreboardManager manager = Bukkit.getScoreboardManager();
	    
	    
	    for(Player p : Bukkit.getOnlinePlayers()){
	    	Scoreboard board = manager.getNewScoreboard();
	    	
	    	Objective objective = board.registerNewObjective("test", "dummy");
	        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

	        objective.setDisplayName("§l§7SeekCraft!");
	        
	        
	        objective.getScore(Bukkit.getOfflinePlayer("§l§9MONEY:")).setScore(10);
	        objective.getScore(Bukkit.getOfflinePlayer("" + (int)Math.round(econ.getBalance(p.getName())))).setScore(9);
	        objective.getScore(Bukkit.getOfflinePlayer("  ")).setScore(8);
	        objective.getScore(Bukkit.getOfflinePlayer("§l§aONLINE:")).setScore(7);
	        objective.getScore(Bukkit.getOfflinePlayer("" + getServer().getOnlinePlayers().length)).setScore(6);
	        objective.getScore(Bukkit.getOfflinePlayer(" ")).setScore(5);
	        objective.getScore(Bukkit.getOfflinePlayer("§l§eGame Types")).setScore(4);
	        objective.getScore(Bukkit.getOfflinePlayer("§c/survival")).setScore(3);
	        objective.getScore(Bukkit.getOfflinePlayer("§c/creative")).setScore(2);
	        objective.getScore(Bukkit.getOfflinePlayer("§c/minigames")).setScore(1);
	        objective.getScore(Bukkit.getOfflinePlayer("§c/jump")).setScore(0);
	        
	        p.setScoreboard(board);
	    }
	}
	
}
