package com.forgestorm.server;

import com.forgestorm.server.command.CommandManager;
import com.forgestorm.server.database.DatabaseManager;
import com.forgestorm.server.discord.DiscordManager;
import com.forgestorm.server.game.GameLoop;
import com.forgestorm.server.game.GameManager;
import com.forgestorm.server.game.abilities.AbilityManager;
import com.forgestorm.server.game.character.CharacterManager;
import com.forgestorm.server.game.rpg.EntityShopManager;
import com.forgestorm.server.game.rpg.FactionManager;
import com.forgestorm.server.game.rpg.skills.SkillNodeManager;
import com.forgestorm.server.game.world.item.DropTableManager;
import com.forgestorm.server.game.world.item.ItemStackManager;
import com.forgestorm.server.game.world.item.trade.TradeManager;
import com.forgestorm.server.game.world.maps.building.WorldBuilder;
import com.forgestorm.server.io.ResourcePathLoader;
import com.forgestorm.server.network.NetworkManager;
import com.forgestorm.server.profile.XenforoProfileManager;
import com.forgestorm.server.scripting.ScriptManager;
import lombok.Getter;

import static com.forgestorm.server.util.Log.println;

@Getter
public class ServerMain {

    private static ServerMain instance = null;

    public static final long SERVER_START_TIME = System.currentTimeMillis();

    // Init first
    private static ResourcePathLoader resourcePathLoader;

    static {
//        resourcePathLoader = new ResourcePathLoader();
    }

    // Framework
    private final DiscordManager discordManager = new DiscordManager();
    private final GameLoop gameLoop = new GameLoop();
    private final CommandManager commandManager = new CommandManager();
    private final DatabaseManager databaseManager = new DatabaseManager();
    private final NetworkManager networkManager = new NetworkManager();

    // Data Loaders
    private final FactionManager factionManager = new FactionManager();
    private final ItemStackManager itemStackManager = new ItemStackManager();
    private final DropTableManager dropTableManager = new DropTableManager();
    private final SkillNodeManager skillNodeManager = new SkillNodeManager();
    private final EntityShopManager entityShopManager = new EntityShopManager();
    private final AbilityManager abilityManager = new AbilityManager();

    // System
    private final WorldBuilder worldBuilder = new WorldBuilder();
    private final TradeManager tradeManager = new TradeManager();
    private final GameManager gameManager = new GameManager();
    private final CharacterManager characterManager = new CharacterManager();
    private final XenforoProfileManager xenforoProfileManager = new XenforoProfileManager();
    private final ScriptManager scriptManager = new ScriptManager();

    private ServerMain() {
    }

    public static boolean ideRun = false;

    public static void main(String[] args) {

        for (String arg : args) {
            if (arg.equalsIgnoreCase("ideRun")) ideRun = true;
        }

        ServerMain.getInstance().startServer();
    }

    public static ServerMain getInstance() {
        if (instance == null) instance = new ServerMain();
        return instance;
    }

    private void startServer() {
        discordManager.start();
        println(true);
        println(getClass(), "Starting Server!");

        // Boot io loaders
        worldBuilder.start();
        itemStackManager.start();
        dropTableManager.start();
        skillNodeManager.start();
        abilityManager.start();

        // Start systems
        databaseManager.start();
        gameManager.start();
        commandManager.start();
        networkManager.start();
        gameLoop.start();
    }

    public void exitServer() {
        println(getClass(), "Stopping Server!");

        // TODO: Implement ExecutorService to manage threads and shut them down in order.

        getGameManager().exit();
        commandManager.exit();
        networkManager.exit();
        databaseManager.exit();

        System.exit(0);
    }
}
