package org.example.payservice.Controller;

import jakarta.validation.Valid;
import org.example.payservice.Body.InvoiceBody;
import org.example.payservice.Body.LoginBody;
import org.example.payservice.Entity.Account;
import org.example.payservice.Entity.Transaction;
import org.example.payservice.Entity.User;
import org.example.payservice.Repositories.AccountRepository;
import org.example.payservice.Repositories.UserRepository;
import org.example.payservice.Service.InvoiceService;
import org.example.payservice.Service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/")
public class ApiController {
    private final InvoiceService invoiceService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public ApiController(InvoiceService invoiceService, JwtService jwtService, UserRepository userRepository) {
        this.invoiceService = invoiceService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }


    @PostMapping("invoice/create")
    public ResponseEntity<?> createInvoice(@RequestBody @Valid InvoiceBody invoice, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            List<FieldError> errors = bindingResult.getFieldErrors();
            List<String> errorMessages = errors.stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }
        invoiceService.save(invoice);
        return ResponseEntity.ok().body("completed");
    }

    @PostMapping("generate_JWT")
    public ResponseEntity<?> generateJWT(@RequestBody @Valid LoginBody loginBody, BindingResult bindingResult) {
        if (bindingResult.hasErrors()){
            List<FieldError> errors = bindingResult.getFieldErrors();
            List<String> errorMessages = errors.stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }
        User user = userRepository.findByEmail(loginBody.getEmail());
        if (user==null || !user.getPassword().equals(loginBody.getPassword())) return ResponseEntity.status(401).body("Incorrect password or email");
        return ResponseEntity.ok().body(jwtService.generateToken(loginBody.getEmail()));
    }

    @PostMapping("callback")
    private ResponseEntity<?> callBack(@RequestBody org.example.payservice.HttpRequest.RequestBody requestBody){
        System.out.println(requestBody);
        return ResponseEntity.ok().body("");
    }
}
