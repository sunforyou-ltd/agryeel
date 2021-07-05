package controllers;

import java.util.List;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import com.avaje.ebean.Ebean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import compornent.SessionCheckComponent;
import compornent.UserComprtnent;
import consts.AgryeelConst;

public class DashBoard extends Controller {

    /**
     * 【AGRYEEL】ダッシュボード
     * @return UI画面レンダー
     */
  @Security.Authenticated(SessionCheckComponent.class)
    public static Result move() {
        if (session(AgryeelConst.SessionKey.ACCOUNTID) == null) {
          return redirect("/move");
        }
        return ok(views.html.dashBoard.render());
    }
}
