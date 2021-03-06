package game;
import ecs100.*;

import java.util.Arrays;

/**
 * Created by Dylan on 29/04/16.
 *
 * Accesses the GWorld object to perform logic
 *
 * This is a static class
 */
public class GLogic {

    private static GLogic currentInstance;

    public static boolean shouldQuit() {
        return shouldQuit;
    }
    public static void setShouldQuit(boolean shouldQuit) {
        GLogic.shouldQuit = shouldQuit;
    }
    public static boolean shouldRestart() {
        return shouldRestart;
    }
    public static void setShouldRestart(boolean shouldRestart) {
        GLogic.shouldRestart = shouldRestart;
    }

    private static boolean shouldQuit;
    private static boolean shouldRestart;

    public static boolean hasWon() {
        return hasWon;
    }

    private static boolean hasWon;

    // Movement functionality
    private static boolean moveUp = false;
    private static boolean moveDown = false;
    private static boolean moveLeft = false;
    private static boolean moveRight = false;



    public static void setMoveUp() {
        if(moveDown == false && moveLeft == false && moveRight == false) {
            moveUp = true;
//            UI.println("Moving up");
//            System.out.println("moved up");
       }
    }
    public static void setMoveDown() {
        if(moveUp == false && moveLeft == false && moveRight == false) {
            moveDown = true;
//            UI.println("Moving down");
        }
    }
    public static void setMoveLeft() {
        if(moveRight == false && moveUp == false && moveDown == false) {
            moveLeft = true;
//            UI.println("Moving left");
        }
    }
    public static void setMoveRight() {
        if(moveLeft == false && moveUp == false && moveDown == false) {
            moveRight = true;
//            UI.println("Moving right");
        }
    }

    private GWorld world;

    private int dRow;
    private int dCol;

    private GLogic(GWorld world) {
        this.world = world;
        shouldQuit = false;
        shouldRestart = false;
        hasWon = false;
    }

    public static GLogic getGLogic(GWorld world) {
        if(currentInstance != null) {
            return currentInstance;
        } else {
            return new GLogic(world);
        }
    }

    public static void makeReferenceNull() {
        currentInstance = null;
    }

    public void update() {
        int width = world.getWidth();
        int height = world.getHeight();
        boolean[][] updated = new boolean[height][width];

        checkMovements();

        GSquare currentSquare;
        GSquare neighbourSquare;

        if(dRow == 0 && dCol == 0) {
            return; // no change
        }
        GSquare[][] powerupMatrix = new GSquare[25][25];
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                GSquare checkForPowerups = world.getCell(i, j);
                if(Arrays.asList(GSquare.POWERUP_TYPES).contains(checkForPowerups.getType())) {
                    powerupMatrix[i][j] = checkForPowerups;
                }
            }
        }

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                currentSquare = world.getCell(i, j);
                neighbourSquare = world.getCell(i + dRow, j + dCol);

                String decision = getMoveDecision(currentSquare, neighbourSquare);
//                if(!decision.equals("nothing")){
//                    UI.println("Single movement");
//                    UI.println(decision);
//                    UI.println(currentSquare);
//                    UI.println(currentSquare.getType());
//                    UI.println(neighbourSquare);
//                    UI.println(neighbourSquare.getType());
//                    UI.println("\n");
//                }
                switch(decision) {
                    case "defer":
                        continue;
                    case "move":
//                        UI.println("Moving in this direction");
                        world.move(i, j, dRow, dCol);
                        world.setCell(new GCell(GSquare.USER_PATH, GSquare.USER_MOVED_TYPE), i + dRow, j + dCol);
                        break;
                    case "stay":
                        break;
                    case "nothing":
                        break;
                    default:
                        break;
                }
                updated[i][j] = true;
            }
        }

        for(int r = 0; r < height; r++) {
            for(int c = 0; c < width; c++) {
                if (powerupMatrix[r][c] == null) continue;
                if(world.getCell(r, c).getType().equals(GSquare.USER_MOVED_TYPE)) {
                    switch(powerupMatrix[r][c].getType()) {
                        case GSquare.POWERUP_GROW_TYPE:
                            world.grow(r, c);
                            break;
                        case GSquare.POWERUP_KILL_TYPE:
                            world.kill(r, c);
                            break;
                        case GSquare.POWERUP_TELEPORT_RIGHT_TYPE:
                            world.setCell(new GCell(GCell.EMPTY_PATH, GCell.EMPTY_TYPE), r, c);
                            world.setCell(new GCell(GCell.USER_PATH, GCell.USER_TYPE), r, c + 5);
                            break;
                        case GSquare.POWERUP_TELEPORT_LEFT_TYPE:
                            world.setCell(new GCell(GCell.EMPTY_PATH, GCell.EMPTY_TYPE), r, c);
                            world.setCell(new GCell(GCell.USER_PATH, GCell.USER_TYPE), r, c - 5);
                            break;
                        case GSquare.POWERUP_TELEPORT_UP_TYPE:
                            world.setCell(new GCell(GCell.EMPTY_PATH, GCell.EMPTY_TYPE), r, c);
                            world.setCell(new GCell(GCell.USER_PATH, GCell.USER_TYPE), r - 5, c);
                            break;
                        case GSquare.POWERUP_TELEPORT_DOWN_TYPE:
                            world.setCell(new GCell(GCell.EMPTY_PATH, GCell.EMPTY_TYPE), r, c);
                            world.setCell(new GCell(GCell.USER_PATH, GCell.USER_TYPE), r + 5, c);
                            break;
                    }
                }
            }
        }

        cleanUpAfterUpdate(updated);

        cleanMovement();
        world.resetCantMoveField();

        if (hasUserWon()) {
            hasWon = true;
        }
    }


    private void cleanMovement() {
        dRow = 0;
        dCol = 0;
    }

    private void checkMovements() {
        if(moveDown == true) {
            dRow = 1;
        } else if(moveUp == true) {
            dRow = -1;
        } else {
            dRow = 0;
        }

        if(moveRight == true) {
            dCol = 1;
        } else if(moveLeft == true) {
            dCol = -1;
        } else {
            dCol = 0;
        }

        moveDown = false;
        moveUp = false;
        moveLeft = false;
        moveRight = false;
    }

    private String getMoveDecision(GSquare thisSquare, GSquare nextSquare) {
        if(thisSquare.getType().equals(GSquare.USER_TYPE)) {
             if(nextSquare.getType().equals(GSquare.EMPTY_TYPE)) {
                 return "move";
             } else if(nextSquare.getType().equals(GSquare.USER_TYPE)) {
                 return "defer";
             } else if(nextSquare.getType().equals(GSquare.WALL_TYPE)) {
                 return "stay";
             }
        } else {
            return "nothing";
        }

        return "move";
    }

    private void cleanUpAfterUpdate(boolean[][] updated) {
        GSquare currentSquare;
        GSquare nextSquare;
        String decision;

        for(int i = 24; i > 0; i--) {
            for(int j = 24; j > 0; j--) {
                if(updated[i][j] == false) {
                    currentSquare = world.getCell(i, j);
                    nextSquare = world.getCell(i + dRow, j + dCol);

                    decision = getMoveDecision(currentSquare, nextSquare);
                    switch(decision) {
                        case "move":
                            world.move(i, j, dRow, dCol);
                        default:
                            break;
                    }
                }
            }
        }

        for(int i = 0; i < 25; i++) {
            for(int j = 0; j < 25; j++) {
                if(world.getCell(i, j).getType().equals(GSquare.USER_MOVED_TYPE)) {
                    world.setCell(new GCell(GSquare.USER_PATH, GSquare.USER_TYPE), i, j);
                }
            }
        }
    }

    private boolean hasUserWon() {
        return world.getGoal().matchesUserState(getUserTileState());
    }

    /**
     * The true values are where the user has a tile there
     * @return This result is compared with GGoal to check if the user fits the goal
     */
    private boolean[][] getUserTileState() {
        boolean[][] state = new boolean[world.getHeight()][world.getWidth()];
        for (int col = 0; col < world.getWidth(); col++) {
            for (int row = 0; row < world.getHeight(); row++) {
                GCell cell = world.getCell(row, col);
                if (cell.getType().equals(GSquare.USER_TYPE)) {
                    state[row][col] = true;
                }
            }
        }
        return state;
    }
}
