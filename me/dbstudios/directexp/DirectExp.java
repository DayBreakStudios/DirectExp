package me.dbstudios.directexp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		String targetName;
		
		if (e instanceof Player) {
			targetName = ((Player)e).getDisplayName();
		} else if (e instanceof Creature) {
			Creature c = (Creature)e;
			
			if (CreatureType.BLAZE.getEntityClass().isInstance(c)) {
				 targetName = "a blaze";
			} else if (CreatureType.CAVE_SPIDER.getEntityClass().isInstance(c)) {
				targetName = "a cave Spider";
			} else if (CreatureType.CHICKEN.getEntityClass().isInstance(c)) {
				targetName = "a chicken";
			} else if (CreatureType.COW.getEntityClass().isInstance(c)) {
				targetName = "a cow";
			} else if (CreatureType.CREEPER.getEntityClass().isInstance(c)) {
				targetName = "a creeper";
			} else if (CreatureType.ENDER_DRAGON.getEntityClass().isInstance(c)) {
				targetName = "an Ender Dragon";
			} else if (CreatureType.ENDERMAN.getEntityClass().isInstance(c)) {
				targetName = "an enderman";
			} else if (CreatureType.GHAST.getEntityClass().isInstance(c)) {
				targetName = "a Ghast";
			} else if (CreatureType.GIANT.getEntityClass().isInstance(c)) {
				targetName = "a Giant";
			} else if (CreatureType.MAGMA_CUBE.getEntityClass().isInstance(c)) {
				targetName = "a Magma Cube";
			} else if (CreatureType.MUSHROOM_COW.getEntityClass().isInstance(c)) {
				targetName = "a mooshroom";
			} else if (CreatureType.PIG.getEntityClass().isInstance(c)) {
				targetName = "a pig";
			} else if (CreatureType.PIG_ZOMBIE.getEntityClass().isInstance(c)) {
				targetName = "a pig-zombie";
			} else if (CreatureType.SHEEP.getEntityClass().isInstance(c)) {
				targetName = "a sheep";
			} else if (CreatureType.SILVERFISH.getEntityClass().isInstance(c)) {
				targetName = "a silverfish";
			} else if (CreatureType.SKELETON.getEntityClass().isInstance(c)) {
				targetName = "a skeleton";
			} else if (CreatureType.SNOWMAN.getEntityClass().isInstance(c)) {
				targetName = "a snowman";
			} else if (CreatureType.SPIDER.getEntityClass().isInstance(c)) {
				targetName = "a spider";
			} else if (CreatureType.SQUID.getEntityClass().isInstance(c)) {
				targetName = "a squid";
			} else if (CreatureType.WOLF.getEntityClass().isInstance(c)) {
				targetName = "a wolf";
			} else if (CreatureType.ZOMBIE.getEntityClass().isInstance(c)) {
				targetName = "a zombie";
			} else {
				targetName = "your target";
			}
		} else {
			targetName = "your target";
		}
		
		return targetName;
	}
	
	public String parse(String msg, int exp, String killedName) {
		String parsed = msg;
		
		parsed = parsed.replaceAll("(?i)\\{exp\\}", String.valueOf(exp));
		parsed = parsed.replaceAll("(?i)\\{target\\}", killedName);
		
		for (ChatColor c : ChatColor.values())
			parsed = parsed.replaceAll("(?i)\\{" + c.name() + "\\}", c.toString());
		
		return parsed;
	}
	
	public void log(Level level, String msg) {
		logger.log(level, "[DirectExp] " + msg);
	}
}
