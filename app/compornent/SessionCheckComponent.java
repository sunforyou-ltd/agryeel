package compornent;

import models.Account;
import consts.AgryeelConst;
import play.Logger;
import play.cache.Cache;
import play.mvc.Http.Context;
import play.mvc.Http.Cookie;
import play.mvc.Http.Request;
import play.mvc.Result;
import play.mvc.Security;

/**
 * 【TOKYOTIMER】セッションチェックコンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class SessionCheckComponent extends Security.Authenticator {

	/**
	 * 未認証時の処理
	 */
	@Override
    public Result onUnauthorized(Context httpContext)
    {

        return redirect("/move");

    }

	/**
	 * ユーザ情報取得処理
	 */
    @Override
    public String getUsername(Context httpContext)
    {

        final Cookie userCookie = httpContext.request().cookie(AgryeelConst.SessionCheck.AppKeyCode);		/* クッキー情報を取得する */

        if (userCookie == null) return null;																/* ログイン情報が存在しない場合, NULLを返す */

        final String 	userToken 	= userCookie.value();													/* クッキー情報よりトークンを取得する */
        final Account 	userInfo 	= (Account)Cache.get(userToken + ".userInfo");							/* トークンより保存しているユーザ情報を取得する */

        if (userInfo == null) {

        	httpContext.response().discardCookie(AgryeelConst.SessionCheck.AppKeyCode); 					/* 既にキャッシュ上から削除されている為、クッキー情報を削除する */
          Logger.info("[ SESSION TIMEOUT ] TOKEN:{}", userToken);
          return null;                                                                          /* ログイン情報が存在しない為, NULLを返す */

        }

        // アクセスのたびにログイン情報登録をリフレッシュする
        registerLoginSession(httpContext, userToken, userInfo);

        return String.valueOf(userInfo.accountId);
    }

    /**
     * ログインセッション作成処理
     * @param context  		コンテキスト情報
     * @param userToken		ユーザトークン
     * @param Account		ユーザ情報
     */
    public static void registerLoginSession(Context httpContext, String userToken, Account userInfo)
    {

        Cache.set(userToken + ".userInfo", userInfo, AgryeelConst.SessionCheck.CacheLimitTime);				/* キャッシュ有効期限を設定 */
        httpContext.response().setCookie(AgryeelConst.SessionCheck.AppKeyCode
        		, userToken, AgryeelConst.SessionCheck.LoginCookieLimitTime);								/* ログインクッキー情報を設定 */

    }

    /**
     * ログインセッション削除処理
     * @param context		コンテキスト情報
     */
    public static void unregisterLoginSession(Context httpContext)
    {

        final Cookie userCookie = httpContext.request().cookie(AgryeelConst.SessionCheck.AppKeyCode);		/* クッキー情報を取得する */

        if (userCookie == null) return;																		/* ログイン情報が存在しない場合, 何も処理を行わない */

        Cache.remove(userCookie.value() + ".userInfo");														/* キャッシュ情報を削除する */
        httpContext.response().discardCookie(AgryeelConst.SessionCheck.AppKeyCode);							/* クッキー情報を削除する */

    }

	/**
	 * セッション上に格納しているユーザ情報を取得する
	 * @param httpContext コンテキスト情報
	 * @return ユーザ情報
	 */
    public static Account getUserInfoFromSession(Request request)
    {


        final Cookie userCookie = request.cookie(AgryeelConst.SessionCheck.AppKeyCode);						/* クッキー情報を取得する */

        if (userCookie == null) return null;																/* ログイン情報が存在しない場合, NULLを返す */

        final String 	userToken 	= userCookie.value();													/* クッキー情報よりトークンを取得する */
        final Account 	userInfo 	= (Account)Cache.get(userToken + ".userInfo");							/* トークンより保存しているユーザ情報を取得する */

        if (userInfo == null) {

            return null;																					/* ログイン情報が存在しない為, NULLを返す */

        }

        return userInfo;
    }


}
