package controllers;

import java.io.File;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class UI extends Controller {

    /**
     * 【AGRYEEL】UIテスト画面遷移
     * @return UI画面レンダー
     */
    public static Result move() {
        return ok(views.html.ui.render());
    }
    public static Result sslserverfile() {
        File file = new File("C:\\agryeel\\public\\.well-known\\pki-validation\\858dc8e19b6bb937be31010814de08f3.txt");
        return ok(file);
    }
}
