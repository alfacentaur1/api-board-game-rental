package cz.cvut.fel.ear.controller;

import cz.cvut.fel.ear.dto.BoardGameDTO;
import cz.cvut.fel.ear.dto.BoardGameToCreateDTO;
import cz.cvut.fel.ear.model.BoardGame;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.model.User;
import cz.cvut.fel.ear.service.BoardGameService;
import cz.cvut.fel.ear.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boardgames")
public class BoardGameController {
    private final BoardGameService boardGameService;
    private final UserService userService;
//    private final RegisteredUserService registeredUserService;

    public BoardGameController(BoardGameService boardGameService, UserService userService) {
        this.boardGameService = boardGameService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<BoardGameDTO> getBoardGame(@PathVariable Long id) {
        BoardGame boardGame = boardGameService.getBoardGame(id);
        BoardGameDTO boardGameDTO = new BoardGameDTO(boardGame.getId(),
                boardGame.getAvailableCopies(),
                boardGame.getDescription(),
                boardGame.getName());
        return new ResponseEntity<>(boardGameDTO, HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<?> addBoardGame(@RequestBody BoardGameToCreateDTO boardGameToCreateDTO) {
        Long id = boardGameService.createBoardGame(boardGameToCreateDTO.getName(),
                boardGameToCreateDTO.getDescription());

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.LOCATION, "/boardGames/" + id);
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);

    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<?> deleteBoardGame(@PathVariable Long gameId) {
        boardGameService.removeBoardGame(gameId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/")
    public ResponseEntity<?> updateBoardGame(@RequestBody BoardGameDTO boardGameDTO) {
        boardGameService.updateBoardGameDescription(boardGameDTO.getId(),
                boardGameDTO.getDescription());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<List<BoardGameDTO>> getAllBoardGames() {
        List<BoardGame> boardGames= boardGameService.getAllBoardGames();
        List<BoardGameDTO> boardGameDTOList = boardGames.stream()
                .map(boardGame -> new BoardGameDTO(
                        boardGame.getId(),
                        boardGame.getAvailableCopies(),
                        boardGame.getDescription(),
                        boardGame.getName()
                ))
                .toList();
        return new ResponseEntity<>(boardGameDTOList, HttpStatus.OK);
    }

    //user is registered user, admin cant have favorite games - thats why we cast it (JFYI)
    @PostMapping("/users/{username}/favorites/{gameId}")
    public ResponseEntity<?> addFavoriteBoardGame(@PathVariable String username, @PathVariable Long gameId) {
        RegisteredUser user = (RegisteredUser) userService.getUserByUsername(username);
        boardGameService.addGameToFavorites(user, gameId);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.LOCATION, "/api/boardgames/users/" + username + "/favorites/" + gameId);
        return new ResponseEntity<>(responseHeaders, HttpStatus.CREATED);

    }

    @DeleteMapping("/users/{username}/favorites/{gameId}")
    public ResponseEntity<?> deleteGameFromFavorites(@PathVariable String username, @PathVariable Long gameId) {
    RegisteredUser user = (RegisteredUser) userService.getUserByUsername(username);
    boardGameService.removeBoardGame(gameId);
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/users/{username}/favorites/")
    public ResponseEntity<List<BoardGameDTO>> getFavorites(@PathVariable String username) {
        RegisteredUser user = (RegisteredUser) userService.getUserByUsername(username);
        List<BoardGameDTO> favoriteGameDTOs = new ArrayList<>();
        for (BoardGame boardGame : user.getFavoriteBoardGames()) {
            BoardGameDTO boardGameDTO = new BoardGameDTO(
                    boardGame.getId(),
                    boardGame.getAvailableCopies(),
                    boardGame.getDescription(),
                    boardGame.getName()
            );
            favoriteGameDTOs.add(boardGameDTO);
        }
        return new ResponseEntity<>(favoriteGameDTOs, HttpStatus.OK);
    }

    @GetMapping("/topBorrowed/{count}")
    public ResponseEntity<List<BoardGameDTO>> getTopBorrowedBoardGames(@PathVariable int count) {
        List<BoardGame> topBorrowedGames = boardGameService.getTopXBorrowedGames(count);
        List<BoardGameDTO> topBorrowedGameDTOs = topBorrowedGames.stream()
                .map(boardGame -> new BoardGameDTO(
                        boardGame.getId(),
                        boardGame.getAvailableCopies(),
                        boardGame.getDescription(),
                        boardGame.getName()
                ))
                .collect(Collectors.toList());
        return new ResponseEntity<>(topBorrowedGameDTOs, HttpStatus.OK);

    }

}
