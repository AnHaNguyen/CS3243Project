package myPackage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeMap;

import myPackage.PlayerSkeleton.Feature;
import myPackage.PlayerSkeleton.NumBricksOnHoleFeature;
import myPackage.PlayerSkeleton.NumFlatPlacesFeature;
import myPackage.PlayerSkeleton.NumHolesFeature;
import myPackage.PlayerSkeleton.NumCompletedLinesFeature;
import myPackage.PlayerSkeleton.NumPieceTypesCanPutFeature;
import myPackage.PlayerSkeleton.SumHeightFeature;
import myPackage.PlayerSkeleton.MaxHeightFeature;
import myPackage.PlayerSkeleton.HeightDifferenceFeature;
import myPackage.PlayerSkeleton.RowTransitionsFeature;
import myPackage.PlayerSkeleton.ColumnTransitionsFeature;



public class TetrisMiner {
    private static final int MAX_COEFFICIENT_VALUE = 100;
    private static final int DIF_COEFFICIENT_VALUE = 100;
    private static final int GAMES = 100;
    private static final int INF = 1000000000;
    private static Random rd;
    //private static GameJudge judge;
    private static List<Integer> curGamePieceSeq = new ArrayList<Integer>();
    
    private static List<CoefficientGroup> mainCoefficientList;
    private static int MAX_SIZE_POPULATION = 10;
    private static int NUM_COEFFICIENTS = 7;
    
    private static CoefficientGroup getRandomCoefficientGroup() {
        CoefficientGroup result = new CoefficientGroup();
        for(int i=0;i<NUM_COEFFICIENTS;i++) {
            result.add(rd.nextInt(MAX_COEFFICIENT_VALUE*2) - MAX_COEFFICIENT_VALUE);
        }
        result.normalize();
        return result;
    }
    
    private static List<Feature> createFeatureList(CoefficientGroup coefficientGroup) {
        List<Feature> featureList = new ArrayList<Feature>(); 
        /*featureList.add(new NumCompletedLinesFeature(coefficientGroup.getCoefficient(0)));
        featureList.add(new NumPieceTypesCanPutFeature(coefficientGroup.getCoefficient(1)));
        featureList.add(new NumHolesFeature(coefficientGroup.getCoefficient(2)));
        featureList.add(new SumHeightFeature(coefficientGroup.getCoefficient(3)));
        //featureList.add(new MaxHeightFeature(coefficientGroup.getCoefficient(4)));
        featureList.add(new HeightDifferenceFeature(coefficientGroup.getCoefficient(4)));
        featureList.add(new RowTransitionsFeature(coefficientGroup.getCoefficient(5)));
        featureList.add(new ColumnTransitionsFeature(coefficientGroup.getCoefficient(6)));*/
        
        featureList.add(new NumCompletedLinesFeature(coefficientGroup.getCoefficient(0)));
        featureList.add(new SumHeightFeature(coefficientGroup.getCoefficient(1)));
        featureList.add(new MaxHeightFeature(coefficientGroup.getCoefficient(2)));
        featureList.add(new HeightDifferenceFeature(coefficientGroup.getCoefficient(3)));
        featureList.add(new NumHolesFeature(coefficientGroup.getCoefficient(4)));
        featureList.add(new NumBricksOnHoleFeature(coefficientGroup.getCoefficient(5)));
        featureList.add(new NumFlatPlacesFeature(coefficientGroup.getCoefficient(6)));
        
        return featureList;
    }
    
    public static void main(String[] args) {
        rd = new Random();
        //judge = new GameJudge(GameJudge.JUDGE_BY_MIN);
        mainCoefficientList = new ArrayList<CoefficientGroup>();
        
        for(int i=0;i<MAX_SIZE_POPULATION;i++) {
            CoefficientGroup coefficientGroup = getRandomCoefficientGroup();
            List<Feature> featureList = createFeatureList(coefficientGroup);
            
            TreeMap<String, Double> result = playGames(featureList, 10);
            coefficientGroup.minScoreReached = result.get("MIN").intValue();
            coefficientGroup.maxScoreReached = result.get("MAX").intValue();
            coefficientGroup.averageScoreReached = result.get("AVERAGE");
            
            if (coefficientGroup.minScoreReached == 0) {
                System.out.println("Repeat " + i);
                i--;
            } else {
                mainCoefficientList.add(coefficientGroup);
                System.out.println("Finished " + i);
            }
        }
        
        Collections.sort(mainCoefficientList);
        for(CoefficientGroup group : mainCoefficientList) {
            System.out.println(group);
        }
        
        while(true) {
            geneticSelection();
            randomWalkSelection();
            
            double sumScore = 0;
            for(int i=0;i<mainCoefficientList.size();i++) {
                sumScore = sumScore + mainCoefficientList.get(i).averageScoreReached;
            }
            System.out.println(String.format("%.2f", sumScore));
            System.out.println(mainCoefficientList.get(0));
            System.out.println(mainCoefficientList.get(mainCoefficientList.size()-1));
            
        }
    }

    private static void randomWalkSelection() {
        int chosen = rd.nextInt(MAX_SIZE_POPULATION);
        CoefficientGroup newGroup = new CoefficientGroup();
        for(int i=0;i<mainCoefficientList.get(chosen).size();i++) {
            double newCoeff = mainCoefficientList.get(chosen).getCoefficient(i) 
                              + rd.nextInt(DIF_COEFFICIENT_VALUE*2) - DIF_COEFFICIENT_VALUE;
            newGroup.add(newCoeff);
        }
        checkAndAddNewGroupIfBetter(newGroup);
    }

    private static void geneticSelection() {
        int chosen1 = 0;
        int chosen2 = 0;
        while(chosen1 == chosen2) {
            chosen1 = rd.nextInt(MAX_SIZE_POPULATION);
            chosen2 = rd.nextInt(MAX_SIZE_POPULATION);
        }
        CoefficientGroup newGroup = new CoefficientGroup();
        for(int i=0;i<mainCoefficientList.get(chosen1).size();i++) {
            double newCoeff = 0;
                newCoeff = mainCoefficientList.get(chosen1).getCoefficient(i) 
                           //* mainCoefficientList.get(chosen1).averageScoreReached
                           * mainCoefficientList.get(chosen1).minScoreReached
                           + mainCoefficientList.get(chosen2).getCoefficient(i) 
                           //* mainCoefficientList.get(chosen2).averageScoreReached;
                           * mainCoefficientList.get(chosen2).minScoreReached;
            newGroup.add(newCoeff);
        }
        checkAndAddNewGroupIfBetter(newGroup);
    }

    private static void checkAndAddNewGroupIfBetter(CoefficientGroup newGroup) {
        newGroup.normalize();
        
        List<Feature> featureList = createFeatureList(newGroup);
        TreeMap<String, Double> result = playGames(featureList, GAMES);
        newGroup.minScoreReached = result.get("MIN").intValue();
        newGroup.maxScoreReached = result.get("MAX").intValue();
        newGroup.averageScoreReached = result.get("AVERAGE");
        addNewGroupIfBetter(newGroup);
    }

    private static void addNewGroupIfBetter(CoefficientGroup newGroup) {
        int lastIndex = mainCoefficientList.size() - 1;
        //if (Double.compare(mainCoefficientList.get(lastIndex).averageScoreReached, newGroup.averageScoreReached) <= 0) {
        if (Double.compare(mainCoefficientList.get(lastIndex).minScoreReached*0.9, newGroup.minScoreReached) <= 0) {
            if (!hasGroup(newGroup)) {
                mainCoefficientList.remove(lastIndex);
                mainCoefficientList.add(newGroup);
                Collections.sort(mainCoefficientList);
            }
        }
    }

    private static boolean hasGroup(CoefficientGroup newGroup) {
        for(int i=0;i<mainCoefficientList.size();i++) {
            if (mainCoefficientList.get(i).equals(newGroup)) return true;
        }
        return false;
    }

    private static TreeMap<String, Double> playGames(List<Feature> featureList, int playTimes) {
        int minScore = INF;
        int maxScore = 0;
        double sumScore = 0;
        for(int i=0;i<playTimes;i++) {
            int score = playGame(i, featureList);
            minScore = Math.min(minScore, score);
            maxScore = Math.max(maxScore, score);
            sumScore = sumScore + score;
            //System.out.print(score + " ");
        }
        //System.out.println();
        TreeMap<String, Double> result = new TreeMap<String, Double>();
        result.put("MIN", minScore*1.0);
        result.put("MAX", maxScore*1.0);
        result.put("AVERAGE", sumScore/playTimes);
        return result;
    }
    
    private static int playGame(int game, List<Feature> featureList) {
        //List<Integer> pieceSeq = readPieceSeqFromTestCase(game);
        //State s = new State(pieceSeq);
        State s = new State();
        PlayerSkeleton p = new PlayerSkeleton(featureList);
        curGamePieceSeq.clear();
        while(!s.hasLost()) {
            curGamePieceSeq.add(s.getNextPiece());
            s.makeMove(p.pickMove(s,s.legalMoves()));
        }
        return s.getRowsCleared();
    }
    
    private static List<Integer> readPieceSeqFromTestCase(int game) {
        List<Integer> pieceSeq = new ArrayList<Integer>();
        try {
            Scanner sc = new Scanner(new File("testcases/"+game+".txt"));
            while(sc.hasNextInt()) pieceSeq.add(sc.nextInt());
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return pieceSeq;
    }
}
