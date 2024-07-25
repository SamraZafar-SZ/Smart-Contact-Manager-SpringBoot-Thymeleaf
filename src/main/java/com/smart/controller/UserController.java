package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName=principal.getName();
		System.out.println("Username "+userName);
		User user=userRepository.getUserByUsername(userName);
		System.out.println("User "+user);
		model.addAttribute("user",user);
	}
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		model.addAttribute("title","Dashboard");
		return "normal/user_dashboard";
	}
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	//processing add contact
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, Principal principal,@RequestParam("profileImage") MultipartFile file, HttpSession session) {
		
		try {
		String name=principal.getName();
		User user=this.userRepository.getUserByUsername(name);
		
		if(file.isEmpty()) {
			System.out.println("Empty File");
			contact.setImage("default.png");
		}else {
			//upload file to folder and update name in database
			contact.setImage(file.getOriginalFilename());
			File savedFile=new ClassPathResource("static/img").getFile();
			Path path=Paths.get(savedFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
		    Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
		    System.out.println("Image is Uploaded");
		}
		
		contact.setUser(user);
		user.getContacts().add(contact);
		this.userRepository.save(user);
		
		System.out.println("Data "+contact);
		System.out.println("Added to DB");
		
		//send success message to user
		 session.setAttribute("message",new Message("Contact Successfully added","success"));
		}
		catch(Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			 session.setAttribute("message",new Message("Something went wron. Please try again.","danger"));
		}
		return "normal/add_contact_form";
	}
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page,Model model,Principal principal) {
		model.addAttribute("title","User Contacts");
		//send contact list from here
		String userName=principal.getName();
		User user=this.userRepository.getUserByUsername(userName);
		
		PageRequest pageable=PageRequest.of(page,4);
		Page<Contact> contacts=this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		return "normal/show_contacts";
	}
	
	@RequestMapping("/{cID}/contact")
	public String showContactDetail(@PathVariable("cID")Integer cID,Model model,Principal principal) {
		
		System.out.println("CID "+cID);
		Optional<Contact> contactOptional=this.contactRepository.findById(cID);
		Contact contact=contactOptional.get();
		String userName=principal.getName();
		User user=this.userRepository.getUserByUsername(userName);
		
		if(user.getId()==contact.getUser().getId()) {
		model.addAttribute("contact",contact);
		}
		return "normal/contact_detail";
	}
	
	@GetMapping("/delete/{cID}")
	public String deleteContact(@PathVariable("cID")Integer cID,Model model,Principal principal,HttpSession session) {
		Optional<Contact> contactOptional=this.contactRepository.findById(cID);
		Contact contact=contactOptional.get();
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByUsername(userName);
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		session.setAttribute("message", new Message("Contact deleted successfully","success"));
		return "redirect:/user/show-contacts/0";
		//}	
	}
	
	@PostMapping("/update-contact/{cID}")
	public String updateForm(@PathVariable("cID")Integer cID,Model model) {
		model.addAttribute("title","Update Form");
		Contact contact=this.contactRepository.findById(cID).get();
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	
	@RequestMapping(value="/process-update", method=RequestMethod.POST)
	public String updateHandler(Principal principal,Model model,@ModelAttribute Contact contact,@RequestParam("profileImage")MultipartFile file,HttpSession session) {
		
		try {
			Contact oldContact=this.contactRepository.findById(contact.getcID()).get();
			if(!file.isEmpty()) {
				//delete old file
				if(oldContact.getImage()!=null) {
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldContact.getImage());
				file1.delete();}
				
				//update new image
				File savedFile=new ClassPathResource("static/img").getFile();
				Path path=Paths.get(savedFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			    Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(oldContact.getImage());
			}
			User user=this.userRepository.getUserByUsername(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Updates Successfully","success"));
		}catch(Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcID()+"/contact";
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(HttpSession session,Principal principal,@RequestParam("oldPassword")String oldPassword,@RequestParam("newPassword")String newPassword) {
		System.out.println(oldPassword);
		System.out.println(newPassword);
		String username=principal.getName();
		User user=this.userRepository.getUserByUsername(username);
		if(this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Password Changed","success"));
		}else {
			session.setAttribute("message", new Message("Wrong Password","danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
	}
}
