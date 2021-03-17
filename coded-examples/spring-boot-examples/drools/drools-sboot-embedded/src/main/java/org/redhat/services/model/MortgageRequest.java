package org.redhat.services.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import mortgages.mortgages.Applicant;
import mortgages.mortgages.Bankruptcy;
import mortgages.mortgages.IncomeSource;
import mortgages.mortgages.LoanApplication;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MortgageRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private Applicant applicant;
    private IncomeSource incomeSource;
    private LoanApplication loanApplication;
    private Bankruptcy bankruptcy;
}
