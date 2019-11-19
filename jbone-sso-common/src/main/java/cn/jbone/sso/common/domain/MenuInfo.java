package cn.jbone.sso.common.domain;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单信息
 */
public class MenuInfo implements Serializable ,Comparable<MenuInfo>{
    private static final long serialVersionUID = -8932497824864351438L;
    private int id;
    private Integer systemId;
    private Integer pid;
    private String name;
    private String url;
    private String target;
    private Long orders;
    private Integer version;
    private String icon;
    private List<MenuInfo> childMenus;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getSystemId() {
        return systemId;
    }

    public void setSystemId(Integer systemId) {
        this.systemId = systemId;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Long getOrders() {
        return orders;
    }

    public void setOrders(Long orders) {
        this.orders = orders;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<MenuInfo> getChildMenus() {
        return childMenus;
    }

    public void setChildMenus(List<MenuInfo> childMenus) {
        this.childMenus = childMenus;
    }

    @Override
    public int compareTo(MenuInfo o) {
        return o.orders.compareTo(this.orders);
    }
}
