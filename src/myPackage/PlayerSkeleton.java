package myPackage;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class PlayerSkeleton {
    private static final boolean SLOW_MODE = false; // set this to false to play fast
    private static final int SLEEP_TIME = 1000;
    private static final int END_GAME = -1000000;
    private static final int COLS = State.COLS;
    private static final int ROWS = State.ROWS;
    private static final int N_PIECES = State.N_PIECES;
    private static final int MAX_DEPTH = 0;
    private static final String MOVE_KEY = "MOVE";
    private static final String POINT_KEY = "POINT";
    
    
    public List<Feature> featureList;
    private int[][][] legalMoves;
    
    //implement this function to have a working system
    public int pickMove(State s, int[][] legalMoves) {
        Field field = new Field(s.getField());
        TreeMap<String, Double> result = DFS(0, s.getNextPiece(), field);
        if (result == null) return 0;
        return result.get(MOVE_KEY).intValue();
    }
    
    private TreeMap<String, Double> DFS(int depth, int nextPiece, Field field) {
        if (depth > MAX_DEPTH) return null;
        int[][] possibleMoves = legalMoves[nextPiece];
        TreeMap<String, Double> bestResult = null;
        for(int i=0;i<possibleMoves.length;i++) {
            int[][] tmpField = tryPuttingPiece(field.field, possibleMoves[i], nextPiece);
            if (tmpField == null) {
                continue;
            }
            Field newField = new Field(tmpField);
            TreeMap<String, Double> worstResult = null;
            for(int j=0;j<N_PIECES;j++) {
                TreeMap<String, Double> result = DFS(depth+1, j, newField);
                if (result != null) {
                    if (worstResult == null || worstResult.get(POINT_KEY).compareTo(result.get(POINT_KEY)) > 0) {
                        worstResult = result;
                    }
                }
            }
            if (worstResult != null) {
                if (bestResult == null || worstResult.get(POINT_KEY).compareTo(bestResult.get(POINT_KEY)) > 0 ) {
                    bestResult = new TreeMap<String, Double>();
                    bestResult.put(POINT_KEY, worstResult.get(POINT_KEY));
                    bestResult.put(MOVE_KEY, i*1.0);
                }
            } else {
                double heuristicValue = getHeuristicValue(newField);
                if (bestResult == null || bestResult.get(POINT_KEY).compareTo(heuristicValue) < 0 ) {
                    bestResult = new TreeMap<String, Double>();
                    bestResult.put(POINT_KEY, heuristicValue);
                    bestResult.put(MOVE_KEY, i*1.0);
                }
            }
        }
        
        return bestResult;
    }

    public PlayerSkeleton(List<Feature> features) {
        featureList = features;
        initLegalMoves();
    }
    
    public PlayerSkeleton(int[] coeff) {
        featureList = new ArrayList<Feature>();
    }
    
    private static double[] defaultValues = {105,  -152,     1,   -48,   -18,    -2,   -27};
    //private static int[] defaultValues = {105,  -152,     1,   -48,   -18,    -2,   -27};
    // Default Values 105,  -152,     1,   -48,   -18,    -2,   -27
    // Average Score: 1638.15 in 100 games 
    // Minimum Score: 111
    // Maximum Score: 8229
    
    public PlayerSkeleton() {
        featureList = new ArrayList<Feature>();
        featureList.add(new NumCompletedLinesFeature(defaultValues[0]));
        featureList.add(new SumHeightFeature(defaultValues[1]));
        featureList.add(new MaxHeightFeature(defaultValues[2]));
        featureList.add(new HeightDifferenceFeature(defaultValues[3]));
        featureList.add(new NumHolesFeature(defaultValues[4]));
        featureList.add(new NumBricksOnHoleFeature(defaultValues[5]));
        featureList.add(new NumFlatPlacesFeature(defaultValues[6]));
        
        //featureList.add(new NumPieceTypesCanPutFeature(defaultValues[1]));
        //featureList.add(new RowTransitionsFeature(defaultValues[5]));
        //featureList.add(new ColumnTransitionsFeature(defaultValues[6]));
        
        
        initLegalMoves();
    }
    
    private void initLegalMoves() {
        legalMoves = new int[N_PIECES][][];
        //for each piece type
        for(int i = 0; i < N_PIECES; i++) {
            //figure number of legal moves
            int n = 0;
            for(int j = 0; j < State.getpOrients()[i]; j++) {
                //number of locations in this orientation
                n += COLS+1-State.getpWidth()[i][j];
            }
            //allocate space
            legalMoves[i] = new int[n][2];
            //for each orientation
            n = 0;
            for(int j = 0; j < State.getpOrients()[i]; j++) {
                //for each slot
                for(int k = 0; k < COLS+1-State.getpWidth()[i][j];k++) {
                    legalMoves[i][n][State.ORIENT] = j;
                    legalMoves[i][n][State.SLOT] = k;
                    n++;
                }
            }
        }
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
            System.out.println(s.getRowsCleared());
        }
        System.out.println("You have completed "+s.getRowsCleared()+" rows.");
    }

    public double heur(State curState, int[] move){
        //update field
        int[][] resultField = tryPuttingPiece(curState.getField(), move, curState.getNextPiece());
        if (resultField == null) return END_GAME;    // end game
        return getHeuristicValue(new Field(resultField));
    }
    
    private double getHeuristicValue(Field field) {
        double hrt = 0;
        for(Feature feature : featureList) hrt = hrt + feature.getValue(field);
        return hrt;
    }
    
    private static int[][] clone(int[][] array) {
        int[][] result = new int[array.length][];
        for(int i=0;i<array.length;i++) {
            result[i] = new int[array[i].length];
            for(int j=0;j<array[i].length;j++) result[i][j] = array[i][j];
        }
        return result;
    }
    
    private static int[][] tryPuttingPiece(int[][] originField, int[] move, int nextPiece) {
        int orient = move[0];
        int slot = move[1];
        int[][] field = clone(originField);
        
        int[] pBottom = State.getpBottom()[nextPiece][orient];
        int pWidth = State.getpWidth()[nextPiece][orient];
        int[] pTop = State.getpTop()[nextPiece][orient];
        int pHeight = State.getpHeight()[nextPiece][orient];
        
        int h = getHeight(slot,field) - pBottom[0];
        for (int i = 1; i < pWidth; i++) h = Math.max(h, getHeight(slot+i,field) - pBottom[i]);
        
        //if leads to game end -> last resort
        if (h + pHeight >= ROWS) return null;
        
        for (int i = slot; i < slot + pWidth; i++) {
            int start = h + pBottom[i-slot];
            for (int j = 0; j < pTop[i-slot] - pBottom[i-slot]; j++) field[start+j][i] = 1;
        }
        return field;
    }

    /*private static void printField(int[][] field){
        for (int i = ROWS - 1; i >= 0; i--){
            for (int j = 0; j < field[i].length; j++) System.out.print(field[i][j] + " ");
            System.out.println("");
        }
    }*/
    
    public static int getHeight(int col, int[][] field){
        for (int i = ROWS - 1; i >= 0; i--) {
            if (field[i][col] != 0) return i+1;
        }
        return 0;
    }
    

    public static class Field {
        public int[][] field;
        public int[] heights;
        private int completedLines;
        
        private static int[] getHeightsOfColumns(int[][] field) {
            int[] heights = new int[COLS];
            for (int i = 0; i < COLS; i++) heights[i] = getHeight(i, field);
            return heights;
        }
        
        public Field(int[][] newField) {
            field = PlayerSkeleton.clone(newField);
            completedLines = removeCompletLines();
            heights = getHeightsOfColumns(field);
        }
        
        public int getCompletedLines() {
            return completedLines;
        }
        
        public boolean isCompleteLine(int rowIndex) {
            for (int j = 0; j < COLS; j++) if (field[rowIndex][j] == 0) return false;
            return true;
        }
        
        private int removeCompletLines() {
            if (field == null) return 0;
            // check for full rows - starting at the top
            
            int removed = 0;
            for(int r = ROWS - 1; r >= 0; r--) {
                //if the row was full - remove it and slide above stuff down
                if(isCompleteLine(r)) {
                    removeLine(r);
                    removed++;
                }
            }
            return removed;
        }

        private void removeLine(int r) {
            for(int c = 0; c < COLS; c++) {
                //slide down all bricks
                for(int i = r; i < ROWS; i++) field[i][c] = (i + 1 < ROWS ? field[i+1][c] : 0);
            }
        }
    }
    
    public static class Feature {
        public double coefficient;
        
        public Feature() {
            this(0);
        }
        
        public Feature(double coeff) {
            coefficient = coeff;
        }
        
        public double getValue(Field field) {
            return 0;
        }
    };
    
    public static class NumHolesFeature extends Feature {
        public NumHolesFeature(double coeff) {
            coefficient = coeff;
        }
        @Override
        public double getValue(Field field) {
            if (field == null) return -END_GAME;
            int result = 0;
            for (int i = 0; i < COLS; i++){
                for (int j = 0; j < field.heights[i]; j++){
                    if (field.field[j][i] == 0) {
                        result++;
                        break;
                    }
                }
            }
            return result * coefficient;
        }
    }
    
    public static class SumHeightFeature extends Feature {
        public SumHeightFeature(double coeff) {
            coefficient = coeff;
        }
        @Override
        public double getValue(Field field) {
            int result = 0;
            for (int i = 0; i < COLS; i++) result += field.heights[i];
            return result * coefficient;
        }
    }
    
    public static class HeightDifferenceFeature extends Feature {
        public HeightDifferenceFeature(double coeff) {
            coefficient = coeff;
        }
        @Override
        public double getValue(Field field) {
            int result = 0;
            for (int i = 0; i < COLS - 1; i++) result += Math.abs(field.heights[i] - field.heights[i+1]);
            return result * coefficient;
        }
    }
    
    public static class MaxHeightFeature extends Feature {
        public MaxHeightFeature(double coeff) {
            coefficient = coeff;
        }
        @Override
        public double getValue(Field field) {
            int result = 0;
            for (int i = 0; i < COLS; i++) result = Math.max(result, field.heights[i]);
            return result * coefficient;
        }
    }
    
    public static class NumBricksOnHoleFeature extends Feature {
        public NumBricksOnHoleFeature(double coeff) {
            coefficient = coeff;
        }
        @Override
        public double getValue(Field field) {
            int result = 0;
            for (int i = 0; i < COLS; i++) {
                int brickCount = 0;
                for (int j = ROWS - 1; j >= 0; j--) {
                    if (field.field[j][i] == 0) {
                        result += brickCount;
                    } else {
                        brickCount++;
                    }
                }
            }
            return result * coefficient;
        }
    }
    
    public static class NumFlatPlacesFeature extends Feature {
        public NumFlatPlacesFeature(double coeff) {
            coefficient = coeff;
        }
        @Override
        public double getValue(Field field) {
            int result = 0;
            for (int i = 0; i < COLS - 1; i++) if (field.heights[i] == field.heights[i+1]) result++;
            return result * coefficient;
        }
    }
    
    public static class RowTransitionsFeature extends Feature {
        public RowTransitionsFeature(double coeff) {
            coefficient = coeff;
        }
        @Override
        public double getValue(Field field) {
            int result = 0;
            for (int i = 0; i < ROWS; i++) {
                for(int j=0;j<COLS-1;j++) {
                    if ((field.field[i][j] == 0 && field.field[i][j+1] != 0) 
                        || (field.field[i][j] != 0 && field.field[i][j+1] == 0)) result++;
                }
            }
            return result * coefficient;
        }
    }
    
    public static class ColumnTransitionsFeature extends Feature {
        public ColumnTransitionsFeature(double coeff) {
            coefficient = coeff;
        }
        @Override
        public double getValue(Field field) {
            int result = 0;
            for (int i = 0; i < ROWS-1; i++) {
                for(int j=0;j<COLS;j++) {
                    if ((field.field[i][j] == 0 && field.field[i+1][j] != 0) 
                        || (field.field[i][j] != 0 && field.field[i+1][j] == 0)) result++;
                }
            }
            return result * coefficient;
        }
    }
    
    public static class NumPieceTypesCanPutFeature extends Feature {
        public NumPieceTypesCanPutFeature(double coeff) {
            coefficient = coeff;
        }
        private static boolean hasSameValue(int[] diff, int[] bottom) {
            for(int i=0;i<diff.length;i++) if (diff[i] != bottom[i]) return false;
            return true;
        }
        
        private static boolean canPutPieceSafely(int piece, Field field) {
            for (int orient = 0; orient < State.getpOrients()[piece]; orient++) {
                int pieceWidth = State.getpWidth()[piece][orient];
                for (int col = 0; col + pieceWidth - 1 < COLS; col++) {
                    int[] diff = new int[pieceWidth];
                    int minDiff = 0;
                    for(int i=1; i<pieceWidth;i++) {
                        diff[i] = field.heights[col+i] - field.heights[col+i-1];
                        minDiff = Math.min(minDiff, diff[i]);
                    }
                    for(int i=0; i<pieceWidth;i++) diff[i] -= minDiff;
                    if (hasSameValue(diff, State.getpBottom()[piece][orient])) return true;
                }
            }
            return false;
        }

        @Override
        public double getValue(Field field) {
            int result = 0;
            for (int i = 0; i < N_PIECES; i++) if (canPutPieceSafely(i, field)) result++;
            return result * coefficient;
        }
    }
    
    public static class NumCompletedLinesFeature extends Feature {
        public NumCompletedLinesFeature(double coeff) {
            coefficient = coeff;
        }
        @Override
        public double getValue(Field field) {
            int result = field.getCompletedLines();
            return result * coefficient;
        }
    }
}
