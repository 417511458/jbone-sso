package cn.jbone.sso.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestServiceId {
    public static void main(String[] args) {
        Pattern pattern = Pattern.compile("^http://sysadmin\\.local\\.jbone\\.cn");
        Matcher matcher = pattern.matcher("http://sysadmin.local.jbone.cn");
        if(matcher.find()) {
            System.out.println(matcher.group(0));  //匹配到的整个结果

        }
    }
}
