package org.example.payservice.Connverters;

import org.example.payservice.Body.InvoiceBody;
import org.example.payservice.Entity.Invoice;

public class InvoiceConverter {
    public InvoiceBody fromInvoiceToInvoiceBody(Invoice invoice){
        InvoiceBody invoiceConvert = new InvoiceBody();
        invoiceConvert.setValueInCrypto(invoice.getValueInCrypto());
        invoiceConvert.setValueInCrypto(invoice.getValueInUsdt());
        invoiceConvert.setIdClient(invoice.getIdClient());
        return invoiceConvert;
    }

    public Invoice fromInvoiceBodyToInvoice(InvoiceBody invoice){
        Invoice invoiceConvert = new Invoice();
        invoiceConvert.setValueInCrypto(invoice.getValueInCrypto());
        invoiceConvert.setValueInUsdt(invoice.getValueInUsdt());
        invoiceConvert.setIdClient(invoice.getIdClient());
        return invoiceConvert;
    }
}
