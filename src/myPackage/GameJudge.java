package myPackage;
import java.util.ArrayList;
import java.util.List;


class GameJudge {
    public static int JUDGE_BY_MIN = 0;
    public static int JUDGE_BY_AVERAGE = 1;
    
    public int mode;
    private List<Integer> bestScoreList;
    private List<Integer> scoreList;
    private int bestMinScore;
    private double bestSum;
    private int bestMaxScore;
    private int curMinScore;
    private int curMaxScore;
    private double curSum;
    
    public GameJudge(int MODE) {
        mode = MODE;
        bestScoreList = new ArrayList<Integer>();
        bestMinScore = 1000000;
        scoreList = new ArrayList<Integer>();
        curMinScore = 1000000;
    }
    
    public boolean isWorstScoreInCurrentList(int score) {
        return score <= curMinScore;
    }

    public void saveCurrentScoreAsBestScore() {
        bestScoreList.clear();
        bestScoreList.addAll(scoreList);
        bestMinScore = curMinScore;
        bestSum = curSum;
        bestMaxScore = curMaxScore;
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
        System.out.printf("{%d %.2f %d} {%d %.2f %d}", curMinScore, getCurrentAverageScore(), curMaxScore,
                                                   bestMinScore, getBestAverageScore(), bestMaxScore);
        if (mode == JUDGE_BY_MIN) return curMinScore > bestMinScore;
        else if (mode == JUDGE_BY_AVERAGE) return getCurrentAverageScore() > getBestAverageScore();
        return false;
    }
    
    public boolean canTerminate() {
        if (mode == JUDGE_BY_MIN && curMinScore < bestMinScore) {
            return true;
        }
        return false;
    }
    
    public double getCurrentAverageScore() {
        return curSum/scoreList.size();
    }
    
    public double getBestAverageScore() {
        return bestSum/bestScoreList.size();
    }
    
    public int getCurrentMinScore() {
        return curMinScore;
    }
    
    public int getBestMinScore() {
        return bestMinScore;
    }
    
    public int getMaxScore() {
        return curMaxScore;
    }
}