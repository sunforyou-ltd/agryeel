package controllers;

import java.util.ArrayList;

import models.Account;
import models.IkubyoDiary;
import models.IkubyoDiarySanpu;

import compornent.NaeMotochoCompornent;
import compornent.NaeStatusCompornent;
import compornent.NouhiComprtnent;

public class IkubyoDiaryThread extends Thread {

  public ArrayList<ArrayList<IkubyoDiarySanpu>> idspss = new ArrayList<ArrayList<IkubyoDiarySanpu>>();
  public ArrayList<IkubyoDiary> ikubyoDiarys           = new ArrayList<IkubyoDiary>();
  public Account account                               = new Account();

  public void run() {
    int i=0;
    for (IkubyoDiary ikubyoDiary : ikubyoDiarys) {
      ArrayList<IkubyoDiarySanpu> idsps;
      if (idspss.size() > 0) {
        idsps = idspss.get(i);
      }
      else {
        idsps = new ArrayList<IkubyoDiarySanpu>();
      }
      /* 元帳照会を更新する */
      NaeMotochoCompornent motochoCompornent = new NaeMotochoCompornent(ikubyoDiary.naeNo);
      motochoCompornent.make();

      /* 苗状況照会を更新する */
      NaeStatusCompornent naeStatusCompornent = new NaeStatusCompornent(ikubyoDiary.naeNo, ikubyoDiary.workId);
      naeStatusCompornent.idsps   = idsps;
      naeStatusCompornent.wdDate  = ikubyoDiary.workDate;
      naeStatusCompornent.update(motochoCompornent.lastMotochoBase);
      i++;
    }
    /* 農肥使用回数を再集計する */
    NouhiComprtnent.updateUseCount(account.farmId);
  }
}
