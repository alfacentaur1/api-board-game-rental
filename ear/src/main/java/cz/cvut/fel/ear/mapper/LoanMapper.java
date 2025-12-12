package cz.cvut.fel.ear.mapper;

import cz.cvut.fel.ear.dto.BoardGameItemDTO;
import cz.cvut.fel.ear.dto.BoardGameLoanDetailDTO;
import cz.cvut.fel.ear.dto.UserSummaryDTO;
import cz.cvut.fel.ear.model.BoardGameLoan;
import cz.cvut.fel.ear.model.RegisteredUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoanMapper {

    private final BoardGameItemMapper boardGameItemMapper;

    public LoanMapper(BoardGameItemMapper boardGameItemMapper) {
        this.boardGameItemMapper = boardGameItemMapper;
    }

    public BoardGameLoanDetailDTO toDetailDto(BoardGameLoan loan) {
        UserSummaryDTO userSummaryDTO = toUserDto(loan.getUser());

        List<BoardGameItemDTO> boardGameItemDTOS = loan.getItems().stream()
                .map(boardGameItemMapper::toDto)
                .toList();

        return new BoardGameLoanDetailDTO(
                loan.getId(),
                loan.getBorrowedAt(),
                loan.getReturnedAt(),
                loan.getDueDate(),
                loan.getStatus(),
                userSummaryDTO,
                boardGameItemDTOS
        );
    }

    private UserSummaryDTO toUserDto(RegisteredUser user) {
        if (user == null) {
            return null;
        }
        return new UserSummaryDTO(
                user.getId(),
                user.getUsername()
        );
    }
}