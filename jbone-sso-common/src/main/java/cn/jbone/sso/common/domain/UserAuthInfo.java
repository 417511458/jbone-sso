package cn.jbone.sso.common.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户权限信息
 */
public class UserAuthInfo implements Serializable {


    private Set<String> permissions;
    private Set<String> roles;
    private Map<String,List<MenuInfo>> allMenus; //所有菜单,key=系统名，value=菜单
    private List<MenuInfo> menus;//当前系统菜单，在子系统中设置

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Map<String, List<MenuInfo>> getAllMenus() {
        return allMenus;
    }

    public void setAllMenus(Map<String, List<MenuInfo>> allMenus) {
        this.allMenus = allMenus;
    }

    public List<MenuInfo> getMenus() {
        return menus;
    }

    public void setMenus(List<MenuInfo> menus) {
        this.menus = menus;
    }
}
