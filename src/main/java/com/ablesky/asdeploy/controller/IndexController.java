package com.ablesky.asdeploy.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ablesky.asdeploy.service.IDeployService;
import com.ablesky.asdeploy.service.IUserService;
import com.ablesky.asdeploy.util.AuthUtil;
import com.ablesky.asdeploy.util.ImageUtil;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

@Controller
public class IndexController {
	
	@Autowired
	private IUserService userService;

	@Autowired
	private IDeployService deployService;
	
	@RequestMapping({ "/", "/main"})
	public String main(Model model) {
		model.addAttribute("deployLock", deployService.checkCurrentLock())
			.addAttribute("currentUser", AuthUtil.getCurrentUser())
			.addAttribute("isSuperAdmin", AuthUtil.isSuperAdmin());
		return "main";
	}
	
	/**
	 * 登录页面
	 */
	@RequestMapping(value="/login", method=RequestMethod.GET)
	public String login() {
		return "login";
	}
	
	/**
	 * 注册成功后，重定向到登录页面
	 */
	@RequestMapping(value="/login/{msg}", method=RequestMethod.GET)
	public String login(@PathVariable("msg") String msg, Model model) {
		if("registerSuccess".equals(msg)) {
			model.addAttribute("successMessage", "注册成功, 请登录!");
		}
		return "login";
	}
	
	/**
	 * 登录操作(实际的登录操作在FormAuthenticationFilter中进行)
	 * 只有登录失败后，才会进入到此方法中
	 */
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public String loginFailed(String username, String password, Model model) {
		model.addAttribute("errorMessage", "用户名或密码错误，请重试!");
		return "login";
	}
	
	@RequestMapping(value="/register", method=RequestMethod.GET)
	public String register() {
		return "register";
	}
	
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public String register(String username, String password, String confirmedPassword, Model model) {
		ModelMap validateResult = new ModelMap();
		if(StringUtils.isBlank(username)) {
			validateResult.addAttribute("usernameError", "用户名不能为空!");
		} else if(userService.getUserByUsername(username) != null) {
			validateResult.addAttribute("usernameError", "用户名已被占用!");
		}
		if(StringUtils.isBlank(password)) {
			validateResult.addAttribute("passwordError", "密码不能为空!");
		} else if (!password.equals(confirmedPassword)) {
			validateResult.addAttribute("confirmedPasswordError", "两次输入的密码不一致!");
		}
		if(validateResult.size() > 0) {
			model.addAllAttributes(validateResult);
			return "register";
		}
		userService.createNewUser(username, password);
		AuthUtil.login(username, password, true);
		return "redirect:/main";
	}
	
	@RequestMapping("/register/verifyImage")
	public void getVerifyImage(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("image/jpeg");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 2000);

		String verifyCode = this.getRandomString(4);
		request.getSession().setAttribute("registerVerifyCode", verifyCode);
		ServletOutputStream outStream = null;
		try {
			outStream = response.getOutputStream();
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(outStream);
			encoder.encode(ImageUtil.generateTextImage(18, 25, verifyCode));
			outStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(outStream);
		}
	}
	
	private String getRandomString(int num) {
		if(num < 1 || num > 32) {
			throw new IllegalArgumentException("The lenght of random string should neither bigger than 32 nor less then 1!");
		}
		return UUID.randomUUID().toString().replaceAll("-", "").substring(0, num);
	}
	
	@RequestMapping("/unauthorized")
	public String unauthorized() {
		return "unauthorized";
	}
	
}
