package com.hireready.hireready.controller;

import com.hireready.hireready.dto.request.LoginRequest;
import com.hireready.hireready.dto.request.RegisterRequest;
import com.hireready.hireready.dto.response.AuthResponse;
import com.hireready.hireready.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @RestController = @Controller + @ResponseBody combined.
// Marks this class as a Spring MVC controller where every method automatically
// serializes its return value to JSON and writes it directly to the HTTP response body.
// Without this, Spring wouldn't know to treat this class as a web endpoint handler.
@RestController

// Sets the base URL prefix for every method in this class.
// All endpoints here will start with /api/auth — each method then appends its own path.
// Keeps the full path DRY: you write /api/auth once instead of on every method.
@RequestMapping("/api/auth")
public class AuthController {

    // final ensures authService can't be reassigned after construction.
    // Best practice with constructor injection — makes the dependency explicit and immutable.
    private final AuthService authService;

    // Constructor injection: Spring sees this constructor and automatically provides
    // the AuthService bean when creating this controller.
    // Preferred over @Autowired field injection because it makes dependencies visible,
    // allows the field to be final, and makes the class easier to test.
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    // @PostMapping("/register") maps HTTP POST requests to /api/auth/register to this method.
    // POST is used here (not GET) because the client is sending data in the request body.
    //
    // @RequestBody tells Spring to deserialize the incoming JSON request body into a
    // RegisterRequest object. Without this, the parameter would be null.
    //
    // @Valid triggers Jakarta Bean Validation on the RegisterRequest object.
    // This runs any validation annotations on RegisterRequest fields (e.g. @NotBlank, @Email)
    // before the method body executes. If validation fails, Spring returns a 400 Bad Request
    // automatically — the method never runs.
    //
    // ResponseEntity<AuthResponse> wraps the response so we control the HTTP status code.
    // ResponseEntity.ok() sets status 200 and puts the AuthResponse as the body.
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    // @PostMapping("/login") maps HTTP POST requests to /api/auth/login to this method.
    // POST is used because the client is sending credentials in the request body —
    // never in the URL, since that would expose them in server logs and browser history.
    //
    // @RequestBody deserializes the incoming JSON into a LoginRequest object.
    // Spring uses Jackson under the hood to map JSON field names to Java field names.
    //
    // @Valid runs validation on LoginRequest fields before the method executes.
    // If the email is blank or password is missing, Spring rejects it with 400 Bad Request.
    //
    // ResponseEntity<AuthResponse>: on success, returns 200 OK with the JWT token in the body.
    // The <AuthResponse> generic tells the compiler what type the body contains — type safety only,
    // no runtime effect.
    //
    // ResponseEntity.ok() is shorthand for ResponseEntity.status(200).body(...).
    // Using ResponseEntity gives us the flexibility to return different status codes
    // (e.g. 401 Unauthorized) in other scenarios without changing the method signature.
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }
}
