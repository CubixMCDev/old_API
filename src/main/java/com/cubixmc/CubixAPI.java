package com.cubixmc;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.cubixmc.commands.CommandCoins;
import com.cubixmc.commands.CommandCredits;
import com.cubixmc.commands.CommandRank;
import com.cubixmc.managers.*;
import com.cubixmc.encapsulation.Get;
import com.cubixmc.encapsulation.Set;
import com.cubixmc.events.EventsListener;
import com.cubixmc.ranks.Rank;
import com.cubixmc.data.sanctions.BanPlayerData;
import com.cubixmc.data.sanctions.MutePlayerData;
import com.cubixmc.data.User;
import com.cubixmc.sql.MysqlManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;

@Getter
@Setter
public class CubixAPI extends JavaPlugin implements Listener {

	private static CubixAPI instance;
	public static CubixAPI getInstance() {
		return instance;
	}

	public String prefix = ChatColor.YELLOW+"CubixMC "+ChatColor.GOLD+"» ";
	public String prefixError = ChatColor.RED+"CubixMC "+ChatColor.DARK_RED+"» ";

	private Map<Player, User> dataPlayers = new HashMap<>();
	private Map<UUID, BanPlayerData> banned = new HashMap<>();
	private Map<UUID, MutePlayerData> muted = new HashMap<>();

	private final Map<String, Rank> idToRank = new HashMap<>();
	private Rank defaultRank;

	private Get get;
	private Set set;
	private UserManager userManager;
	private BanManager banManager;
	private MuteManager muteManager;
	private ModManager modManager;
	private FriendsManager friendsManager;
	private PartyManager partyManager;
	private MysqlManager databaseManager;

	private boolean teamTagOn = true;
	
	private Scoreboard s;
	
	@Override
	public void onEnable() {
		instance = this;
		get = new Get(this);
		set = new Set(this);

		loadConfig();
		s = Bukkit.getScoreboardManager().getMainScoreboard();
		databaseManager = new MysqlManager(this);
		userManager = new UserManager(this);
		banManager = new BanManager(this);
		muteManager = new MuteManager(this);
		modManager = new ModManager(this);
		friendsManager = new FriendsManager(this);
		partyManager = new PartyManager(this);

		loadRanks();
		registerCommands();

		Bukkit.getPluginManager().registerEvents(new EventsListener(this), this);

		banManager.loadBannedPlayers();
		muteManager.loadMutedPlayers();
	}

	private void registerCommands() {
		getCommand("coins").setExecutor(new CommandCoins(this));
		getCommand("credits").setExecutor(new CommandCredits(this));
		getCommand("ban").setExecutor(banManager);
		getCommand("unban").setExecutor(banManager);
		getCommand("tempban").setExecutor(banManager);
		getCommand("mute").setExecutor(muteManager);
		getCommand("tempmute").setExecutor(muteManager);
		getCommand("unmute").setExecutor(muteManager);
		getCommand("rank").setExecutor(new CommandRank(this));
	}

	private void loadRanks() {
		for (String s : getConfig().getConfigurationSection("ranks").getKeys(false)) {
			Rank rank = new Rank(getConfig().getString("ranks." + s + ".id"),
					ChatColor.valueOf(getConfig().getString("ranks." + s + ".colorChat")),
					getConfig().getStringList("ranks." + s + ".perms"));
			if (getConfig().isSet("ranks." + s + ".default")) {
				this.defaultRank = rank;
			}
			if (getConfig().isSet("ranks." + s + ".inherit")) {
				rank.setInherits(getConfig().getStringList("ranks." + s + ".inherit"));
			}
			
			idToRank.put(getConfig().getString("ranks." + s + ".id").toLowerCase(), rank);
		}
		if (defaultRank == null) {
			System.out.println("Aucun rank par default n'est définit !");
			Bukkit.getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		this.databaseManager.close();
	}

	private void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public Get get() {
		return get;
	}

	public Set set(){
		return set;
	}
}
