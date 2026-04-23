package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class UserController {
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RoleRepository roleRepository;

    @Autowired

    public UserController(PasswordEncoder passwordEncoder, UserService userService, RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping("/user")
    public String getUsersPage(@RequestParam(required = false) int id, Model model) {
        Optional<User> userOptional = userService.findUserById(id);
        if (userOptional.isPresent()) {
            model.addAttribute("user", userOptional.get());
            return "user";
        } else {
            return "redirect:/index";
        }
    }

    @GetMapping("/admin")
    public String getUsersPageForAdmin (@RequestParam(required = false) int id,
                                        Model model) {
        Optional<User> userOptional = userService.findUserById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("user", user);
            model.addAttribute("allRoles", roleRepository.findAll());
            return "admin";
        } else {
            return "redirect:/index";
        }
    }

    @PostMapping("/admin/update")
    public String getAdminPage(@RequestParam int id,
                               @RequestParam String username,
                               @RequestParam(required = false) String password,
                               @RequestParam(required = false) List<String> roles,
                               Model model) {

        Collection<Role> newRoles = roles.stream()
                .map(role -> roleRepository.findByName(role)
                        .orElseGet(() -> roleRepository.save(new Role(role))))
                .collect(Collectors.toSet());

        String encodedPassword;
        if (password != null && !password.isEmpty()) {
            encodedPassword = passwordEncoder.encode(password);
        } else {
            Optional<User> user = userService.findUserById(id);
            encodedPassword = user.get().getPassword();
        }

        userService.updateUser(id, username, encodedPassword, newRoles);
        return "redirect:/index";
    }

    @PostMapping("/add-user")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          Model model) {
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(username, encodedPassword);
        userService.saveUser(user);
        model.addAttribute("user", user);
        return "redirect:/index";
    }

    @PostMapping("/user/delete")
    public String deleteUser(@RequestParam int id) {
        userService.deleteUserById(id);
        return "redirect:/index";
    }


}
