package batch;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import consts.AgryeelConst;

import batch.type.AveTime;

import models.Account;
import models.Farm;
import models.TimeLine;
import models.WorkDiary;

import play.Logger;
import play.api.Mode;
import play.api.Play;

/**
 * ソケットサーバクラス
 * @author kimura
 *
 */
public class SocketServer {

  /**
   * メイン処理
   * @param args
   */
  public static void main(String args[]) {
    Logger.info("---------- SocketServer START ----------");
    ServerSocket server;
    try {
      //----- WEB SOCKET START -----
      server = new ServerSocket(363);
      Socket client = server.accept();

      //----- STREAM GET -----
      InputStream in = client.getInputStream();
      OutputStream out = client.getOutputStream();

      //----- HAND SHAKING -----
      String data = new Scanner(in,"UTF-8").useDelimiter("\\r\\n\\r\\n").next();
      Matcher get = Pattern.compile("^GET").matcher(data);

      if (get.find()) {
        Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
        match.find();
        byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                + "Connection: Upgrade\r\n"
                + "Upgrade: websocket\r\n"
                + "Sec-WebSocket-Accept: "
                + DatatypeConverter
                .printBase64Binary(
                        MessageDigest
                        .getInstance("SHA-1")
                        .digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11")
                                .getBytes("UTF-8")))
                + "\r\n\r\n")
                .getBytes("UTF-8");

        out.write(response, 0, response.length);
    }
    } catch (IOException e) {
      Logger.error("SocketServerError:" + e.getMessage(), e);
      e.printStackTrace();
    } catch (NoSuchAlgorithmException e) {
      // TODO 自動生成された catch ブロック
      Logger.error("SocketServerError:" + e.getMessage(), e);
      e.printStackTrace();
    }
  }
}