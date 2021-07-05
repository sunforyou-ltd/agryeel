package util;

import java.sql.Date;
import java.text.SimpleDateFormat;

/**
 * 【AGRYEEL】文字列操作ユーティリティ
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class StringU {
    /**
     * 文字列がNULL場合、空文字に変換して文字列を戻します。<br>
     * また通常文字列の場合、前後の半角スペースを削除して文字列を戻します。
     * @param value 対象文字列
     * @return 変換後文字列
     */
    public static String setNullTrim(String value) {

        String sResult = "";			//戻り値文字列

        if (value != null) {			//対象文字列がNULL以外の場合
            sResult = value.trim();		//前後の半角スペースを削除する
        }

        return sResult;


    }

    /**
     * 渡された日付を指定されたパターンの文字列に変換する
     * @param pattern 指定パターン
     * @param date    変換元日付
     * @return        日付文字列
     */
    public static String dateFormat(String pattern, Date date) {
        String 				sResult 	= "";									//戻り値文字列
        SimpleDateFormat	sdf 		= new SimpleDateFormat(pattern);		//日付フォーマット

        if (date != null) {				//対象日付がNULL以外の場合

            sResult = sdf.format(date);

        }

        return sResult;

    }

    public static String toWideNumber(String number) {

	    StringBuffer sb = new StringBuffer(number);
	    for (int i = 0; i < number.length(); i++) {

	        char c = number.charAt(i);
	        if (c >= '0' && c <= '9') {

	            sb.setCharAt(i, (char)(c - '0' + '０'));
	        }
	    }

	    return sb.toString();

    }

    public static boolean nullcheck(String pText) {
      boolean result = false;
      if (pText == null || "".equals(pText)) { // Null OR Empty
        result = true;
      }
      return result;
    }
    public static String zenkakuAlphabetToHankaku(String s) {
      StringBuffer sb = new StringBuffer(s);
      for (int i = 0; i < sb.length(); i++) {
        char c = sb.charAt(i);
        if (c >= 'ａ' && c <= 'ｚ') {
          sb.setCharAt(i, (char) (c - 'ａ' + 'a'));
        } else if (c >= 'Ａ' && c <= 'Ｚ') {
          sb.setCharAt(i, (char) (c - 'Ａ' + 'A'));
        }
      }
      return sb.toString();
    }
    public static String hankakuAlphabetToZenkakuAlphabet(String s) {
      StringBuffer sb = new StringBuffer(s);
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c >= 'a' && c <= 'z') {
          sb.setCharAt(i, (char)(c - 'a' + 'ａ'));
        } else if (c >= 'A' && c <= 'Z') {
          sb.setCharAt(i, (char)(c - 'A' + 'Ａ'));
        }
      }
      return sb.toString();
    }
    public static String hankakuNumberToZenkakuNumber(String s) {
      StringBuffer sb = new StringBuffer(s);
      for (int i = 0; i < s.length(); i++) {
        char c = s.charAt(i);
        if (c >= '0' && c <= '9') {
          sb.setCharAt(i, (char) (c - '0' + '０'));
        }
      }
      return sb.toString();
    }
    public static String zenkakuNumToHankaku(String s) {
      StringBuffer sb = new StringBuffer(s);
      for (int i = 0; i < sb.length(); i++) {
        char c = sb.charAt(i);
        if (c >= '０' && c <= '９') {
          sb.setCharAt(i, (char)(c - '０' + '0'));
        }
      }
      return sb.toString();
    }
}
