package org.example.payservice.Excepion;

public class SendTransactionError extends Exception {
    public SendTransactionError(String message) {
        super(message);
    }
}
