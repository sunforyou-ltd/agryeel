package compornent;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import models.Weather;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

import util.DateU;

public class AicaCompornent {

  double   bSekisan0 = 0;
  double[] bSekisanx = new double[7];
  public int getSekisanCount() {
    return bSekisanx.length;
  }


  public boolean modelingSekisan(String pPointCd, java.sql.Date pBaseDate) {
    boolean modeling = false;
    Calendar cStart = Calendar.getInstance();
    cStart.setTime(pBaseDate);
    Calendar cEnd   = Calendar.getInstance();
    cEnd.setTime(pBaseDate);
    cEnd.add(Calendar.MONTH, 2);

    long dfDate = DateU.GetDiffDate(cStart.getTime(), cEnd.getTime());
    DecimalFormat     df = new DecimalFormat("#,##0.0");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");

    Calendar cprev  = Calendar.getInstance();
    Calendar cSys   = Calendar.getInstance();
    cprev.add(Calendar.DATE, -20);
    List<double[]> aryY = new ArrayList<double[]>();
    List<double[][]> aryX = new ArrayList<double[][]>();
    int countY = 0;
    for (int i = 0; i < (int)dfDate; i++) {
      aryY.clear();
      aryX.clear();
      countY = 0;
      if (i != 0) {
        cprev.add(Calendar.DATE, 1);
        cSys.add(Calendar.DATE, 1);
        cStart.add(Calendar.DATE, 1);
      }

      cprev.set(Calendar.YEAR, cStart.get(Calendar.YEAR));
      cSys.set(Calendar.YEAR, cStart.get(Calendar.YEAR));
      if (cprev.compareTo(cSys) == 1) {
        cSys.add(Calendar.YEAR, 1);
      }
      for (int iYear = 0; iYear < 10; iYear++) {
        cprev.add(Calendar.YEAR, -1);
        cSys.add(Calendar.YEAR, -1);
        List<Weather> weathers = Weather.getWeather(pPointCd, new java.sql.Date(cprev.getTimeInMillis()), new java.sql.Date(cSys.getTimeInMillis()));

        if (weathers == null || weathers.size() == 0) {
          continue;
        }

        double[]  y    = new double[weathers.size() - 7];
        double[][] x   = new double[weathers.size() - 7][7];

        int idxx = 0;
        int idxy = 0;

        for (Weather w : weathers) {
          if (idxx <= 6) {
            x[idxy][idxx] = w.kionAve;
          }
          else {
            y[idxy]       = w.kionAve;
//            Logger.info("[ AICA COMP ] {} {} {} {} {} {} {} = {}", x[idxy][0], x[idxy][1], x[idxy][2], x[idxy][3], x[idxy][4], x[idxy][5], x[idxy][6], y[idxy]);
            idxy++;
            countY++;
            if ((weathers.size() - 7) <= idxy) {
              break;
            }
            for (int idx=1; idx<=6; idx++) {
              x[idxy][idx-1]  = x[idxy-1][idx];
              x[idxy][6]      = w.kionAve;
            }
          }
          idxx++;
        }
        aryY.add(y);
        aryX.add(x);
      }

    }

    OLSMultipleLinearRegression mr  = new OLSMultipleLinearRegression();

    double[]  y    = new double[countY];
    double[][] x   = new double[countY][7];

    int iYidx = 0;
    int iXidx = 0;
    for (double[] ydata: aryY) {
      double[][] xx  = aryX.get(iXidx);
      for (int ii = 0; ii < ydata.length; ii++) {
        y[iYidx] = ydata[ii];
        x[iYidx] = xx[ii];
        iYidx++;
      }
      iXidx++;
    }

    mr.newSampleData(y, x);

    double[] coes = mr.estimateRegressionParameters();
    boolean init = true;
    int ibx = 0;
    for (double coe : coes) {
      if (init) {
        this.bSekisan0      = coe;
        init = false;
//        Logger.info("----------------------------------------------------------");
//        Logger.info("[B0]{}", this.bSekisan0);
      }
      else {
        this.bSekisanx[ibx] = coe;
//        Logger.info("[B{}]{}",(ibx + 1), this.bSekisanx[ibx]);
        ibx++;
      }
    }
    modeling = true;
    return modeling;
  }
  public double[] predictionSekisan(double[] x, int checkCount) {
    double[] sekisan = new double[checkCount];
    for (int i=0; i < checkCount; i++) {
      sekisan[i] = this.bSekisan0;
      for (int idx=0; idx < 7; idx++) {
        sekisan[i] += (x[idx] * this.bSekisanx[idx]);
      }
      for (int idx=0; idx < 6; idx++) {
        x[idx] = x[idx + 1];
      }
      x[6] = sekisan[i];
    }
    return sekisan;
  }
}
