package com.yf.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.servlet.http.HttpServletResponse;

// 启用Web安全配置
@EnableWebSecurity
// Web安全配置类，继承自WebSecurityConfigurerAdapter
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 配置HttpSecurity的安全策略
     *
     * @param http HttpSecurity对象，用于配置HTTP安全设置
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 配置请求的授权规则
        http
                .authorizeRequests()
                .antMatchers("/login", "/register").permitAll() // 允许所有人访问登录和注册页面
                .anyRequest().authenticated() // 其他所有请求都需要认证
                .and()
                .formLogin() // 表单登录配置
                .loginPage("/login") // 设置登录页面
                // 移除登录成功后的重定向，允许Spring Security自动处理认证成功的情况
                .permitAll() // 登录页面无需认证
                .and()
                .logout() // 注销配置
                .logoutSuccessHandler((request, response, authentication) -> { // 自定义注销成功处理器
                    // 在这里可以处理注销成功后的逻辑，例如记录日志或发送响应状态
                    // 注意：通常不需要显式重定向，Spring Security会根据配置自动处理
                })
                .invalidateHttpSession(true) // 注销时销毁session
                .deleteCookies("JSESSIONID") // 注销时删除cookie
                .and()
                .exceptionHandling() // 异常处理配置
                .authenticationEntryPoint((request, response, authException) -> { // 认证失败处理
                    // 在这里可以处理未认证用户的访问，例如返回401状态码
                    // 注意：通常不需要显式重定向，Spring Security会根据配置自动处理
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> { // 授权失败处理
                    // 在这里可以处理没有权限用户的访问，例如返回403状态码
                    // 注意：通常不需要显式重定向，Spring Security会根据配置自动处理
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                });
    }

    /**
     * 配置密码编码器
     *
     * @return 返回配置的密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用BCrypt算法进行密码编码
        return new BCryptPasswordEncoder();
    }

}
