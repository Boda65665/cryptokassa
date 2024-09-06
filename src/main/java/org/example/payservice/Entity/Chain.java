package org.example.payservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chain")
@Entity()
public class Chain {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "rpc")
    private String rpc;
    @Column(name = "chain_id")
    private String chainId;
    @Column(name = "contract_usdt")
    private String contractUSDT;
    @Column(name = "awaiting_confirmation")
    private int awaitingConfirmation;
    @Column(name = "last_block_or_transaction")
    private BigInteger lastBlockOrTransaction;
    @Column(name = "type")
    private String type;
    @Column(name = "divide_by_usdt_for_convert")
    private BigDecimal divideByUsdtForConvert;
}
