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
        File file = new File("C:\\agryeel\\public\\.well-known\\pki-validation\\df488890b9ceb39e683e4a76a928f6d0.txt");
        return ok(file);
    }
}
