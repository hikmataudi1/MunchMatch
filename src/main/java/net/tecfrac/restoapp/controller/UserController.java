package net.tecfrac.restoapp.controller;
import lombok.AllArgsConstructor;
import net.tecfrac.restoapp.dto.UserDto;
import net.tecfrac.restoapp.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok().body(users);
    }
    @GetMapping("{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok().body(user);
    }
    @PostMapping("/ids")
    public ResponseEntity<List<UserDto>> getUsersByIds(@RequestBody List<Long> ids) {
        List<UserDto> users = userService.getUsersByIds(ids);
        return ResponseEntity.ok(users);
    }
    @PutMapping
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userUpdated, Principal principal){
        UserDto updatedUser = userService.updateUser(userUpdated.getProfileImageUrl(),principal);
        return ResponseEntity.ok().body(updatedUser);
    }
}
