package util;

public class MathU {
  public static double round(double pValue, int pKeta) {
    double result = pValue;
    double keta = Math.pow(10, pKeta);

    result = ((double)Math.round(result * keta))/keta;

    return result;
  }
}
