package compornent;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import play.Logger;

import consts.AgryeelConst;

/**
 * 【AGRYEEL】メールコンポーネント
 *
 * @author SUN FOR YOU.Ltd
 *
 */
public class MailComprtnent {

	/** 送信成功 */
	public static final int 	SEND_SUCCESS	= 0;
	/** 送信失敗 */
	public static final int 	SEND_ERROR	= -1;

	/**
	 * コンストラクタ
	 */
	public MailComprtnent() {


	}

	public static int send(String toMailAddress, String subject, String message) {

        SimpleEmail mailer 	= new SimpleEmail();
        int			iResult	= SEND_SUCCESS;
        try {

            mailer.setCharset(AgryeelConst.MailInfo.CHARSET);
            mailer.setHostName(AgryeelConst.MailInfo.HOSTNAME);
            mailer.setSmtpPort(AgryeelConst.MailInfo.PORT);
            mailer.setSSL(AgryeelConst.MailInfo.SSL);
            mailer.setAuthentication(AgryeelConst.MailInfo.AUTHUSER, AgryeelConst.MailInfo.AUTHPASSWORD);
            mailer.setFrom(AgryeelConst.MailInfo.FROMADDRESS);
            mailer.setMsg(message);
            mailer.setSubject(subject);
            mailer.addTo(toMailAddress);
            mailer.send();

        } catch(EmailException e) {

            Logger.error(e.toString(), e);
            iResult	= SEND_ERROR;

        }

        return iResult;

	}

}
