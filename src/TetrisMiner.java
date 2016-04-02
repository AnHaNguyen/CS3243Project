import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TetrisMiner {
    private static final int MAX_COEFFICIENT_VALUE = 1000;
    private static final int DIFF_COEFFICIENT_VALUE = 50;
    private static final int GAMES = 100;
    private static Random rd;
    private static GameJudge judge;
    
    public static void main(String[] args) {
        rd = new Random();
        judge = new GameJudge(GameJudge.JUDGE_BY_MIN);
        
        //int[] coeff = {105,  -152,     1,   -48,   -18,    -2,   -27};
        int[] coeff = {0, 0, 0, 0, 0, 0, 0};
        
        for(int i=0;i<GAMES;i++) judge.addScore(playGame(coeff));
        double average = judge.getAverage();
        int minScore = judge.getMinScore();
        int maxScore = judge.getMaxScore();
        System.out.printf("Average of %5d, %5d, %5d, %5d, %5d, %5d, %5d is %6.2f (%d %d)\n", 
                coeff[0],coeff[1],coeff[2],coeff[3],coeff[4],coeff[5],coeff[6], average, minScore, maxScore);
        judge.saveCurrentScore();
        
        localSearch(coeff);
    }

    private static void localSearch(int[] coeff) {
        int numCoefficients = coeff.length;
        while (true) {
            int[] oldValues = new int[numCoefficients];
            for(int i=0;i<numCoefficients;i++) {
                oldValues[i] = coeff[i];
                coeff[i] = (coeff[i] + rd.nextInt(DIFF_COEFFICIENT_VALUE) - DIFF_COEFFICIENT_VALUE/2);
                coeff[i] = Math.max(coeff[i], -MAX_COEFFICIENT_VALUE);
                coeff[i] = Math.min(coeff[i], MAX_COEFFICIENT_VALUE);
            }
            
            judge.clearCurrentScore();
            for(int i=0;i<GAMES;i++) {
                judge.addScore(playGame(coeff));
                if (judge.canTerminate()) break;
            }
            
            if (judge.isCurrentBetter()) {
                System.out.printf(" (%5d, %5d, %5d, %5d, %5d, %5d, %5d)\n", coeff[0],coeff[1],coeff[2],coeff[3],coeff[4],coeff[5],coeff[6]);
                double average = judge.getAverage();
                int minScore = judge.getMinScore();
                int maxScore = judge.getMaxScore();
                System.out.printf("Average of %5d, %5d, %5d, %5d, %5d, %5d, %5d is %6.2f (%d %d)\n", 
                        coeff[0],coeff[1],coeff[2],coeff[3],coeff[4],coeff[5],coeff[6], average, minScore, maxScore);
                judge.saveCurrentScore();
            } else {
                System.out.printf(" (%5d, %5d, %5d, %5d, %5d, %5d, %5d)  Best: (%5d, %5d, %5d, %5d, %5d, %5d, %5d)\n", 
                        coeff[0],coeff[1],coeff[2],coeff[3],coeff[4],coeff[5],coeff[6],
                        oldValues[0],oldValues[1],oldValues[2],oldValues[3],oldValues[4],oldValues[5],oldValues[6]);
                for(int i=0;i<numCoefficients;i++) coeff[i] = oldValues[i];
            }
        }
    }

    private static int playGame(int[] coefficients) {
        State s = new State();
        PlayerSkeleton p = new PlayerSkeleton(coefficients);
        while(!s.hasLost()) s.makeMove(p.pickMove(s,s.legalMoves()));
        return s.getRowsCleared();
    }
    
    private static class GameJudge {
        public static int JUDGE_BY_MIN = 0;
        public static int JUDGE_BY_AVERAGE = 1;
        
        private int mode;
        private List<Integer> preScoreList;
        private List<Integer> scoreList;
        private int preMinScore;
        private double preSum;
        private int curMinScore;
        private int curMaxScore;
        private double curSum;
        
        public GameJudge(int MODE) {
            mode = MODE;
            preScoreList = new ArrayList<Integer>();
            scoreList = new ArrayList<Integer>();
            curMinScore = 1000000;
        }
        
        public void saveCurrentScore() {
            preScoreList.clear();
            preScoreList.addAll(scoreList);
            preMinScore = curMinScore;
            preSum = curSum;
        }
        
        public void clearCurrentScore() {
            scoreList.clear();
            curMinScore = 1000000;
            curMaxScore = 0;
            curSum = 0;
        }
        
        public void addScore(int score) {
            scoreList.add(score);
            curSum = curSum + score;
            curMinScore = Math.min(curMinScore, score);
            curMaxScore = Math.max(curMaxScore, score);
        }
        
        public boolean isCurrentBetter() {
            if (mode == JUDGE_BY_MIN) {
                System.out.print("" + curMinScore + " " + preMinScore);
                return curMinScore > preMinScore;
            } else if (mode == JUDGE_BY_AVERAGE) {
                System.out.print("" + curSum/scoreList.size() + " " + preSum/preScoreList.size());
                return curSum/scoreList.size() > preSum/preScoreList.size();
            }
            return false;
        }
        
        public boolean canTerminate() {
            if (mode == JUDGE_BY_MIN && curMinScore < preMinScore) {
                return true;
            }
            return false;
        }
        
        public double getAverage() {
            return curSum/scoreList.size();
        }
        
        public int getMinScore() {
            return curMinScore;
        }
        
        public int getMaxScore() {
            return curMaxScore;
        }
    }
}
