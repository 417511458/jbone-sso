package cn.jbone.sso.client;

import cn.jbone.configuration.JboneConfiguration;
import cn.jbone.sso.client.listener.JboneCasSessionListener;
import cn.jbone.sso.client.pac4j.handler.JboneCasLogoutHandler;
import cn.jbone.sso.client.realm.JboneCasRealm;
import cn.jbone.sso.client.session.JboneCasSessionDao;
import cn.jbone.sso.client.session.JboneCasSessionFactory;
import cn.jbone.sso.client.session.JboneSessionTicketStore;
import io.buji.pac4j.context.ShiroSessionStore;
import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.LogoutFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.subject.Pac4jSubjectFactory;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionFactory;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.*;

/**
 * Shiro集成Cas配置
 *
 */
@Configuration
public class ShiroCasConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ShiroCasConfiguration.class);

    @Bean
    Config getConfig(JboneConfiguration jboneConfiguration, ShiroSessionStore sessionStore, JboneCasLogoutHandler jboneCasLogoutHandler){
        CasConfiguration casConfiguration = new CasConfiguration(jboneConfiguration.getSso().getSsoServerUrl()+jboneConfiguration.getSso().getLoginUrl(), jboneConfiguration.getSso().getSsoServerUrl());
        casConfiguration.setLogoutHandler(jboneCasLogoutHandler);
        casConfiguration.setAcceptAnyProxy(true);
        casConfiguration.setLoginUrl(jboneConfiguration.getSso().getSsoServerUrl() + jboneConfiguration.getSso().getLoginUrl());
//        casConfiguration.setProtocol(CasProtocol.CAS20); //注意：2。0协议不支持扩展属性传递
        casConfiguration.setPrefixUrl(jboneConfiguration.getSso().getSsoServerUrl() + "/");

        CasClient casClient = new CasClient(casConfiguration);
        casClient.setCallbackUrl(jboneConfiguration.getSso().getCurrentServerUrlPrefix() + jboneConfiguration.getSso().getSsoFilterUrlPattern() + "?client_name=CasClient");
        casClient.setName("CasClient");

        Clients clients = new Clients(jboneConfiguration.getSso().getCurrentServerUrlPrefix() + jboneConfiguration.getSso().getSsoFilterUrlPattern() + "?client_name=CasClient", casClient);
        Config config = new Config(clients);
        config.setSessionStore(sessionStore);

        return config;
    }

    @Bean
    ShiroSessionStore getJboneSessionStore(){
        return new ShiroSessionStore();
    }

    @Bean
    JboneSessionTicketStore getSessionTicketStore(StringRedisTemplate stringRedisTemplate, JboneConfiguration jboneConfiguration){
        JboneSessionTicketStore sessionTicketStore = new JboneSessionTicketStore();
        sessionTicketStore.setRedisTemplate(stringRedisTemplate);
        sessionTicketStore.setTimeout(jboneConfiguration.getSso().getClientSessionTimeout());
        return sessionTicketStore;
    }

    @Bean
    JboneCasLogoutHandler getJboneCasLogoutHandler(SessionManager sessionManager,JboneSessionTicketStore sessionTicketStore){
        JboneCasLogoutHandler handler = new JboneCasLogoutHandler(sessionTicketStore);
        handler.setDestroySession(true);
        handler.setSessionManager(sessionManager);
        return handler;
    }

    @Bean
    public JboneCasRealm getJboneCasRealm(EhCacheManager ehCacheManager, JboneConfiguration jboneConfiguration){
        JboneCasRealm realm = new JboneCasRealm(ehCacheManager, jboneConfiguration.getSys().getServerName());
        return realm;
    }

    @Bean
    public EhCacheManager getEhCacheManager() {
        EhCacheManager em = new EhCacheManager();
        em.setCacheManagerConfigFile("classpath:ehcache-shiro.xml");
        return em;
    }

    /**
     * 注册DelegatingFilterProxy（Shiro）
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new DelegatingFilterProxy("shiroFilter"));
        //  该值缺省为false,表示生命周期由SpringApplicationContext管理,设置为true则表示由ServletContainer管理
        filterRegistration.addInitParameter("targetFilterLifecycle", "true");
        filterRegistration.setEnabled(true);
        filterRegistration.addUrlPatterns("/*");
        filterRegistration.setDispatcherTypes(EnumSet.allOf(DispatcherType.class));

        return filterRegistration;
    }

    @Bean(name = "lifecycleBeanPostProcessor")
    public LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator daap = new DefaultAdvisorAutoProxyCreator();
        daap.setProxyTargetClass(true);
        return daap;
    }

    @Bean(name = "securityManager")
    public DefaultWebSecurityManager getDefaultWebSecurityManager(JboneCasRealm jboneCasRealm,DefaultWebSessionManager sessionManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(jboneCasRealm);
        //用户授权/认证信息Cache, 采用EhCache 缓存
        securityManager.setCacheManager(getEhCacheManager());
        securityManager.setSubjectFactory(new Pac4jSubjectFactory());
        securityManager.setSessionManager(sessionManager);

        return securityManager;
    }

    @Bean(name = "sessionManager")
    public DefaultWebSessionManager getDefaultWebSessionManager(SessionListener sessionListener, SessionDAO sessionDao, SessionFactory sessionFactory, JboneConfiguration jboneConfiguration, SimpleCookie cookie){
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setGlobalSessionTimeout(jboneConfiguration.getSso().getClientSessionTimeout());
        sessionManager.setSessionValidationSchedulerEnabled(false);
        sessionManager.setSessionListeners(Arrays.asList(sessionListener));
        sessionManager.setSessionDAO(sessionDao);
        sessionManager.setSessionFactory(sessionFactory);
        sessionManager.setSessionIdCookie(cookie);
        return sessionManager;
    }

    @Bean(name = "sessionDao")
    public SessionDAO getSessionDao(StringRedisTemplate redisTemplate,JboneSessionTicketStore sessionTicketStore){
        JboneCasSessionDao sessionDao = new JboneCasSessionDao(redisTemplate);
        sessionDao.setSessionTicketStore(sessionTicketStore);
        return sessionDao;
    }

    @Bean
    public SimpleCookie getCookie(){
        SimpleCookie cookie = new SimpleCookie();
        cookie.setName("j_s_id");
        cookie.setHttpOnly(false);
        cookie.setPath("/");
        return cookie;
    }


    @Bean(name = "sessionListener")
    public SessionListener getSessionListener(){
        return new JboneCasSessionListener();
    }

    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory(JboneConfiguration jboneConfiguration){
        return new JboneCasSessionFactory(jboneConfiguration);
    }

    @Bean
    public AuthorizationAttributeSourceAdvisor getAuthorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    /**
     * 加载shiroFilter权限控制规则
     */
    private void loadShiroFilterChain(ShiroFilterFactoryBean shiroFilterFactoryBean, JboneConfiguration jboneConfiguration){
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String,String>();

        filterChainDefinitionMap.put(jboneConfiguration.getSso().getSsoFilterUrlPattern(), "callback");// shiro集成cas后，首先添加该规则
        filterChainDefinitionMap.put(jboneConfiguration.getSso().getLogoutUrl(),"logout");

        //添加jbone.cas的配置规则
        if(jboneConfiguration.getSso().getFilterChainDefinition() != null){
            filterChainDefinitionMap.putAll(jboneConfiguration.getSso().getFilterChainDefinition());
        }

        //如果配置了/**，则优先自定义的过滤规则，如果没有配置，则全部由cas过滤
        String common = filterChainDefinitionMap.get("/**");
        if(StringUtils.isEmpty(common)){
            filterChainDefinitionMap.put("/**", "security");
        }else if(!common.equals("anon")){
            filterChainDefinitionMap.put("/**", "security," + common);
        }

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
    }

    /**
     * 过滤器
     * @param securityManager ShiroCasConfiguration.
     * @param jboneConfiguration jboneConfiguration
     * @param redisTemplate redisTemplate
     * @param sessionManager sessionManager
     * @param config config
     * @return shiroFilter
     */
    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(DefaultWebSecurityManager securityManager, JboneConfiguration jboneConfiguration, StringRedisTemplate redisTemplate, DefaultWebSessionManager sessionManager, Config config) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

        // SecurityManager，Shiro安全管理器
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        // Shiro登录页面，这里默认为CAS的登录页面：/login?service=serviceurl
        shiroFilterFactoryBean.setLoginUrl(jboneConfiguration.getSso().getEncodedLoginUrl());
        shiroFilterFactoryBean.setSuccessUrl(jboneConfiguration.getSso().getSuccessUrl());
        shiroFilterFactoryBean.setUnauthorizedUrl(jboneConfiguration.getSso().getUnauthorizedUrl());

        // 添加casFilter到shiroFilter中
        Map<String, Filter> filters = new HashMap<>();
        CallbackFilter callbackFilter = new CallbackFilter();
        callbackFilter.setConfig(config);
        callbackFilter.setDefaultUrl(jboneConfiguration.getSso().getSuccessUrl());
        filters.put("callback", callbackFilter);

        LogoutFilter logoutFilter = new LogoutFilter();
        logoutFilter.setConfig(config);
        logoutFilter.setDefaultUrl(jboneConfiguration.getSso().getCurrentServerUrlPrefix() + jboneConfiguration.getSso().getSsoFilterUrlPattern() + "?client_name=CasClient");
        logoutFilter.setCentralLogout(true);
        logoutFilter.setLocalLogout(true);//销毁本地
        filters.put("logout", logoutFilter);

        SecurityFilter securityFilter = new SecurityFilter();
        securityFilter.setConfig(config);
        securityFilter.setClients("CasClient");
        filters.put("security", securityFilter);

        shiroFilterFactoryBean.setFilters(filters);

        loadShiroFilterChain(shiroFilterFactoryBean, jboneConfiguration);
        return shiroFilterFactoryBean;
    }

}
