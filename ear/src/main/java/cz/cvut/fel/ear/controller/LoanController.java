package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.controller.response.ResponseWrapper;
import cz.cvut.fel.ear.dto.BoardGameLoanDetailDTO;
import cz.cvut.fel.ear.dto.BoardGameLoanToCreateDTO;
import cz.cvut.fel.ear.dto.LoanIdDTO;
import cz.cvut.fel.ear.dto.LoanStatusDTO;
import cz.cvut.fel.ear.mapper.LoanMapper;
import cz.cvut.fel.ear.model.BoardGameLoan;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.service.LoanService;
import cz.cvut.fel.ear.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;
    private final LoanMapper loanMapper;
    private final UserService userService;

    public LoanController(LoanService loanService, LoanMapper loanMapper, UserService userService) {
        this.loanService = loanService;
        this.loanMapper = loanMapper;
        this.userService = userService;
    }

    /**
     * Retrieves detailed information about a specific loan by its ID.
     *
     * @param id The ID of the loan to retrieve.
     * @return A ResponseEntity containing the BoardGameLoanDetailDTO.
     */
    @Operation(summary = "Get Loan by ID", description = "Retrieves detailed information about a specific loan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loan successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "Loan not found", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @loanSecurity.isLoanOwner(#id, authentication)")
    public ResponseEntity<Map<String, Object>> getLoanById(
            @Parameter(description = "ID of the loan to retrieve", example = "1", required = true)
            @PathVariable long id) {
        BoardGameLoan boardGameLoan = loanService.getBoardGameLoan(id);
        BoardGameLoanDetailDTO loanDetailDTO = loanMapper.toDetailDto(boardGameLoan);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "Loan");
        generator.addResponseData("loan", loanDetailDTO);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Retrieves all loans with pending status.
     *
     * @return A ResponseEntity containing a list of pending BoardGameLoanDetailDTOs.
     */
    @Operation(summary = "Get All Pending Loans", description = "Retrieves all loans with pending status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pending loans successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllPendingLoans() {
        List<BoardGameLoan> pendingLoans = loanService.getAllPendingLoans();
        List<BoardGameLoanDetailDTO> pendingLoanDTOs = pendingLoans.stream()
                .map(loanMapper::toDetailDto)
                .toList();

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "Loan");
        generator.addResponseData("amount", pendingLoanDTOs.size());
        generator.addResponseData("loans", pendingLoanDTOs);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Retrieves all loans currently in the system.
     *
     * @return A ResponseEntity containing a list of all BoardGameLoanDetailDTOs.
     */
    @Operation(summary = "Get All Loans", description = "Retrieves all loans in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All loans successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllLoans() {
        List<BoardGameLoan> allLoans = loanService.getBoardGameLoans();
        List<BoardGameLoanDetailDTO> allLoanDTOs = allLoans.stream()
                .map(loanMapper::toDetailDto)
                .toList();

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "Loan");
        generator.addResponseData("amount", allLoanDTOs.size());
        generator.addResponseData("loans", allLoanDTOs);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Retrieves all loans for a specific user.
     *
     * @param userId The ID of the user.
     * @return A ResponseEntity containing a list of the user's BoardGameLoanDetailDTOs.
     */
    @Operation(summary = "Get Loans by User ID", description = "Retrieves all loans for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User loans successfully retrieved", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or #userId == authentication.principal.id)")
    public ResponseEntity<Map<String, Object>> getLoansByUserId(
            @Parameter(description = "ID of the user to retrieve loans for", example = "1", required = true)
            @PathVariable long userId) {
        List<BoardGameLoan> userLoans = loanService.getAllBoardGameLoansByUser(userId);
        List<BoardGameLoanDetailDTO> userLoanDTOs = userLoans.stream()
                .map(loanMapper::toDetailDto)
                .toList();

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "Loan");
        generator.addResponseData("amount", userLoanDTOs.size());
        generator.addResponseData("loans", userLoanDTOs);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }



    /**
     * Changes the status of a specific loan.
     *
     * @param dto The DTO containing the loan ID and the new status.
     * @return A ResponseEntity indicating success.
     */
    @Operation(summary = "Change Loan Status", description = "Manually changes the status of a loan")
    @PatchMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> changeLoanStatus(
            @Valid @RequestBody LoanStatusDTO dto
    ) {
        loanService.changeLoanStatus(dto.loanId(), dto.status());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_MODIFIED, "Loan");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Retrieves all loans that are currently approved (borrowed).
     *
     * @return A ResponseEntity containing a list of borrowed BoardGameLoanDetailDTOs.
     */
    @Operation(summary = "Get All Borrowed Loans", description = "Retrieves all loans that are currently approved (borrowed)")
    @GetMapping("/borrowed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllBorrowedLoans() {
        List<BoardGameLoan> borrowedLoans = loanService.getAllApprovedLoans();
        List<BoardGameLoanDetailDTO> borrowedLoanDTOs = borrowedLoans.stream()
                .map(loanMapper::toDetailDto)
                .toList();

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_FOUND, "Loan");
        generator.addResponseData("amount", borrowedLoanDTOs.size());
        generator.addResponseData("loans", borrowedLoanDTOs);

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Creates a new loan request for a board game.
     *
     * @param boardGameLoanToCreateDTO Data transfer object containing loan details.
     * @return A ResponseEntity indicating success and the location of the new loan.
     */
    @Operation(summary = "Create Loan", description = "Creates a new loan request for a board game item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan successfully created", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "400", description = "Validation error occurred", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", description = "User or Board Game Item not found", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Board Game Item is not available", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Authentication required", content = @Content(schema = @Schema(hidden = true))),
    })
    @PostMapping("/")
    @PreAuthorize("isAuthenticated() and hasRole('USER')")
    public ResponseEntity<Map<String, Object>> createLoan(
            @Valid @RequestBody BoardGameLoanToCreateDTO boardGameLoanToCreateDTO,
            Principal principal
    ) {
        RegisteredUser user = (RegisteredUser) userService.getUserByUsername(principal.getName());

        long loanId = loanService.createLoan(
                boardGameLoanToCreateDTO.dueDate(),
                boardGameLoanToCreateDTO.boardGameNames(),
                user.getId()
        );

        BoardGameLoan newLoan = loanService.getBoardGameLoan(loanId);
        BoardGameLoanDetailDTO newLoanDto = loanMapper.toDetailDto(newLoan);

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_CREATED, "Loan");
        generator.addResponseData("loan", newLoanDto);

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Location", "api/loans/" + newLoan.getId());

        return new ResponseEntity<>(generator.getResponse(), responseHeaders, HttpStatus.CREATED);
    }

    /**
     * Approves a pending loan request.
     */
    @Operation(summary = "Approve Loan", description = "Approves a pending loan request")
    @PatchMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> approveLoan(
            @Valid @RequestBody LoanIdDTO dto
    ) {
        loanService.approveGameLoan(dto.loanId());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_MODIFIED, "Loan");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Rejects a pending loan request.
     */
    @Operation(summary = "Reject Loan", description = "Rejects a pending loan request")
    @PatchMapping("/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> rejectLoan(
            @Valid @RequestBody LoanIdDTO dto
    ) {
        loanService.rejectGameLoan(dto.loanId());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_MODIFIED, "Loan");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }

    /**
     * Marks a loan as returned.
     */
    @Operation(summary = "Return Loan", description = "Marks a loan as returned")
    @PatchMapping("/return")
    @PreAuthorize("isAuthenticated() and (hasRole('USER') and @loanSecurity.isLoanOwner(#dto.loanId(), authentication)) or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> returnLoan(
            @Valid @RequestBody LoanIdDTO dto
    ) {
        loanService.returnBoardGameLoan(dto.loanId());

        ResponseWrapper generator = new ResponseWrapper();
        generator.setResponseInfoMessage(ResponseWrapper.ResponseInfoCode.SUCCESS_MODIFIED, "Loan");

        return new ResponseEntity<>(generator.getResponse(), HttpStatus.OK);
    }
}