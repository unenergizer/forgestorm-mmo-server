package com.valenguard.server.util;

import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.Tile;

import java.util.*;

public class PathFinding {

    private static final boolean PRINT_DEBUG = false;

    private final List<MoveNode> closedSet = new ArrayList<>();
    private final List<MoveNode> openSet = new ArrayList<>();

    private final short ALGORITHM_RADIUS = GameConstants.QUIT_ATTACK_RADIUS;
    private final short GRID_LENGTH = (ALGORITHM_RADIUS * 2) + 1;

    private final MoveNode[][] grid = new MoveNode[GRID_LENGTH][GRID_LENGTH];

    private int calculateHeuristic(int ax, int ay, int bx, int by) {
        return Math.abs(bx - ax) + Math.abs(by - ay);
    }

    private MoveNode getCurrentNode() {
        MoveNode current = openSet.get(0);
        for (MoveNode openNode : openSet)
            if (current.getCostF() > openNode.getCostF()) current = openNode;
        return current;
    }

    private void initializeGrid(GameMap gameMap, short startX, short startY) {

        short bottomX = (short) (startX - ALGORITHM_RADIUS);
        short bottomY = (short) (startY - ALGORITHM_RADIUS);

        for (short i = 0; i < GRID_LENGTH; i++) {
            for (short j = 0; j < GRID_LENGTH; j++) {
                short worldX = (short) (bottomX + i);
                short worldY = (short) (bottomY + j);

                Tile worldTile = null;
                if (!gameMap.isOutOfBounds(worldX, worldY)) {
                    worldTile = gameMap.getTileByLocation(new Location(gameMap.getMapName(), worldX, worldY));
                }

                if (worldTile == null) {
                    grid[i][j] = null;
                } else if (!worldTile.isTraversable()) {
                    grid[i][j] = null;
                } else {
                    grid[i][j] = new MoveNode(worldX, worldY, i, j);
                }
            }
        }

        for (short i = 0; i < GRID_LENGTH; i++) {
            for (short j = 0; j < GRID_LENGTH; j++) {
                if (grid[i][j] != null) {
                    grid[i][j].setMapName(gameMap.getMapName());
                    grid[i][j].addNeighbors(GRID_LENGTH, grid);
                }
            }
        }
    }

    private boolean initialConditions(GameMap gameMap, short startX, short startY, short finalX, short finalY) {
        if (startX == finalX && startY == finalY) return false;

        if (!gameMap.isTraversable(new Location(gameMap.getMapName(), startX, startY))) return false;
        if (!gameMap.isTraversable(new Location(gameMap.getMapName(), finalX, finalY))) return false;

        return Math.abs(finalX - startX) <= ALGORITHM_RADIUS && Math.abs(finalY - startY) <= ALGORITHM_RADIUS;
    }

    private void evaluateNeighbors(MoveNode current, MoveNode goalNode) {
        for (MoveNode neighbor : current.getNeighbors()) {
            if (neighbor == null) continue;

            if (!closedSet.contains(neighbor)) {
                int attemptG = neighbor.getCostG() + 1;

                if (openSet.contains(neighbor)) {
                    if (attemptG < neighbor.getCostG()) neighbor.setCostG(attemptG);
                } else {
                    neighbor.setCostG(attemptG);
                    openSet.add(neighbor);
                }

                neighbor.setHeuristic(calculateHeuristic(neighbor.getI(), neighbor.getJ(), goalNode.getI(), goalNode.getJ()));
                neighbor.setCostF(neighbor.getHeuristic() + neighbor.getCostG());
                neighbor.setParentNode(current);
            }
        }
    }

    public Queue<MoveNode> findPath(GameMap gameMap, short startX, short startY, short finalX, short finalY) {
        if (!initialConditions(gameMap, startX, startY, finalX, finalY)) return null;

        initializeGrid(gameMap, startX, startY);

        // Start node
        openSet.add(grid[ALGORITHM_RADIUS][ALGORITHM_RADIUS]);

        MoveNode goalNode = grid[ALGORITHM_RADIUS + finalX - startX][ALGORITHM_RADIUS + finalY - startY];

        while (!openSet.isEmpty()) {
            MoveNode current = getCurrentNode();

            if (current.equals(goalNode)) {

                List<MoveNode> pathFound = new LinkedList<>();
                MoveNode iterateNode = current;

                pathFound.add(iterateNode);
                while (iterateNode.getParentNode() != null) {
                    pathFound.add(iterateNode.getParentNode());
                    iterateNode = iterateNode.getParentNode();
                }

                finish();
                Collections.reverse(pathFound);
                @SuppressWarnings("unchecked") Queue<MoveNode> queuePath = (Queue<MoveNode>) pathFound;
                queuePath.remove(); // Removing the node the player is standing on.
                return queuePath;
            }

            openSet.remove(current);
            closedSet.add(current);

            evaluateNeighbors(current, goalNode);
        }

        finish();
        return null;
    }

    private void finish() {
        closedSet.clear();
        openSet.clear();
    }
}