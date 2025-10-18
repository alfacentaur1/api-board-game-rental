package cz.cvut.fel.ear.dao;

import cz.cvut.fel.ear.model.BoardGameLoan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardGameLoanRepository extends JpaRepository<BoardGameLoan, Long> {
}
