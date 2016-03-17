import java.io.*;

public class PlayerSkeleton {
	public static final int COLS = 10;
	public static final int ROWS = 21;
	public static final int N_PIECES = 7;
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
		System.out.println(max);
		System.out.println(bestMove);
		return bestMove;
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
				Thread.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("You have completed "+s.getRowsCleared()+" rows.");
		
	}

	/*heuristics function taken from:
	 * https://codemyroad.wordpress.com/2013/04/14/tetris-ai-the-near-perfect-player/
	 */
	public static double heur(State curState, int[] move){
		/* Heuristics function is calculated by the following formula
		 * a * (Aggregate Height) + b * (Complete Lines) + c * (Holes) + d * (Bumpiness)
		 * where a = -0.510066, b = 0.760666, c = -0.35663, d = -0.184483
		*/
		//define constants
		double a = -0.510066;
		double b = 0.760666;
		double c = -0.35663;
		double d = -0.184483;
		
		//update field
		int nextPiece = curState.getNextPiece();
		
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
			return -1000;
		}
		
		for (int i = slot; i < slot + pWidth; i++){		
			int start = h + pBottom[i-slot];
			
			for (int j = 0; j < pTop[i-slot] - pBottom[i-slot]; j++){
				field[start+j][i] = 1;
			}
		}
		
		//calculate heuristics values
		int[] heights = new int[COLS];
		for (int i = 0; i < COLS; i++){
			heights[i] = getHeight(i, field);
		}
		
		int minHeight = ROWS;
		int sumHeight = 0;
		int heightDiff = 0;
		for (int i = 0; i < COLS; i++){
			minHeight = Math.min(minHeight, heights[i]);
			sumHeight += heights[i];
			if (i < COLS - 1){
				heightDiff += Math.abs(heights[i] - heights[i+1]);
			}
		}
		int completeLines = getCompleteLines(field, minHeight);
		int holes = getHoles(field, heights);
		//System.out.println(sumHeight + " " + completeLines + " " + holes + " " + heightDiff);
		
		double hrt = a*sumHeight + b*completeLines + c*holes + d*heightDiff;
		return hrt;
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
	
	public static int getHoles(int[][] field, int[] heights){
		int numHoles = 0;
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
	
	public static int getCompleteLines(int[][] field, int height){
		int lines = 0;
		boolean full = true;
		for (int i = 0; i < height; i++){
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
