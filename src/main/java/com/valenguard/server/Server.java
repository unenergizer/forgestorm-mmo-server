package com.valenguard.server;

import com.valenguard.server.command.CommandManager;
import com.valenguard.server.database.DatabaseManager;
import com.valenguard.server.game.GameLoop;
import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.abilities.AbilityManager;
import com.valenguard.server.game.character.CharacterManager;
import com.valenguard.server.game.rpg.EntityShopManager;
import com.valenguard.server.game.rpg.FactionManager;
import com.valenguard.server.game.rpg.skills.SkillNodeManager;
import com.valenguard.server.game.world.item.DropTableManager;
import com.valenguard.server.game.world.item.ItemStackManager;
import com.valenguard.server.game.world.item.trade.TradeManager;
import com.valenguard.server.network.NetworkManager;
import lombok.Getter;
import org.fusesource.jansi.AnsiConsole;

import static com.valenguard.server.util.Log.println;
import static org.fusesource.jansi.Ansi.ansi;

@Getter
public class Server {

    private static Server instance = null;

    public static final long SERVER_START_TIME = System.currentTimeMillis();

    // Framework
    private final GameLoop gameLoop = new GameLoop();
    private final CommandManager commandManager = new CommandManager();
    private final DatabaseManager databaseManager = new DatabaseManager();
    private final NetworkManager networkManager = new NetworkManager();

    // System
    private final TradeManager tradeManager = new TradeManager();
    private final GameManager gameManager = new GameManager();
    private final CharacterManager characterManager = new CharacterManager();

    // Data Loaders
    private final FactionManager factionManager = new FactionManager();
    private final ItemStackManager itemStackManager = new ItemStackManager();
    private final DropTableManager dropTableManager = new DropTableManager();
    private final SkillNodeManager skillNodeManager = new SkillNodeManager();
    private final EntityShopManager entityShopManager = new EntityShopManager();
    private final AbilityManager abilityManager = new AbilityManager();

    private Server() {
    }

    public static void main(String[] args) {
        Server.getInstance().startServer();
    }

    public static Server getInstance() {
        if (instance == null) instance = new Server();
        return instance;
    }

    private void startServer() {
        AnsiConsole.systemInstall();
        println(getClass(), ansi().fgBlue().bgBrightYellow() + "Starting Server!");

        // Boot io loaders
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

        AnsiConsole.systemUninstall();
        System.exit(0);
    }
}
