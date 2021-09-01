package com.forgestorm.server.util;

import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.maps.GameWorld;

import java.util.*;

public class PathFinding {

    private final List<MoveNode> closedSet = new ArrayList<>();
    private final List<MoveNode> openSet = new ArrayList<>();

    private final int ALGORITHM_RADIUS = GameConstants.QUIT_ATTACK_RADIUS;
    private final int GRID_LENGTH = (ALGORITHM_RADIUS * 2) + 1;

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

    private void initializeGrid(GameWorld gameWorld, int startX, int startY, short worldZ) {

        int bottomX = startX - ALGORITHM_RADIUS;
        int bottomY = startY - ALGORITHM_RADIUS;

        for (int i = 0; i < GRID_LENGTH; i++) {
            for (int j = 0; j < GRID_LENGTH; j++) {
                int worldX = bottomX + i;
                int worldY = bottomY + j;

                boolean isTraversable = gameWorld.isTraversable(worldX, worldY, worldZ);

                if (isTraversable) {
                    grid[i][j] = new MoveNode(worldX, worldY, i, j);
                } else {
                    grid[i][j] = null;
                }

            }
        }

        for (int i = 0; i < GRID_LENGTH; i++) {
            for (int j = 0; j < GRID_LENGTH; j++) {
                if (grid[i][j] != null) {
                    grid[i][j].setMapName(gameWorld.getWorldName());
                    grid[i][j].addNeighbors(GRID_LENGTH, grid);
                }
            }
        }
    }

    private boolean initialConditions(GameWorld gameWorld, int startX, int startY, int finalX, int finalY, short worldZ) {
        if (startX == finalX && startY == finalY) return false;

        if (!gameWorld.isTraversable(startX, startY, worldZ)) return false;
        if (!gameWorld.isTraversable(finalX, finalY, worldZ)) return false;

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

    public Queue<MoveNode> findPath(GameWorld gameWorld, int startX, int startY, int finalX, int finalY, short worldZ) {
        if (!initialConditions(gameWorld, startX, startY, finalX, finalY, worldZ)) return null;

        initializeGrid(gameWorld, startX, startY, worldZ);

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