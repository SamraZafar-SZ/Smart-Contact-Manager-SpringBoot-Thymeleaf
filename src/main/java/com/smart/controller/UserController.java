package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    // Method for adding common data to response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();
        System.out.println("Username " + userName);
        User user = userRepository.getUserByUsername(userName);
        System.out.println("User " + user);
        model.addAttribute("user", user);
    }

    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("title", "Dashboard");
        return "normal/user_dashboard";
    }

    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    // Processing add contact
    @PostMapping("/process-contact")
    public ResponseEntity<Map<String, Object>> processContact(@ModelAttribute Contact contact, Principal principal,
            @RequestParam("profileImage") MultipartFile file, HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            String name = principal.getName();
            User user = this.userRepository.getUserByUsername(name);

            if (file.isEmpty()) {
                System.out.println("Empty File");
                contact.setImage("default.png");
            } else {
                // Upload file to folder and update name in database
                contact.setImage(file.getOriginalFilename());
                File savedFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(savedFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Image is Uploaded");
            }

            contact.setUser(user);
            user.getContacts().add(contact);
            this.userRepository.save(user);

            System.out.println("Data " + contact);
            System.out.println("Added to DB");

            // Send success message to user
            session.setAttribute("message", new Message("Contact Successfully added", "success"));
            response.put("success", true);
            response.put("message", "Contact Successfully added");
        } catch (Exception e) {
            System.out.println("ERROR " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong. Please try again.", "danger"));
            response.put("success", false);
            response.put("message", "Something went wrong. Please try again.");
            response.put("error", e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "http://localhost:5174");
        headers.add("Access-Control-Allow-Credentials", "true");

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @GetMapping("/show-contacts/{page}")
    @CrossOrigin(origins = "http://localhost:5174")
    public ResponseEntity<Map<String, Object>> showContacts(@PathVariable("page") Integer page, Principal principal) {
        String userName = principal.getName();
        User user = userRepository.getUserByUsername(userName);

        PageRequest pageable = PageRequest.of(page, 4); // Assuming 4 items per page
        Page<Contact> contacts = contactRepository.findContactsByUser(user.getId(), pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("contacts", contacts.getContent());
        response.put("currentPage", contacts.getNumber());
        response.put("totalItems", contacts.getTotalElements());
        response.put("totalPages", contacts.getTotalPages());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "http://localhost:5174");
        headers.add("Access-Control-Allow-Credentials", "true");

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

    @GetMapping("/{cID}/contact")
    public ResponseEntity<Contact> getContact(@PathVariable("cID") Integer cID, Principal principal) {
        String userName = principal.getName();
        User user = this.userRepository.getUserByUsername(userName);
        Optional<Contact> contactOptional = this.contactRepository.findById(cID);
        Contact contact = contactOptional.orElseThrow(() -> new RuntimeException("Contact not found"));
        if (user.getId() == contact.getUser().getId()) {
            return ResponseEntity.ok(contact);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @GetMapping("/delete/{cID}")
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable("cID") Integer cID, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        try {
            Optional<Contact> contactOptional = this.contactRepository.findById(cID);
            Contact contact = contactOptional.orElseThrow(() -> new RuntimeException("Contact not found"));
            String userName = principal.getName();
            User user = this.userRepository.getUserByUsername(userName);
            if (user.getId()==(contact.getUser().getId())) {
                user.getContacts().remove(contact);
                this.userRepository.save(user);
                response.put("success", true);
                response.put("message", "Contact deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Unauthorized");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Something went wrong");
            response.put("error", e.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/update-contact/{cID}")
    public String updateForm(@PathVariable("cID") Integer cID, Model model) {
        model.addAttribute("title", "Update Form");
        Contact contact = this.contactRepository.findById(cID).get();
        model.addAttribute("contact", contact);
        return "normal/update_form";
    }

    @PostMapping("/process-update")
    public ResponseEntity<Map<String, Object>> updateContact(Principal principal, @ModelAttribute Contact contact,
            @RequestParam("profileImage") MultipartFile file, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Contact oldContact = this.contactRepository.findById(contact.getcID()).get();
            if (!file.isEmpty()) {
                // Delete old file
                if (oldContact.getImage() != null) {
                    File deleteFile = new ClassPathResource("static/img").getFile();
                    File file1 = new File(deleteFile, oldContact.getImage());
                    file1.delete();
                }

                // Update new image
                File savedFile = new ClassPathResource("static/img").getFile();
                Path path = Paths.get(savedFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                contact.setImage(file.getOriginalFilename());
            } else {
                contact.setImage(oldContact.getImage());
            }
            User user = this.userRepository.getUserByUsername(principal.getName());
            contact.setUser(user);
            this.contactRepository.save(contact);
            session.setAttribute("message", new Message("Updates Successfully", "success"));
            response.put("success", true);
            response.put("message", "Updates Successfully");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Something went wrong");
            response.put("error", e.getMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/profile")
    public String yourProfile(Model model) {
        model.addAttribute("title", "Profile Page");
        return "normal/profile";
    }

    // Open settings handler
    @GetMapping("/settings")
    public String openSettings() {
        return "normal/settings";
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(HttpSession session, Principal principal,
                                                              @RequestBody Map<String, String> passwordData) {
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        Map<String, Object> response = new HashMap<>();

        try {
            String username = principal.getName();
            User user = this.userRepository.getUserByUsername(username);

            if (this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
                this.userRepository.save(user);
                session.setAttribute("message", new Message("Password Changed", "success"));
                response.put("success", true);
                response.put("message", "Password Changed Successfully");
            } else {
                session.setAttribute("message", new Message("Wrong Password", "danger"));
                response.put("success", false);
                response.put("message", "Incorrect Old Password");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Something went wrong");
            response.put("error", e.getMessage());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", "http://localhost:5174");
        headers.add("Access-Control-Allow-Credentials", "true");

        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

}
