package cn.jbone.sso.common.exceptions;

/**
 * 用户无权登录
 */
public class AccountPermissionDeniedException extends RuntimeException {
    public AccountPermissionDeniedException(){super();}
    public AccountPermissionDeniedException(String message){
        super(message);
    }
}
