package org.example.payservice.Connverters;

import org.example.payservice.Body.InvoiceBody;
import org.example.payservice.Entity.Invoice;

public class InvoiceConverter {
    public Invoice fromInvoiceBodyToInvoice(InvoiceBody invoice){
        Invoice invoiceConvert = new Invoice();
        invoiceConvert.setIdClient(invoice.getIdClient());
        return invoiceConvert;
    }
}
