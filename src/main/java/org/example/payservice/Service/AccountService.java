package org.example.payservice.Service;

import org.example.payservice.Crypto.Web3jService;
import org.example.payservice.Entity.Account;
import org.example.payservice.Repositories.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final Web3jService web3jService;

    public AccountService(AccountRepository accountRepository, Web3jService web3jService) {
        this.accountRepository = accountRepository;
        this.web3jService = web3jService;
    }

    public String getFreeAddressAccount(){
        List<Account> freeAccounts = accountRepository.getFreeAccount();
        if (freeAccounts.isEmpty())return web3jService.createNewAccount();
        else {
            String address = freeAccounts.getFirst().getAddress();
            updateBusyStatusByAddress(address);
            return address;
        }
    }

    public void updateBusyStatusByAddress(String address){
        accountRepository.updateIsBusyStatus(address);
    }
}
