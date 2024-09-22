package com.softserve.itacademy.controller;

import com.softserve.itacademy.dto.userDto.UpdateUserDto;
import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.service.UserService;
import com.softserve.itacademy.dto.userDto.CreateUserDto;
import com.softserve.itacademy.dto.userDto.UserDto;
import com.softserve.itacademy.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    // TODO: for admins only
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("user", new CreateUserDto());
        return "create-user";
    }

    // TODO: for admins only
    @PostMapping("/create")
    public String create(@Validated @ModelAttribute("user") User user, BindingResult result) {
        if (result.hasErrors()) {
            return "create-user";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.USER);
        User newUser = userService.create(user);
        return "redirect:/todos/all/users/" + newUser.getId();
    }

    // TODO: for admins and if requested info about current user
    @GetMapping("/{id}/read")
    public String read(@PathVariable long id, Model model) {
        User user = userService.readById(id);
        model.addAttribute("user", user);
        return "user-info";
    }

    // TODO: for admins and if requested info about current user
    @GetMapping("/{id}/update")
    public String update(@PathVariable long id, Model model) {
        User user = userService.readById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        return "update-user";
    }

    // TODO: for admins and if updating info about current user
    @PostMapping("/{id}/update")
    public String update(@PathVariable long id, Model model,
                         @Validated @ModelAttribute("user") UpdateUserDto updateUserDto, BindingResult result) {
        UserDto oldUser = userService.findByIdThrowing(id);

        if (result.hasErrors()) {
            updateUserDto.setRole(oldUser.getRole()); // fallback to the current role
            model.addAttribute("roles", UserRole.values());
            return "update-user";
        }

        userService.update(updateUserDto);
        return "redirect:/users/" + id + "/read";
    }

    // TODO: for admins or if deleting current user
    @GetMapping("/{id}/delete")
    public String delete(@PathVariable("id") long id) {
        User currentUser = userService.getCurrentUser();
        if (currentUser.getId() == id) {
            userService.delete(id);
            SecurityContextHolder.clearContext();
            return "redirect:/login";
        }
        userService.delete(id);
        return "redirect:/users/all";
    }

    // TODO: for admins only
    @GetMapping("/all")
    public String getAll(Model model) {
        model.addAttribute("users", userService.getAll());
        return "users-list";
    }
}
