package org.example.payservice.Service;

import org.example.payservice.Body.InvoiceBody;
import org.example.payservice.Connverters.InvoiceConverter;
import org.example.payservice.Entity.Invoice;
import org.example.payservice.Repositories.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InvoiceService {
    private final InvoiceConverter invoiceConverter = new InvoiceConverter();
    private final InvoiceRepository invoiceRepository;
    private final AccountService accountService;

    public InvoiceService(InvoiceRepository invoiceRepository, AccountService accountService) {
        this.invoiceRepository = invoiceRepository;
        this.accountService = accountService;
    }

    public void save(InvoiceBody invoiceBody){
        Invoice invoice = invoiceConverter.fromInvoiceBodyToInvoice(invoiceBody);
        invoice.setAddress(accountService.getFreeAddressAccount());
        invoice.setLocalDateTime(LocalDateTime.now());
        invoiceRepository.save(invoice);
    }

    public void deleteByExpired(){
        List<Invoice> invoices = invoiceRepository.deleteByExpired(LocalDateTime.now().minusHours(1));
        for (Invoice invoice : invoices) {
            delete(invoice);
        }
    }

    public void delete(Invoice invoice){
        invoiceRepository.delete(invoice);
        accountService.updateBusyStatusByAddress(invoice.getAddress());
    }

    public List<Invoice> findAll() {
        return invoiceRepository.findAll();
    }
}
