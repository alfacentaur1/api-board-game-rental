package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.dto.BoardGameLoanDetailDTO;
import cz.cvut.fel.ear.dto.BoardGameLoanToCreateDTO;
import cz.cvut.fel.ear.mapper.LoanMapper;
import cz.cvut.fel.ear.model.BoardGameLoan;
import cz.cvut.fel.ear.model.Status;
import cz.cvut.fel.ear.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final LoanMapper loanMapper;

    public LoanController(LoanService loanService, LoanMapper loanMapper) {
        this.loanService = loanService;
        this.loanMapper = loanMapper;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @loanSecurity.isLoanOwner(#id, authentication)")
    public ResponseEntity<BoardGameLoanDetailDTO> getLoanById(@PathVariable long id) {
        BoardGameLoan boardGameLoan = loanService.getBoardGameLoan(id);
        BoardGameLoanDetailDTO loanDetailDTO = loanMapper.toDetailDto(boardGameLoan);
        return ResponseEntity.ok(loanDetailDTO);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BoardGameLoanDetailDTO>> getAllPendingLoans() {
        List<BoardGameLoan> pendingLoans = loanService.getAllPendingLoans();
        List<BoardGameLoanDetailDTO> pendingLoanDTOs = pendingLoans.stream()
                .map(loanMapper::toDetailDto)
                .toList();
        return ResponseEntity.ok(pendingLoanDTOs);
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BoardGameLoanDetailDTO>> getAllLoans() {
        List<BoardGameLoan> allLoans = loanService.getBoardGameLoans();
        List<BoardGameLoanDetailDTO> allLoanDTOs = allLoans.stream()
                .map(loanMapper::toDetailDto)
                .toList();
        return ResponseEntity.ok(allLoanDTOs);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<BoardGameLoanDetailDTO>> getLoansByUserId(@PathVariable long userId) {
        List<BoardGameLoan> userLoans = loanService.getAllBoardGameLoansByUser(userId);
        List<BoardGameLoanDetailDTO> userLoanDTOs = userLoans.stream()
                .map(loanMapper::toDetailDto)
                .toList();
        return ResponseEntity.ok(userLoanDTOs);
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveLoan(@PathVariable long id) {
        loanService.approveGameLoan(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectLoan(@PathVariable long id) {
        loanService.rejectGameLoan(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeLoanStatus(@PathVariable long id, @RequestParam Status status) {
        loanService.changeLoanStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/borrowed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BoardGameLoanDetailDTO>> getAllBorrowedLoans() {
        List<BoardGameLoan> borrowedLoans = loanService.getAllApprovedLoans();
        List<BoardGameLoanDetailDTO> borrowedLoanDTOs = borrowedLoans.stream()
                .map(loanMapper::toDetailDto)
                .toList();
        return ResponseEntity.ok(borrowedLoanDTOs);
    }

    @PostMapping("/")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BoardGameLoanDetailDTO> createLoan(@RequestBody BoardGameLoanToCreateDTO loanDetailDTO) {
        long loanId = loanService.createLoan(
                loanDetailDTO.getDueDate(),
                loanDetailDTO.getBoardGameNames(),
                loanDetailDTO.getUserId()
        );

        BoardGameLoan newLoan = loanService.getBoardGameLoan(loanId);
        BoardGameLoanDetailDTO newLoanDto = loanMapper.toDetailDto(newLoan);

        URI location = URI.create("/api/loans/" + newLoan.getId());
        return ResponseEntity.created(location).body(newLoanDto);
    }

    @PostMapping("/{loanId}/return")
    @PreAuthorize("hasRole('USER') and @loanSecurity.isLoanOwner(#loanId, authentication)")
    public ResponseEntity<?> returnLoan(@PathVariable long loanId) {
        loanService.returnBoardGameLoan(loanId);
        return ResponseEntity.ok().build();
    }
}