import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TetrisMiner {
    private static final int MAX_COEFFICIENT_VALUE = 1000;
    private static final int DIFF_COEFFICIENT_VALUE = 50;
    private static final int GAMES = 50;
    
    public static void main(String[] args) {
        Random rd = new Random();
        GameJudge judge = new GameJudge(GameJudge.JUDGE_BY_MIN);
        
        int[] coeff = {422, 955, 48, 246, 48};
        int numCoefficients = coeff.length;
        
        int a = coeff[0];
        int b = coeff[1];
        int c = coeff[2];
        int d = coeff[3];
        int e = coeff[4];
        
        for(int i=0;i<GAMES;i++) judge.addScore(playGame(a,b,c,d,e));
        double average = judge.getAverage();
        int minScore = judge.getMinScore();
        int maxScore = judge.getMaxScore();
        System.out.println(String.format("Average of %2d, %2d, %2d, %2d, %2d is %6.2f (%d %d)", a,b,c,d,e, average, minScore, maxScore));
        judge.saveCurrentScore();
        
        while (true) {
            int[] oldValues = new int[numCoefficients];
            for(int i=0;i<numCoefficients;i++) {
                oldValues[i] = coeff[i];
                coeff[i] = (coeff[i] + rd.nextInt(DIFF_COEFFICIENT_VALUE) - DIFF_COEFFICIENT_VALUE/2);
                coeff[i] = Math.max(coeff[i], 0);
                coeff[i] = Math.min(coeff[i], MAX_COEFFICIENT_VALUE);
            }
            
            a = coeff[0];
            b = coeff[1];
            c = coeff[2];
            d = coeff[3];
            e = coeff[4];
            
            judge.clearCurrentScore();
            for(int i=0;i<GAMES;i++) {
                judge.addScore(playGame(a,b,c,d,e));
                if (judge.canTerminate()) break;
            }
            
            if (judge.isCurrentBetter()) {
                System.out.println(String.format(" (%2d, %2d, %2d, %2d, %2d)", a,b,c,d,e));
                average = judge.getAverage();
                minScore = judge.getMinScore();
                maxScore = judge.getMaxScore();
                System.out.println(String.format("Average of %2d, %2d, %2d, %2d, %2d is %6.2f (%d %d)", a,b,c,d,e, average, minScore, maxScore));
                judge.saveCurrentScore();
            } else {
                System.out.println(String.format(" (%2d, %2d, %2d, %2d, %2d)", a,b,c,d,e));
                for(int i=0;i<numCoefficients;i++) coeff[i] = oldValues[i];
            }
        }
    }

    private static int playGame(double completeLinesCoefficientParam, double sumHeightCoefficientParam, 
                                 double holesEfficientParam, double heightDiffCoefficientParam, 
                                 double bricksOnHolesCoefficientParam) {
        State s = new State();
        PlayerSkeleton p = new PlayerSkeleton(completeLinesCoefficientParam, sumHeightCoefficientParam,
                                              holesEfficientParam, heightDiffCoefficientParam, 
                                              bricksOnHolesCoefficientParam);
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
        private int preMaxScore;
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
            preMaxScore = curMaxScore;
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
