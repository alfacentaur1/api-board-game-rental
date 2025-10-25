package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.dao.BoardGameItemRepository;
import org.springframework.stereotype.Service;

@Service
public class BoardGameItemService {
    private final BoardGameItemRepository boardGameItemRepository;

    public BoardGameItemService(BoardGameItemRepository boardGameItemRepository) {
        this.boardGameItemRepository = boardGameItemRepository;
    }


}
