package fi.jyu.issuetracker.controllers;

import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import fi.jyu.issuetracker.dao.models.LoginRequest;
import fi.jyu.issuetracker.security.JwtTokenProvider;
import fi.jyu.issuetracker.security.model.JwtAuthenticationResponse;

/**
 * Service for applications login functionality.
 */
@RestController
public class LoginService {


	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JwtTokenProvider tokenProvider;
	
	/**
	 * Authenticates user with username and password.
	 * @param loginRequest request containing username and password.
	 * @return Authentication response containing JWT token, if authentication succeeds.
	 */
	@PostMapping("/api/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {


        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));


        SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, "User logged in", true));
    }
	
	
}
