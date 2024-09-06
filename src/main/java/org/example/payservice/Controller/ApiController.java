package org.example.payservice.Controller;

import org.example.payservice.Entity.Invoice;
import org.example.payservice.Repositories.InvoiceRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoice")
public class ApiController {
    private final InvoiceRepository invoiceRepository;

    public ApiController(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

//    @PostMapping("/create")
//    public String createInvoice(@RequestBody Invoice invoice){
//
//
//    }
}
