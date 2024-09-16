package org.example.payservice.Repositories;

import org.example.payservice.Entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {
    @Query("select i from Invoice i where i.localDateTime<=?1")
    List<Invoice> deleteByExpired(LocalDateTime dateTimeFilter);
}
