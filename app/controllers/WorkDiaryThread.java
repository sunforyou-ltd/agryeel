package controllers;

import java.util.ArrayList;

import models.Account;
import models.WorkDiary;
import models.WorkDiarySanpu;
import compornent.CompartmentStatusCompornent;
import compornent.HashuCompornent;
import compornent.MotochoCompornent;
import compornent.NouhiComprtnent;

public class WorkDiaryThread extends Thread {

  public ArrayList<ArrayList<WorkDiarySanpu>> wdspss = new ArrayList<ArrayList<WorkDiarySanpu>>();
  public ArrayList<WorkDiary> workDiarys             = new ArrayList<WorkDiary>();
  public Account account                             = new Account();

  public void run() {
    int i=0;
    for (WorkDiary workDiary : workDiarys) {
      ArrayList<WorkDiarySanpu> wdsps;
      if (wdspss.size() > 0) {
        wdsps = wdspss.get(i);
      }
      else {
        wdsps = new ArrayList<WorkDiarySanpu>();
      }
      /* 元帳照会を更新する */
      MotochoCompornent motochoCompornent = new MotochoCompornent(workDiary.kukakuId);
      motochoCompornent.make();

      /* 区画状況照会を更新する */
      CompartmentStatusCompornent compartmentStatusCompornent = new CompartmentStatusCompornent(workDiary.kukakuId, workDiary.workId);
      compartmentStatusCompornent.wdsps   = wdsps;
      compartmentStatusCompornent.wdDate  = workDiary.workDate;
      compartmentStatusCompornent.update(motochoCompornent.lastMotochoBase);
      i++;
    }
    /* 農肥使用回数を再集計する */
    NouhiComprtnent.updateUseCount(account.farmId);
    /* 播種回数を再集計する */
    HashuCompornent.updateUseCount(account.farmId);
  }
}
