package likelion.sajaboys.soboonsoboon.controller;

import jakarta.validation.Valid;
import likelion.sajaboys.soboonsoboon.domain.User;
import likelion.sajaboys.soboonsoboon.dto.UserDtos;
import likelion.sajaboys.soboonsoboon.repository.UserRepository;
import likelion.sajaboys.soboonsoboon.util.ApiException;
import likelion.sajaboys.soboonsoboon.util.ApiSuccess;
import likelion.sajaboys.soboonsoboon.util.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final UserRepository userRepo;

    public UsersController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping
    public ResponseEntity<ApiSuccess<UserDtos.Response>> create(@Valid @RequestBody UserDtos.CreateRequest req) {
        if (userRepo.existsByUsername(req.username())) {
            throw new ApiException(ErrorCode.CONFLICT, "username already exists");
        }
        User u = User.builder().username(req.username()).build();
        User saved = userRepo.save(u);
        var res = new UserDtos.Response(saved.getId(), saved.getUsername(), saved.getCreatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccess.of(res));
    }

    @GetMapping("/{id}")
    public ApiSuccess<UserDtos.Response> get(@PathVariable Long id) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, "user not found"));
        return ApiSuccess.of(new UserDtos.Response(u.getId(), u.getUsername(), u.getCreatedAt()));
    }
}
