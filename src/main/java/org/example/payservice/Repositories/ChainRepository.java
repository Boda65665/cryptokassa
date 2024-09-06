package org.example.payservice.Repositories;

import org.example.payservice.Entity.Chain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigInteger;
import java.util.List;

@Repository
public interface ChainRepository extends JpaRepository<Chain, Integer> {
    @Modifying
    @Transactional
    @Query("update Chain c set c.lastBlockOrTransaction = ?1 where c.chainId=?2")
    void updateLastBlockOrTransaction(BigInteger last, String chainId);
    Chain findByChainId(String chainId);
    List<Chain> findAllByType(String type);
}
