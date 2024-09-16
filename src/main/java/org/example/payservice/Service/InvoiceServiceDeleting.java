package org.example.payservice.Service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InvoiceServiceDeleting {
    private final InvoiceService invoiceService;

    public InvoiceServiceDeleting(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @Scheduled(fixedRate = 60000)
    public void deleteExpired(){
        invoiceService.deleteByExpired();
    }
}
