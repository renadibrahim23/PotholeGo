package com.community.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.community.model.lookup.Role;
import com.community.model.user.Resident;
import com.community.model.user.User;
import com.community.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String SESSION_USER = "userId";

    private final UserRepository users;
    private final BCryptPasswordEncoder encoder;

    public AuthController(UserRepository users, BCryptPasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public record LoginRequest(String email, String password) {
    }

    public record RegisterRequest(String name, String email, String password) {
    }

    // what the UI is allowed to see about a user (never the password hash)
    public record UserInfo(int id, String name, String email, String role) {
        public static UserInfo from(User u) {
            return new UserInfo(u.getId(), u.getName(), u.getEmail(), u.getRole().getName());
        }
    }

    @PostMapping("/login")
    public UserInfo login(@RequestBody LoginRequest req, HttpSession session) {
        if (req.email() == null || req.password() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email and password are required");
        }
        User u = users.findByEmail(req.email())
                .filter(found -> encoder.matches(req.password(), found.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));
        session.setAttribute(SESSION_USER, u.getId());
        return UserInfo.from(u);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserInfo register(@RequestBody RegisterRequest req, HttpSession session) {
        if (req.name() == null || req.name().isBlank()
                || req.email() == null || !req.email().contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and a valid email are required");
        }
        if (req.password() == null || req.password().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
        }
        if (users.findByEmail(req.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        Resident donor = new Resident();
        donor.setName(req.name().trim());
        donor.setEmail(req.email().trim());
        donor.setPasswordHash(encoder.encode(req.password()));
        donor.setRole(users.role(Role.RESIDENT));
        users.save(donor);
        session.setAttribute(SESSION_USER, donor.getId());
        return UserInfo.from(donor);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        session.invalidate();
    }

    // 204 when nobody is logged in, so the landing page can load without an error
    @GetMapping("/me")
    public ResponseEntity<UserInfo> me(HttpSession session) {
        Object id = session.getAttribute(SESSION_USER);
        return id == null
                ? ResponseEntity.noContent().build()
                : users.findById((Integer) id).map(UserInfo::from)
                        .map(ResponseEntity::ok)
                        .orElse(ResponseEntity.noContent().build());
    }

    // ---- used by the other controllers ----

    public static User requireUser(HttpSession session, UserRepository users) {
        Object id = session.getAttribute(SESSION_USER);
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first");
        }
        return users.findById((Integer) id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in first"));
    }

    public static User requireAdmin(HttpSession session, UserRepository users) {
        User u = requireUser(session, users);
        if (!isAdmin(u)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admins only");
        }
        return u;
    }

    public static boolean isAdmin(User u) {
        return Role.ADMIN.equals(u.getRole().getName());
    }
}
