package compornent;

import java.util.ArrayList;
import java.util.List;

import models.IkubyoDiary;
import models.IkubyoPlan;
import models.NaeStatus;
import models.Work;
import models.Youki;
import play.mvc.Http.Session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】育苗記録作業別廃棄
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class HaikiIkubyoCompornent extends CommonIkubyoDiaryWork {

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     */
    public HaikiIkubyoCompornent(Session session, ObjectNode resultJson) {
        super(session, resultJson);
    }

    /**
     * コンストラクタ
     * @param session
     * @param resultJson
     * @param api
     */
    public HaikiIkubyoCompornent(Session session, ObjectNode resultJson, Boolean api) {
        super(session, resultJson, api);
    }

    /**
     * 初期処理
     */
    @Override
    public void init() {

      String naeNo = "";
      String naeName = "";
      double haikiRyo = 0;
      double haikiSuryo = 0;
      double zaikoKosu = 0;
      double kosu = 0;
      int haikiUnit = 0;

      /* 廃棄情報の取得 */
      double farmId = Double.parseDouble(this.session.get((AgryeelConst.SessionKey.FARMID)));
      if (this.ikubyoDiary != null) { /* 育苗記録編集の場合 */
        NaeStatus naeStatusData = NaeStatus.getStatusOfNae(this.naeNo);
        IkubyoDiary id = IkubyoDiary.find.where().eq("nae_no", this.naeNo).ne("youki_id", 0).findUnique();
        Youki y = Youki.getYoukiInfo(id.youkiId);

        haikiRyo   = this.ikubyoDiary.haikiRyo;    //廃棄量
        haikiSuryo = this.ikubyoDiary.naeSuryo;    //廃棄数量
        zaikoKosu  = naeStatusData.kosu;           //在庫個数
        kosu       = y.kosu;
        haikiUnit  = y.unitKind;
      }
      else if (this.ikubyoPlan != null) { /* 作業計画編集の場合 */

        haikiRyo      = this.ikubyoPlan.haikiRyo;     //廃棄量
        haikiSuryo    = this.ikubyoPlan.naeSuryo;     //廃棄数量
      }
      else {
        if ("".equals(this.naeNo)) {
          List<NaeStatus> naeStatusList = NaeStatus.find.where().eq("farm_id", farmId).ne("zaiko_suryo", 0).orderBy("nae_no").findList();
          for (NaeStatus ns : naeStatusList) {
            IkubyoDiary id = IkubyoDiary.find.where().eq("nae_no", ns.naeNo).ne("youki_id", 0).findUnique();
            Youki y = Youki.getYoukiInfo(id.youkiId);
        
            naeNo      = ns.naeNo;
            String[] sNaeNos = naeNo.split("-");
            naeName    = ns.hinsyuName + "(" + sNaeNos[1] + ")";
            haikiRyo   = ns.kosu;                      //廃棄量
            haikiSuryo = ns.zaikoSuryo;                //廃棄数量
            zaikoKosu  = ns.kosu;                      //在庫個数
            kosu       = y.kosu;
            haikiUnit  = y.unitKind;
            break;
          }
          resultJson.put("naeNo", naeNo);
          resultJson.put("naeName", naeName);
        }
        else {
          NaeStatus naeStatus = NaeStatus.find.where().eq("nae_no", this.naeNo).findUnique();
          IkubyoDiary id = IkubyoDiary.find.where().eq("nae_no", this.naeNo).ne("youki_id", 0).findUnique();
          Youki y = Youki.getYoukiInfo(id.youkiId);

          haikiRyo   = naeStatus.kosu;                      //廃棄量
          haikiSuryo = naeStatus.zaikoSuryo;                //廃棄数量
          zaikoKosu  = naeStatus.kosu;                      //在庫個数
          kosu       = y.kosu;
          haikiUnit  = y.unitKind;
        }
      }

      resultJson.put("haikiRyo"      , haikiRyo);      //廃棄量
      resultJson.put("haikiSuryo"    , haikiSuryo);    //廃棄数量
      resultJson.put("zaikoKosu"     , zaikoKosu);     //在庫個数
      resultJson.put("kosu"          , kosu);          //容器個数
      resultJson.put("haikiUnit"     , haikiUnit);     //廃棄単位種別
    }

    /**
     * 育苗記録保存
     */
    @Override
    public void commit(JsonNode input, IkubyoDiary ikd, Work wk) {

      super.commit(input, ikd, wk);

      try {
    	ikd.haikiRyo = Double.parseDouble(input.get("haikiRyo").asText());
    	ikd.naeSuryo = Double.parseDouble(input.get("haikiSuryo").asText());
      }
      catch (Exception ex) {
    	ikd.haikiRyo = 0;
    	ikd.naeSuryo = 0;
      }
    }
    /**
     * 育苗計画保存
     */
    @Override
    public void plan(JsonNode input, IkubyoPlan ikp, Work wk) {

      super.plan(input, ikp, wk);

      try {
        ikp.haikiRyo = Double.parseDouble(input.get("haikiRyo").asText());
        ikp.naeSuryo = Double.parseDouble(input.get("haikiSuryo").asText());
      }
      catch (Exception ex) {
    	ikp.haikiRyo = 0;
    	ikp.naeSuryo = 0;
      }

    }

}
