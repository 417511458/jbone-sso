package cn.jbone.sso.common.exceptions;

/**
 * 密码错误异常
 */
public class AccountInvalidPasswordException extends RuntimeException {
    public AccountInvalidPasswordException(){super();}
    public AccountInvalidPasswordException(String message){
        super(message);
    }
}
