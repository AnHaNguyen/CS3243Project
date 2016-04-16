package myPackage;
import java.util.ArrayList;
import java.util.List;

public class CoefficientGroup implements Comparable<CoefficientGroup>{
    private static final double EPS = 0.000000000001;
    
    private List<Double> coefficientList;
    public int minScoreReached;
    public int maxScoreReached;
    public double averageScoreReached;
    
    public CoefficientGroup() {
        coefficientList = new ArrayList<Double>();
    }
    
    public void add(double num) {
        coefficientList.add(num);
    }
    
    public void normalize() {
        double tmp = 0;
        for(int i=0;i<coefficientList.size();i++) {
            tmp = tmp + coefficientList.get(i)*coefficientList.get(i);
        }
        tmp = Math.sqrt(tmp);
        for(int i=0;i<coefficientList.size();i++) {
            coefficientList.set(i, coefficientList.get(i)/tmp);
        }
    }

    public double getCoefficient(int index) {
        return coefficientList.get(index);
    }
    
    public int size() {
        return coefficientList.size();
    }
    
    public boolean equals(CoefficientGroup o) {
        for(int i=0;i<size();i++) {
            if (Math.abs(getCoefficient(i) - o.getCoefficient(i)) > EPS) return false;
        }
        return true;
    }
    
    @Override
    public int compareTo(CoefficientGroup o) {
        return -Double.compare(minScoreReached, o.minScoreReached);
        //return -Double.compare(averageScoreReached, o.averageScoreReached);
    }
    
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(String.format("%.2f", averageScoreReached) + " (" + minScoreReached + ", " + maxScoreReached +")");
        result.append(" {");
        for(int i=0;i<coefficientList.size()-1;i++) {
            result.append(coefficientList.get(i));
            result.append(",");
        }
        result.append(coefficientList.get(coefficientList.size()-1));
        result.append("}");
        return result.toString();
    }
}
