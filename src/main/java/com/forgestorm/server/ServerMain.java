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
import com.forgestorm.server.io.todo.FileManager;
import com.forgestorm.server.network.NetworkManager;
import com.forgestorm.server.profile.XenforoProfileManager;
import com.forgestorm.server.scripting.ScriptManager;
import com.forgestorm.server.versioning.VersionMain;
import lombok.Getter;

import static com.forgestorm.server.util.Log.println;

@Getter
public class ServerMain {

    private static ServerMain instance = null;

    public static final long SERVER_START_TIME = System.currentTimeMillis();

    // Framework
    private DiscordManager discordManager;
    private GameLoop gameLoop;
    private CommandManager commandManager;
    private DatabaseManager databaseManager;
    private NetworkManager networkManager;

    // Data Loaders
    private FileManager fileManager;
    private FactionManager factionManager;
    private ItemStackManager itemStackManager;
    private DropTableManager dropTableManager;
    private SkillNodeManager skillNodeManager;
    private EntityShopManager entityShopManager;
    private AbilityManager abilityManager;

    // System
    private WorldBuilder worldBuilder;
    private TradeManager tradeManager;
    private GameManager gameManager;
    private CharacterManager characterManager;
    private XenforoProfileManager xenforoProfileManager;
    private ScriptManager scriptManager;

    // Client Files Versioning
    private VersionMain versionMain;

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
        // Framework
        discordManager = new DiscordManager();
        gameLoop = new GameLoop();
        commandManager = new CommandManager();
        databaseManager = new DatabaseManager();
        networkManager = new NetworkManager();

        // Data Loaders
        fileManager = new FileManager();
        factionManager = new FactionManager();
        itemStackManager = new ItemStackManager();
        dropTableManager = new DropTableManager();
        skillNodeManager = new SkillNodeManager();
        entityShopManager = new EntityShopManager();
        abilityManager = new AbilityManager();

        // System
        worldBuilder = new WorldBuilder();
        tradeManager = new TradeManager();
        gameManager = new GameManager();
        characterManager = new CharacterManager();
        xenforoProfileManager = new XenforoProfileManager();
        scriptManager = new ScriptManager();

        /// STARTING....

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

        // Client files updater
        versionMain = new VersionMain();
    }

    public void exitServer() {
        println(getClass(), "Stopping Server!");

        // TODO: Implement ExecutorService to manage threads and shut them down in order.

        getGameManager().exit();
        commandManager.exit();
        networkManager.exit();
        databaseManager.exit();
        fileManager.dispose();

        System.exit(0);
    }
}
