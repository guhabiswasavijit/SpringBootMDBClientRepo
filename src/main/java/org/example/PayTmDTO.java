package org.example;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class PayTmDTO implements Serializable {
    private String txDate;
    private String activity;
    private String sourceDestination;
    private String walletTxId;
    private String usr_comment;
    private String debit;
    private String credit;
    private String transaction_breakup;
    private String status;
}
