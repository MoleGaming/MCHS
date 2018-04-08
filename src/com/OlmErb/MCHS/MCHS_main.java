package com.OlmErb.MCHS;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

public class MCHS_main extends JavaPlugin implements Listener{
	
	private final int LOC_AMNT = 4;
	
	public boolean inGame = false;
	private Logger logger;
	public Location lobby;
	public Location waitingRoom;
	public Location hiderSpawn;
	public Location seekerSpawn;
	public World world;
	private ArrayList<Player> players;
	public int seekTime;
	public int hideTime;
	public int gameDuration;
	public String currentEvent;
	private ScoreboardManager mg;
	private Scoreboard sc;
	private Team Seekers;
	private Team Hiders;
	private boolean blockDebug=true;
	
	public void onEnable(){
		PluginDescriptionFile pdfFile = getDescription();
		logger = Bukkit.getLogger();
		getServer().getPluginManager().registerEvents(this, this);
		if(init()){
			logger.log(Level.INFO, "Started "+pdfFile.getName()+" (V "+pdfFile.getVersion()+") by "+pdfFile.getAuthors());
		}else{
			logger.log(Level.SEVERE, "ERROR: Failed to start "+pdfFile.getName()+" (V "+pdfFile.getVersion()+") by "+pdfFile.getAuthors());
		}
	}
	public void onDisable(){
		PluginDescriptionFile pdfFile = getDescription();
		logger = Bukkit.getLogger();
		
		if(disable()){
		logger.log(Level.INFO, pdfFile.getName()+" (V "+pdfFile.getVersion()+") disabled properly.");
		}else{
			logger.log(Level.SEVERE, "ERROR: "+pdfFile.getName()+" (V "+pdfFile.getVersion()+") failed to disable properly.");
		}
	}
	public void enableCommands(){
		this.getCommand("setHSPos").setExecutor(new MCHS_main());
		this.getCommand("setHStime").setExecutor(new MCHS_main());
		this.getCommand("starths").setExecutor(new MCHS_main());
		this.getCommand("disablehs").setExecutor(new MCHS_main());
	}
	public boolean init(){
		mg = Bukkit.getScoreboardManager();
		sc = mg.getNewScoreboard();
		if(sc.getEntries().contains("Seekers"))
			Seekers = sc.registerNewTeam("Seekers");
		if(sc.getEntries().contains("Hiders"))
			Hiders = sc.registerNewTeam("Hiders");
		inGame=false;
		File data = new File("dat");
		
		Seekers.setColor(ChatColor.RED);
		Hiders.setColor(ChatColor.BLUE);
		
		Seekers.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
		Hiders.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
		
		try {
			if(data.createNewFile()){
				logger.log(Level.CONFIG, "Created data file");
				world=Bukkit.getWorlds().get(0);
				lobby = new Location(world, 0, 0, 0);
				waitingRoom = new Location(world, 0, 0, 0);
				hiderSpawn = new Location(world, 0, 0, 0);
				seekerSpawn = new Location(world, 0, 0, 0);
				hideTime=0;
				seekTime=0;
			}else{
				logger.log(Level.CONFIG, "Loaded data file");
				BufferedReader reader = new BufferedReader(new FileReader(data));
				world = Bukkit.getWorld(reader.readLine());
				Location[] locs = new Location[LOC_AMNT];
				for(int i=0;i<LOC_AMNT;i++){
					locs[i]=new Location(world, Double.parseDouble(reader.readLine()),Double.parseDouble(reader.readLine()),Double.parseDouble(reader.readLine()));
				}
				hideTime=Integer.parseInt(reader.readLine());
				seekTime=Integer.parseInt(reader.readLine());
				reader.close();
				lobby = locs[0];
				waitingRoom = locs[1];
				hiderSpawn = locs[2];
				seekerSpawn = locs[3];
			}
		} catch (Exception e) {
			logger.log(Level.CONFIG, "Created data file");
			world=Bukkit.getWorlds().get(0);
			lobby = new Location(world, 0, 0, 0);
			waitingRoom = new Location(world, 0, 0, 0);
			hiderSpawn = new Location(world, 0, 0, 0);
			seekerSpawn = new Location(world, 0, 0, 0);
			hideTime=0;
			seekTime=0;
			return false;
		}
		return true;
		
	}	
	public boolean disable(){
		
		File data = new File("dat");
		
		try{
			
			data.createNewFile();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(data));
			
			writer.write(world.getName());
			writer.newLine();
			writer.write(""+lobby.getX());
			writer.newLine();
			writer.write(""+lobby.getY());
			writer.newLine();
			writer.write(""+lobby.getZ());
			writer.newLine();
			writer.write(""+waitingRoom.getX());
			writer.newLine();
			writer.write(""+waitingRoom.getY());
			writer.newLine();
			writer.write(""+waitingRoom.getZ());
			writer.newLine();
			writer.write(""+hiderSpawn.getX());
			writer.newLine();
			writer.write(""+hiderSpawn.getY());
			writer.newLine();
			writer.write(""+hiderSpawn.getZ());
			writer.newLine();
			writer.write(""+seekerSpawn.getX());
			writer.newLine();
			writer.write(""+seekerSpawn.getY());
			writer.newLine();
			writer.write(""+seekerSpawn.getZ());
			writer.newLine();
			writer.write(""+hideTime);
			writer.newLine();
			writer.write(""+seekTime);
			
			writer.close();
			
		}catch(Exception e){
			return false;
		}
		return true;
	}

	public boolean onCommand(CommandSender s, Command c, String label, String[] args){
		String cName = c.getName();
		if(c.getName().equalsIgnoreCase("sethspos")&& s instanceof Player){
			if(inGame){
				s.sendMessage("You can't change positions during the game!");
				return false;
			}
			if(!(args.length>0)){
				s.sendMessage("Please select a location to set");
				return false;
			}
			players = new ArrayList<Player>(Bukkit.getOnlinePlayers());
			Player p = getPlayer(players, s.getName());
			Location pLoc = p.getLocation();
			int pX = ((int) pLoc.getBlockX());
			int pY = ((int) pLoc.getBlockY());
			int pZ = ((int) pLoc.getBlockZ());
			Location newLoc = new Location(p.getWorld(), pX, pY, pZ);
			switch(args[0].toLowerCase()){
				case "lobby":
					s.sendMessage("lobby");
					lobby=newLoc;
				break;
				case "wait":
					waitingRoom=newLoc;
				break;
				case "seeker":
					seekerSpawn=newLoc;
				break;
				case "hider":
					hiderSpawn=newLoc;
				break;
				default:
					s.sendMessage("Invalid argument \""+args[0]+"\"");
					return false;
			}
			s.sendMessage("set position \""+args[0]+"\" to "+pX+", "+pY+", "+pZ);
		}else
		if(cName.equals("sethstime")){
			try{
				switch(args[0]){
					case "hide":
						hideTime=Integer.parseInt(args[1]);
					break;
					case "seek":
						seekTime=Integer.parseInt(args[1]);
					break;
					default:
						s.sendMessage("You must select \"hide\" or \"seek\" for the time!");
						return false;
				}
			}catch(NumberFormatException e){
				s.sendMessage("Time must be an integer!");
				return false;
			}
			s.sendMessage("set \""+args[0]+"\" time to "+args[1]+" seconds.");
		}else
		if(c.getName().equalsIgnoreCase("starths")){
			inGame=true;
			currentEvent="hide";
			players = new ArrayList<Player>(Bukkit.getOnlinePlayers());
			int r = (int)(Math.random()*players.size());
			for(int i=0;i<players.size();i++){
				Player p = players.get(i);
				if(r==i){
					Seekers.addEntry(p.getName());
				}else{
					Hiders.addEntry(p.getName());
				}
			}
			gameTimer(hideTime);
		}else
		if(c.getName().equalsIgnoreCase("disablehs")){
			Bukkit.getPluginManager().disablePlugin(this);
		}
		return true;
	}
	
	private Player getPlayer(Collection<? extends Player> players, String name){
		
		for(Player p:players){
			if(p.getName().equals(name))
				return p;
		}
		
		return null;
	}

	public void gameTimer(int duration){
		gameDuration=duration;
		new BukkitRunnable(){

			@Override
			public void run() {
				if(gameDuration==0){
					currentEvent=doGameStuff(currentEvent);
					this.cancel();
					return;
				}
				if(gameDuration<10)
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "execute @a ~ ~ ~ /playsound minecraft:block.lever.click master @p");
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title @a actionbar [{\"text\":\""+gameDuration--+"\",\"color\":\"red\"}]");
				
			}
			
		}.runTaskTimer(this, 0, 20);
	}
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent ev){
		if(inGame&&currentEvent.equals("seek"))
		for(Entity e: ev.getPlayer().getNearbyEntities(2, 2, 2)){
			if(e instanceof Player&&Hiders.hasEntry(e.getName())){
				Player p = getPlayer(Bukkit.getOnlinePlayers(), e.getName());
				if(Seekers.hasEntry(p.getName())){
					ev.getPlayer().sendMessage(ChatColor.RED+"You have been caught!");
					Hiders.removeEntry(ev.getPlayer().getName());
					Seekers.addEntry(ev.getPlayer().getName());
				}
			}
		}
	}
	@EventHandler
	public void onVineGrow(BlockSpreadEvent ev){
		ev.setCancelled(true);
	}
	
	public String doGameStuff(String event){
		if(event.equals("hide")){
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title @a actionbar [{\"text\":\"THE SEEKER HAS BEEN RELEASED!\",\"color\":\"dark_red\",\"bold\":true}]");
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "execute @a ~ ~ ~ /playsound minecraft:entity.wither.spawn master @p");
			return "seek";
		}else
		if(event.equals("seek")){
			
		}
		return null;
	}
	
}
