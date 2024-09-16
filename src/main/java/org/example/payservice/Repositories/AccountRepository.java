package org.example.payservice.Repositories;

import org.example.payservice.Entity.Account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    @Modifying
    @Transactional
    @Query("Update Account a SET a.isBusy=CASE WHEN a.isBusy = TRUE THEN FALSE ELSE TRUE END where a.address=?1")
    void updateIsBusyStatus(String address);
    @Query("select a from Account a where a.isBusy=false")
    List<Account> getFreeAccount();
}
