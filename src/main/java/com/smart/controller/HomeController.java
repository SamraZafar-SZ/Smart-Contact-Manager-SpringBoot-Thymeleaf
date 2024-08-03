package com.smart.controller;

import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
	
	@PostMapping("/do_register")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> registerUser(@RequestBody @Valid User user, HttpSession session) {
	    System.out.println(user);
		Map<String, Object> response = new HashMap<>();
	    try {
	        if (userRepository.existsByEmail(user.getEmail())) {
	            response.put("success", false);
	            response.put("message", "Email already exists");
	            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	        }

	        user.setRole("ROLE_USER");
	        user.setEnabled(true);
	        user.setImageurl("default.png");
	        user.setPassword(passwordEncoder.encode(user.getPassword()));
	        User result = userRepository.save(user);
	        session.setAttribute("message", new Message("Successfully Registered", "alert-success"));
	        response.put("success", true);
	        response.put("message", "Successfully Registered");
	        response.put("user", result);
	        return ResponseEntity.ok(response);
	    } catch (Exception e) {
	        response.put("success", false);
	        response.put("message", "Something went wrong");
	        response.put("error", e.getMessage());
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	    }
	}


	@RequestMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title","Login Page");
		return "signin";	
	}
	
	@PostMapping("/do_login")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> doLogin(@RequestBody Map<String, String> loginUser, HttpSession session) {
        System.out.println(loginUser);
		Map<String, Object> response = new HashMap<>();
        try {
            String username = loginUser.get("username");
            String password = loginUser.get("password");

            User user = userRepository.getUserByUsername(username);
            if (user != null && passwordEncoder.matches(password, user.getPassword())) {
                session.setAttribute("user", user);
                response.put("success", true);
                response.put("message", "Login successful");
                response.put("user", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid username or password");
                response.put("user", null);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Something went wrong");
            response.put("error", e.getMessage());
            response.put("user", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
