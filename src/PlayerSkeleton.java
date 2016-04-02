

public class PlayerSkeleton {
    private static final boolean SLOW_MODE = false; // set this to false to play fast
    private static final int SLEEP_TIME = 1000;
    private static final int END_GAME = -1000000;
    private static final int COLS = 10;
    private static final int ROWS = 21;
    private static final int N_PIECES = 7;

    private static double sumHeightCoefficient;
    private static double completeLinesCoefficient;
    private static double holesEfficient;
    private static double heightDiffCoefficient;
    private static double bricksOnHolesCoefficient;
    
    //implement this function to have a working system
    public int pickMove(State s, int[][] legalMoves) {
        double max = heur(s, legalMoves[0]);
        int bestMove = 0;
        
        for (int i = 1; i < legalMoves.length; i++){    
            double heuristics = heur(s, legalMoves[i]);
            if (heuristics > max){
                max = heuristics;
                bestMove = i;
            }
        }
        //System.out.println(max);
        //System.out.println(bestMove);
        return bestMove;
    }
    
    public PlayerSkeleton(double completeLinesCoefficientParam, double sumHeightCoefficientParam, 
                          double holesEfficientParam, double heightDiffCoefficientParam, 
                          double bricksOnHolesCoefficientParam) {
        completeLinesCoefficient = completeLinesCoefficientParam;
        sumHeightCoefficient = sumHeightCoefficientParam;
        holesEfficient = holesEfficientParam;
        heightDiffCoefficient = heightDiffCoefficientParam;
        bricksOnHolesCoefficient = bricksOnHolesCoefficientParam;
    }
    
    public PlayerSkeleton() {
        // Default Values 422, 955, 48, 246, 48
        // Average Score: ~1000
        // Maximum Score: 3465
        completeLinesCoefficient = 1000422;
        sumHeightCoefficient = 955;
        holesEfficient = 48;
        heightDiffCoefficient = 246;
        bricksOnHolesCoefficient = 48;
    }
    
    public static void main(String[] args) {
        State s = new State();
        new TFrame(s);
        PlayerSkeleton p = new PlayerSkeleton();
        while(!s.hasLost()) {
            s.makeMove(p.pickMove(s,s.legalMoves()));
            s.draw();
            s.drawNext(0,0);
            try {
                if (SLOW_MODE) Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //System.out.println(s.getRowsCleared());
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
        
    }

    /*heuristics function taken from:
     * https://codemyroad.wordpress.com/2013/04/14/tetris-ai-the-near-perfect-player/
     */
    public static double heur(State curState, int[] move){
        //update field
        int[][] field = tryPuttingPiece(curState, move, curState.getNextPiece());
        if (field == null) return END_GAME;    // end game
        
        //calculate heuristics values
        int sumHeight = getSumHeight(field);
        int heightDiff = getHeightDiff(field);
        int completeLines = getCompleteLines(field);
        int holes = getHoles(field);
        int bricksOnHoles = getBricksOnHoles(field);
        //System.out.println(sumHeight + " " + completeLines + " " + holes + " " + heightDiff);
        
        double hrt = completeLinesCoefficient * completeLines 
                     - sumHeightCoefficient * sumHeight 
                     - holesEfficient * holes 
                     - heightDiffCoefficient * heightDiff 
                     - bricksOnHolesCoefficient*bricksOnHoles;
        return hrt;
    }
    
    private static int getBricksOnHoles(int[][] field) {
        int bricksOnHoles = 0;
        for (int i = 0; i < COLS; i++) {
            int brickCount = 0;
            for (int j = ROWS - 1; j >= 0; j--) {
                if (field[j][i] == 0) {
                    bricksOnHoles += brickCount;
                } else {
                    brickCount++;
                }
            }
        }
        return bricksOnHoles;
    }

    private static int[][] tryPuttingPiece(State curState, int[] move, int nextPiece) {
        int orient = move[0];
        int slot = move[1];
        int[][] field = new int[ROWS][COLS];
        int[][] originField = curState.getField();
        
        for (int i = 0; i < ROWS; i++){
            for (int j = 0; j < COLS; j++){
                field[i][j] = originField[i][j];
            }
        }

        int[] pBottom = State.getpBottom()[nextPiece][orient];
        int pWidth = State.getpWidth()[nextPiece][orient];
        int[] pTop = State.getpTop()[nextPiece][orient];
        int pHeight = State.getpHeight()[nextPiece][orient];
        
        int h = getHeight(slot,field) - pBottom[0];
        
        for (int i = 1; i < pWidth; i++){
            h = Math.max(h, getHeight(slot+i,field)- pBottom[i]);
        }
        
        if (h + pHeight >= ROWS){ //if leads to game end -> last resort
            return null;
        }
        
        for (int i = slot; i < slot + pWidth; i++){        
            int start = h + pBottom[i-slot];
            
            for (int j = 0; j < pTop[i-slot] - pBottom[i-slot]; j++){
                field[start+j][i] = 1;
            }
        }
        return field;
    }

    private static int getHeightDiff(int[][] field) {
        int heightDiff = 0;
        int[] heights = new int[COLS];
        for (int i = 0; i < COLS; i++){
            heights[i] = getHeight(i, field);
        }
        for (int i = 0; i < COLS; i++){
            if (i < COLS - 1){
                heightDiff += Math.abs(heights[i] - heights[i+1]);
            }
        }
        return heightDiff;
    }

    private static int getSumHeight(int[][] field) {
        int sumHeight = 0;
        for (int i = 0; i < COLS; i++) sumHeight += getHeight(i, field);
        return sumHeight;
    }

    public static void printField(int[][] field){
        for (int i = ROWS - 1; i >= 0; i--){
            for (int j = 0; j < field[i].length; j++){
                System.out.print(field[i][j] + " ");
            }
            System.out.println("");
        }
    }
    
    public static int getHeight(int col, int[][] field){
        for (int i = ROWS -1; i >= 0; i--){
            if (field[i][col] != 0){
                return i+1;
            }
        }
        return 0;
    }
    
    public static int getHoles(int[][] field){
        int numHoles = 0;
        int[] heights = new int[COLS];
        for (int i = 0; i < COLS; i++){
            heights[i] = getHeight(i, field);
        }
        for (int i = 0; i < COLS; i++){
            for (int j = 0; j < heights[i]; j++){
                if (field[j][i] == 0){
                    numHoles++;
                    break;
                }
            }
        }
        return numHoles;
    }
    
    public static int getCompleteLines(int[][] field){
        int lines = 0;
        boolean full = true;
        for (int i = 0; i < ROWS; i++){
            full = true;
            for (int j = 0; j < COLS; j++){
                if (field[i][j] == 0){
                    full = false;
                }
            }
            if (full){
                lines++;
            }
        }
        return lines;
    }
}
