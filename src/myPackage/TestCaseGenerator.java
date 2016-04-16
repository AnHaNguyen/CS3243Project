package myPackage;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class TestCaseGenerator {
    private static int MAX_PIECES = 5000;
    private static int NUM_PIECES = State.N_PIECES;
    private static int TC = 0;
    private static Random rd;
    public static void main(String[] args) {
        rd = new Random();
        //generateTestCaseForEachTypeOfPiece();
        while(TC<100) {
            generateRandomTestCase();
        }
    }
    
    private static void generateRandomTestCase() {
        List<Integer> pieceSeq = new ArrayList<Integer>();
        for(int j=0;j<MAX_PIECES;j++) pieceSeq.add(rd.nextInt(NUM_PIECES));
        String fileName = getNextFile();
        writePieceSeqToFile(pieceSeq, fileName);
    }

    private static void generateTestCaseForEachTypeOfPiece() {
        for(int i=0;i<NUM_PIECES;i++) {
            List<Integer> pieceSeq = new ArrayList<Integer>();
            for(int j=0;j<MAX_PIECES;j++) pieceSeq.add(i);
            String fileName = getNextFile();
            writePieceSeqToFile(pieceSeq, fileName);
        }
    }

    private static void writePieceSeqToFile(List<Integer> pieceSeq, String fileName) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(fileName);
            for(Integer piece : pieceSeq) writer.print(piece + " ");
            writer.close();
            System.out.println(fileName + " created.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String getNextFile() {
        String fileName = "testcases/" + (TC++) + ".txt";
        return fileName;
    }

}
