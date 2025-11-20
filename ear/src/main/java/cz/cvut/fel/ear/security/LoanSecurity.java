package cz.cvut.fel.ear.security;

import cz.cvut.fel.ear.dao.BoardGameLoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("loanSecurity")
public class LoanSecurity {

    @Autowired
    private BoardGameLoanRepository boardGameLoanRepository;

    //check if the authenticated user is the owner of the loan in the loan controller
    public boolean isLoanOwner(Long loanId, Authentication authentication) {
        var loan = boardGameLoanRepository.findById(loanId).orElse(null);
        if (loan == null) return false;

        String currentUsername = authentication.getName();
        return loan.getUser().getUsername().equals(currentUsername);
    }
}
