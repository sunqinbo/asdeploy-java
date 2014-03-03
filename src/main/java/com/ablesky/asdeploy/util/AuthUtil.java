package com.ablesky.asdeploy.util;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import com.ablesky.asdeploy.pojo.User;

public class AuthUtil {
	
	private AuthUtil() {}
	
	public static Subject getCurrentSubject() {
		return SecurityUtils.getSubject();
	}
	
	/**
	 * 判断是否已登录
	 */
	public static boolean isAuthenticated() {
		return getCurrentSubject().isAuthenticated();
	}
	
	public static String getCurrentUsername() {
		return (String) getCurrentSubject().getPrincipal();
	}
	
	public static User getCurrentUser() {
		return getCurrentSubject().getPrincipals().byType(User.class).iterator().next();
	}
	
	public static void login(String username, String password) {
		UsernamePasswordToken token = new UsernamePasswordToken(username, password);
		getCurrentSubject().login(token);
	}
	
	public static void logout() {
		getCurrentSubject().logout();
	}

}
