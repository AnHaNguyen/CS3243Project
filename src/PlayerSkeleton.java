

public class PlayerSkeleton {
    private static final boolean SLOW_MODE = false; // set this to false to play fast
    private static final int SLEEP_TIME = 300;
    private static final int END_GAME = -1000000;
    private static final int COLS = 10;
    private static final int ROWS = 21;
    
    public static int COMPLETE_LINES = 0;
    public static int SUM_HEIGHT = 1;
    public static int MAX_HEIGHT = 2;
    public static int HEIGHT_DIFF = 3;
    public static int HOLES = 4;
    public static int BRICKS_ON_HOLES = 5;
    public static int FLAT_PLACE = 6;
    
    private static int completeLinesCoefficient;
    private static int sumHeightCoefficient;
    private static int maxHeightCoefficient;
    private static int heightDiffCoefficient;
    private static int holesEfficient;
    private static int bricksOnHolesCoefficient;
    private static int flatPlaceCoefficient;
    
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
    
    public PlayerSkeleton(int[] coefficients) {
        completeLinesCoefficient = coefficients[COMPLETE_LINES];
        sumHeightCoefficient = coefficients[SUM_HEIGHT];
        maxHeightCoefficient = coefficients[MAX_HEIGHT];
        heightDiffCoefficient = coefficients[HEIGHT_DIFF];
        holesEfficient = coefficients[HOLES];
        bricksOnHolesCoefficient = coefficients[BRICKS_ON_HOLES];
        flatPlaceCoefficient = coefficients[FLAT_PLACE];
    }
    
    private static int[] defaultValues = {105,  -152,     1,   -48,   -18,    -2,   -27};
    // Default Values 105,  -152,     1,   -48,   -18,    -2,   -27
    // Average Score: 1638.15 in 100 games 
    // Minimum Score: 111
    // Maximum Score: 8229

    public PlayerSkeleton() {
        this(defaultValues);
    }
    
    public static void main(String[] args) {
        State s = new State();
        new TFrame(s);
        PlayerSkeleton p = new PlayerSkeleton();
        while(!s.hasLost()) {
            s.makeMove(p.pickMove(s,s.legalMoves()));
            System.out.println(s.getRowsCleared());
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
        int maxHeight = getMaxHeight(field);
        int heightDiff = getHeightDiff(field);
        int completeLines = getCompleteLines(field);
        int holes = getHoles(field);
        int bricksOnHoles = getBricksOnHoles(field);
        int flatPlaces = getFlatPlaces(field);
        //System.out.println(sumHeight + " " + completeLines + " " + holes + " " + heightDiff);
        
        double hrt = completeLinesCoefficient * completeLines 
                     + sumHeightCoefficient * sumHeight 
                     + maxHeightCoefficient * maxHeight 
                     + holesEfficient * holes 
                     + heightDiffCoefficient * heightDiff 
                     + bricksOnHolesCoefficient * bricksOnHoles
                     + flatPlaceCoefficient * flatPlaces;
        return hrt;
    }
    
    private static int getFlatPlaces(int[][] field) {
        int heightFlat = 0;
        int[] heights = new int[COLS];
        for (int i = 0; i < COLS; i++) heights[i] = getHeight(i, field);
        for (int i = 0; i < COLS - 1; i++) if (heights[i] == heights[i+1]) heightFlat++;
        return heightFlat;
    }

    private static int getMaxHeight(int[][] field) {
        int maxHeight = 0;
        for (int i = 0; i < COLS; i++) maxHeight = Math.max(maxHeight, getHeight(i, field));
        return maxHeight;
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
        for (int i = 0; i < COLS; i++) heights[i] = getHeight(i, field);
        for (int i = 0; i < COLS - 1; i++) heightDiff += Math.abs(heights[i] - heights[i+1]);
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
        for (int i = ROWS - 1; i >= 0; i--){
            if (field[i][col] != 0) return i+1;
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
        for (int i = 0; i < ROWS; i++){
            if (isCompleteLine(field[i])) lines++;
        }
        return lines;
    }

    private static boolean isCompleteLine(int[] row) {
        for (int j = 0; j < COLS; j++) if (row[j] == 0) return false;
        return true;
    }
}
