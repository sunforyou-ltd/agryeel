package controllers;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;
import compornent.MailComprtnent;

import consts.AgryeelConst;

/**
 * 各種サービス用コントローラー
 * @author kimura
 *
 */
public class ServiceController extends Controller {

    public static Result mailSend(String name, String mail) {
      /* 戻り値用JSONデータの生成 */
      ObjectNode resultJson   = Json.newObject();

      /*------------------------------------------------------------------------------------------------------------*/
      /* レジストレーションコードを送信する                                                                         */
      /*------------------------------------------------------------------------------------------------------------*/
      StringBuffer sb = new StringBuffer();

      sb.append("（AICA関係者各位）\n");
      sb.append("\n");
      sb.append(name + "様からAICAの使用申請が届いています。\n");
      sb.append("登録メールアドレスは「" + mail + "」になります\n");
      sb.append("\n");
      sb.append("対応よろしくお願いします。\n");

      MailComprtnent.send("aica@sunforyou.jp", "｢AICA｣の使用申請が届いています", sb.toString());

      resultJson.put(AgryeelConst.Result.RESULT, AgryeelConst.Result.SUCCESS);
      return ok(resultJson);
    }
}
