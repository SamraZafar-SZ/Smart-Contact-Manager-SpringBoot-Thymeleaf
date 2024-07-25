package com.smart.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		
		model.addAttribute("title","Register - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping(value="/do_register",method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user")User user, Model model, BindingResult result,HttpSession session) {
		
		try {
			if(result.hasErrors()) {
				System.out.println("ERROR"+result.toString());
				model.addAttribute("user",user);
				return "signup";
			}
		user.setRole("ROLE_USER");
		user.setEnabled(true);
		user.setImageurl("default.png");
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		User result1=this.userRepository.save(user);
		System.out.println("USER"+result1);
		model.addAttribute("user",new User());
		session.setAttribute("message", new Message("Successfully Registered","alert-success"));
		return "signup";
		}catch(Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("Something went wrong","alert-danger"));
			return "signup";
		}
	}
	@RequestMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title","Login Page");
		return "signin";	
	}
}
