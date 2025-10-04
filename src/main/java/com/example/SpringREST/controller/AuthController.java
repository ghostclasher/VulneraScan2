package com.example.SpringREST.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.naming.AuthenticationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.SpringREST.model.Account;
import com.example.SpringREST.payload.auth.AccountDTO;
import com.example.SpringREST.payload.auth.AccountViewDTO;
import com.example.SpringREST.payload.auth.AuthoritiesDTO;
import com.example.SpringREST.payload.auth.PasswordDTO;
import com.example.SpringREST.payload.auth.ProfileDTO;
import com.example.SpringREST.payload.auth.TokenDTO;
import com.example.SpringREST.payload.auth.userLoginDTO;
import com.example.SpringREST.service.Accountservice;
import com.example.SpringREST.service.TokenService;
import com.example.SpringREST.util.constants.AccountError;
import com.example.SpringREST.util.constants.AccountSuccess;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


 


@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth Controller", description = "Controller for account managment")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Autowired
    private Accountservice accountservice;
      
    public AuthController(TokenService tokenService,AuthenticationManager authenticationManager){
        this.tokenService=tokenService;
        this.authenticationManager=authenticationManager;
    }

    @PostMapping("/token")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<TokenDTO> token( @Valid @RequestBody userLoginDTO userLogin)throws AuthenticationException {
         try{
            Authentication authentication=authenticationManager
        .authenticate(new UsernamePasswordAuthenticationToken(userLogin.getEmail(), userLogin.getPassword()));
        return  ResponseEntity.ok(new TokenDTO(tokenService.generateToken(authentication)));
         }
         catch(Exception e){
            log.debug(AccountError.TOKEN_GENERATION_ERROR.toString() +" :" + e.getMessage());
            return new ResponseEntity<>(new TokenDTO(null), HttpStatus.BAD_GATEWAY);
         }
    }



    // addUser API
    @PostMapping(value = "users/add",consumes = "application/json",produces = "application/json")
    @Operation(summary = "Add a new user")
    @ResponseStatus(HttpStatus.CREATED )
    @ApiResponse(responseCode = "201", description = "User added successfully")
    @ApiResponse(responseCode = "400", description = "Bad request, user could not be added")
    public ResponseEntity<String> addUser( @Valid @RequestBody AccountDTO accountDTO) {
        try {
            // Logic to add user
            Account account=new Account();
            account.setEmail(accountDTO.getEmail());
            account.setPassword(accountDTO.getPassword());
            // account.setRole("ROLE_USER"); // Default role can be set here);
            accountservice.save(account);
            return ResponseEntity.ok(AccountSuccess.ACCOUNT_ADDED.toString());
          
        } catch (Exception e) {
            log.error(AccountError.ADD_ACCOUNT_ERROR.toString() + " :" + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }



    // getAllUsers API
    @GetMapping(value = "users",produces = "application/json")
    @Operation(summary = "Get all users")
     @SecurityRequirement(name="restful-demo-api")
    public List<AccountViewDTO> Users() {
        List<AccountViewDTO> accounts=new ArrayList<>();
        for(Account account:accountservice.findAll()){
            accounts.add(new AccountViewDTO(account.getId(),account.getEmail(), account.getAuthorities()));
        }
        return accounts;
    }



    // get profile api
    @GetMapping(value="/profile",produces = "application/json")
    @ApiResponse(responseCode = "200", description = "profile fetched successfully")
     @ApiResponse(responseCode = "401", description = "dont have permission to access profile")
     @Operation(summary = "Fetch user profile")
     @SecurityRequirement(name="restful-demo-api")
     public ProfileDTO getProfile(Authentication authentication){
        String email=authentication.getName();
        Optional<Account> optionalaccount=accountservice.findByEmail(email);
        if(optionalaccount.isPresent()){
            Account account=optionalaccount.get();
            return new ProfileDTO((account.getId()), account.getEmail(),account.getAuthorities());
        }
        else{
            return null;
        }

     }



        // update password api
    @PutMapping(value="/profile/update-password",produces = "application/json",consumes="application/json")
    @ApiResponse(responseCode = "200", description = "profile fetched successfully")
     @ApiResponse(responseCode = "401", description = "dont have permission to access profile")
     @Operation(summary = "update password")
     @SecurityRequirement(name="restful-demo-api")
     public AccountViewDTO updatePassword(@Valid @RequestBody PasswordDTO passwordDTO,Authentication authentication){
        String email=authentication.getName();
        Optional<Account> optionalaccount=accountservice.findByEmail(email);
        if(optionalaccount.isPresent()){
            Account account=optionalaccount.get();
            account.setPassword(passwordDTO.getNewpassword());
            accountservice.save(account);
            AccountViewDTO accountViewDTO=new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorities());
            return accountViewDTO;
        }
        else{
            return null;
        }

     }



           // update Authorities api
    @PutMapping(value="/users/{user_id}/update-authorities/",produces = "application/json",consumes="application/json")
    @ApiResponse(responseCode = "200", description = "authoritites updated successfully")
     @ApiResponse(responseCode = "401", description = "dont have permission to update authorities")
     @Operation(summary = "update authtoritites")
     @SecurityRequirement(name="restful-demo-api")
     public  ResponseEntity<AccountViewDTO> updateauthorities(@Valid @RequestBody AuthoritiesDTO authoritiesDTO,@PathVariable long user_id){

        Optional<Account> optionalaccount=accountservice.findByID(user_id);
        if(optionalaccount.isPresent()){
            Account account=optionalaccount.get();
            account.setAuthorities(authoritiesDTO.getAuthorities());
            accountservice.save(account);
            AccountViewDTO accountViewDTO=new AccountViewDTO(account.getId(), account.getEmail(), account.getAuthorities());
            return  ResponseEntity.ok(accountViewDTO);
        }
        else{
            return null;
        }

     }



           // delete account api
    @DeleteMapping(value="/profile/delete")
    @ApiResponse(responseCode = "200", description = "Account deleted successfully")
     @ApiResponse(responseCode = "401", description = "dont have permission to update authorities")
     @Operation(summary = "Delete account")
     @SecurityRequirement(name="restful-demo-api")
     public  ResponseEntity<String> delete_profile(Authentication authentication){

        String email=authentication.getName();
        Optional<Account> optionalaccount=accountservice.findByEmail(email);
        if(optionalaccount.isPresent()){
            Account account=optionalaccount.get();
            accountservice.deleteByID(optionalaccount.get().getId());
            return ResponseEntity.ok("Account deleted succesfully");
            
        }
        else{
            return new ResponseEntity<>("Bad request", HttpStatus.BAD_REQUEST);
        }

     }
    
    
    
}
