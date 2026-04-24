package ru.kata.spring.boot_security.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleServiceImpl;
import ru.kata.spring.boot_security.demo.service.UserServiceImpl;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserServiceImpl userServiceImpl;
    private final RoleServiceImpl roleServiceImpl;

    public AdminController(UserServiceImpl userServiceImpl, RoleServiceImpl roleServiceImpl) {
        this.userServiceImpl = userServiceImpl;
        this.roleServiceImpl = roleServiceImpl;
    }

    @GetMapping
    public String getUsersPageForAdmin (@RequestParam(required = false) int id,
                                        Model model) {
        Optional<User> userOptional = userServiceImpl.findUserById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            model.addAttribute("user", user);
            model.addAttribute("allRoles", roleServiceImpl.findAll());
            return "admin";
        } else {
            return "redirect:/index";
        }
    }

    @PostMapping("/update")
    public String getAdminPage(@RequestParam int id,
                               @RequestParam String username,
                               @RequestParam(required = false) String password,
                               @RequestParam(required = false) List<String> roles,
                               Model model) {

        Collection<Role> newRoles = roles.stream()
                .map(role -> roleServiceImpl.findByName(role)
                        .orElseGet(() -> roleServiceImpl.save(new Role(role))))
                .collect(Collectors.toSet());
        userServiceImpl.updateUser(id, username, password, newRoles);
        return "redirect:/index";
    }

    @PostMapping("/add-user")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          Model model) {
        User user = new User(username, password);
        userServiceImpl.saveUser(user);
        model.addAttribute("user", user);
        return "redirect:/index";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam int id) {
        userServiceImpl.deleteUserById(id);
        return "redirect:/index";
    }
}
