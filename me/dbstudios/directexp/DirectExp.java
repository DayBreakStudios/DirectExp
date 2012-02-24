package me.dbstudios.directexp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DirectExp extends JavaPlugin {
	private enum Directories {
		Base("plugins::dbstudios::DirectExp::"),
		Config(Base + "config::");
		
		String path;
		
		Directories(String path) {
			this.path = path.replace("::", File.separator);
		}
		
		public String toString() {
			return path;
		}
	}
	
	private Hashtable<CreatureType, Integer> drops = new Hashtable<CreatureType, Integer>();
	private Logger logger;
	
	public void onEnable() {
		long startTime = System.currentTimeMillis();
		
		logger = this.getServer().getLogger();
		
		this.extract("config.yml", Directories.Config.toString());
		this.loadExpDrops();
		this.getServer().getPluginManager().registerEvents(new DEEntityListener(this), this);
		
		long bootTime = System.currentTimeMillis() - startTime;
		
		this.log(Level.INFO, "DirectExp (v" + this.getDescription().getVersion() + ") loaded in " + bootTime + " milliseconds.");
	}
	
	public void onDisable() {
		this.log(Level.INFO, "DirectExp disabled.");
	}
	
	public int getDrop(Entity e, int def) {
		CreatureType ctype = this.getCreatureType(e);
		
		if (ctype != null && drops.containsKey(ctype) && drops.get(ctype) != -1)
			return drops.get(ctype);
		
		return def;
	}
	
	public CreatureType getCreatureType(Entity e) {
		for (CreatureType t : CreatureType.values())
			if (t.getEntityClass().isInstance(e)) return t;
		
		return null;
	}
	
	public void extract(String file, String destination) {
		File f = new File(destination);
		
		if (!f.exists())
			f.mkdirs();
		
		f = new File(destination + file);
		
		if (f.exists())
			return;
		
		try {
			f.createNewFile();
			
			InputStream in = this.getClass().getResourceAsStream("/resources/" + file);
			OutputStream out = new FileOutputStream(f);
			
			int b;
			while ((b = in.read()) != -1)
				out.write(b);
			
			in.close();
			out.close();
			
			this.log(Level.INFO, "Extracted " + file + " to " + destination + file + ".");
		} catch (Exception e) {
			this.log(Level.WARNING, "Could not extract " + file + " to " + destination + file + ".");
		}
	}
	
	public FileConfiguration getMainConfig() {
		return YamlConfiguration.loadConfiguration(new File(Directories.Config + "config.yml"));
	}
	
	public void loadExpDrops() {
		FileConfiguration config = this.getMainConfig();
		ConfigurationSection section = config.getConfigurationSection("config");
		drops = new Hashtable<CreatureType, Integer>();
		
		if (section != null)
			for (String key : section.getKeys(false))
				for (CreatureType t : CreatureType.values())
					if (t.name().equalsIgnoreCase(key)) drops.put(t, config.getInt("config." + key, -1));
	}
	
	public String getTargetName(Entity e) {
		String targetName = null;
		
		if (e instanceof Player) {
			targetName = ((Player)e).getDisplayName();
		} else if (e instanceof Creature) {
			Creature c = (Creature)e;
			
			for (CreatureType type : CreatureType.values()) {
				if (type.getEntityClass().isInstance(c)) {
					if (Pattern.compile("(?i)^[aeiou].*?").matcher(type.name()).find()) {	
						targetName = "an ";
					} else {
						targetName = "a ";	
					}
					
					targetName += type.name().toLowerCase();
				}
			}
		}
		
		if (targetName == null)
			targetName = "your target";
		
		return targetName;
	}
	
	public String parse(String msg, int exp, int dmg, String killedName) {
		String parsed = msg;
		
		parsed = parsed.replaceAll("(?i)\\{exp\\}", String.valueOf(exp));
		parsed = parsed.replaceAll("(?i)\\{target\\}", killedName);
		parsed = parsed.replaceAll("(?i)\\{dmg\\}", String.valueOf(dmg));
		
		for (ChatColor c : ChatColor.values())
			parsed = parsed.replaceAll("(?i)\\{" + c.name() + "\\}", c.toString());
		
		return parsed;
	}
	
	public void log(Level level, String msg) {
		logger.log(level, "[DirectExp] " + msg);
	}
}
