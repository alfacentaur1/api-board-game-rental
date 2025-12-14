package cz.cvut.fel.ear;

import cz.cvut.fel.ear.dao.*;
import cz.cvut.fel.ear.dto.UserRegistrationDTO;
import cz.cvut.fel.ear.model.*;
import cz.cvut.fel.ear.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final BoardGameService boardGameService;
    private final BoardGameItemService boardGameItemService;
    private final CategoryService categoryService;
    private final LoanService loanService;
    private final ReviewService reviewService;
    private final BoardGameLoanRepository loanRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public DataInitializer(UserService userService, BoardGameService boardGameService,
                           BoardGameItemService boardGameItemService, CategoryService categoryService,
                           LoanService loanService, ReviewService reviewService,
                           BoardGameLoanRepository loanRepository, UserRepository userRepository,
                           CategoryRepository categoryRepository) {
        this.userService = userService;
        this.boardGameService = boardGameService;
        this.boardGameItemService = boardGameItemService;
        this.categoryService = categoryService;
        this.loanService = loanService;
        this.reviewService = reviewService;
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 STARTING ROBUST DATA INITIALIZATION...");

        List<RegisteredUser> users = createRoles();
        Map<String, Long> categories = createCategories();
        Map<String, Long> games = createGames(categories);

        createItems(games);

        if (!users.isEmpty()) {
            createLoans(games, users);
            createReviews(games, users);
        }

        System.out.println("✅ MASSIVE DATA INITIALIZATION COMPLETE");
    }

    private List<RegisteredUser> createRoles() {
        List<RegisteredUser> createdUsers = new ArrayList<>();
        try {
            createAdminIfNotExist("System Admin", "admin@ear.cz", "admin");
            createAdminIfNotExist("Head Librarian", "librarian@ear.cz", "librarian");

            String[] names = {
                    "John", "Jane", "Alice", "Bob", "Charlie",
                    "David", "Eve", "Frank", "Grace", "Hank",
                    "Ivy", "Jack", "Kevin", "Laura", "Mike"
            };

            for (int i = 0; i < names.length; i++) {
                String username = names[i].toLowerCase() + i;
                createUserIfNotExist(names[i] + " Doe", username + "@example.com", username);
                try {
                    createdUsers.add((RegisteredUser) userService.getUserByUsername(username));
                } catch (Exception e) {
                    System.out.println("⚠️ Could not retrieve user " + username);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning during user creation: " + e.getMessage());
        }
        return createdUsers;
    }

    private void createUserIfNotExist(String name, String email, String username) {
        try {
            userService.registerUser(new UserRegistrationDTO(name, email, "12345", username));
        } catch (Exception ignored) {}
    }

    private void createAdminIfNotExist(String name, String email, String username) {
        try {
            userService.registerAdmin(new UserRegistrationDTO(name, email, "12345", username));
        } catch (Exception ignored) {}
    }

    private Map<String, Long> createCategories() {
        Map<String, Long> map = new HashMap<>();
        String[] cats = {
                "Strategy", "Family", "Party", "Abstract", "Thematic",
                "Wargame", "Eurogame", "Children", "Cooperative", "Deckbuilding",
                "Dexterity", "Puzzle", "Sci-Fi", "Fantasy", "Horror", "Adventure"
        };

        for (String c : cats) {
            try {
                map.put(c, categoryService.addCategory(c));
            } catch (Exception e) {
                categoryRepository.findByName(c).ifPresent(cat -> map.put(c, cat.getId()));
            }
        }
        return map;
    }

    private Long getCatId(Map<String, Long> categories, String name) {
        Long id = categories.get(name);
        if (id == null) {
            try {
                id = categoryService.addCategory(name);
                categories.put(name, id);
            } catch (Exception e) {
                id = categoryRepository.findByName(name).map(Category::getId).orElse(null);
            }
        }
        return id;
    }

    private Map<String, Long> createGames(Map<String, Long> c) {
        Map<String, Long> g = new HashMap<>();

        createGame(g, "Catan", "Collect resources.", 4, 2, getCatId(c, "Strategy"), getCatId(c, "Family"));
        createGame(g, "Carcassonne", "Tile-placement.", 5, 1, getCatId(c, "Family"), getCatId(c, "Strategy"));
        createGame(g, "Ticket to Ride", "Trains.", 6, 0, getCatId(c, "Family"));
        createGame(g, "Pandemic", "Save the world.", 4, 1, getCatId(c, "Cooperative"), getCatId(c, "Strategy"));
        createGame(g, "Gloomhaven", "Tactical combat.", 2, 0, getCatId(c, "Fantasy"), getCatId(c, "Strategy"), getCatId(c, "Thematic"));
        createGame(g, "Terraforming Mars", "Mars.", 5, 2, getCatId(c, "Sci-Fi"), getCatId(c, "Strategy"), getCatId(c, "Eurogame"));
        createGame(g, "Scythe", "Mechs.", 3, 0, getCatId(c, "Strategy"), getCatId(c, "Sci-Fi"));
        createGame(g, "Wingspan", "Birds.", 4, 0, getCatId(c, "Family"), getCatId(c, "Strategy"));
        createGame(g, "Azul", "Tiles.", 5, 0, getCatId(c, "Abstract"), getCatId(c, "Family"));
        createGame(g, "Codenames", "Words.", 6, 1, getCatId(c, "Party"));
        createGame(g, "7 Wonders", "Civ builder.", 4, 0, getCatId(c, "Strategy"), getCatId(c, "Family"));
        createGame(g, "Splendor", "Gems.", 5, 1, getCatId(c, "Family"), getCatId(c, "Strategy"));
        createGame(g, "Dominion", "Deck-building.", 4, 1, getCatId(c, "Deckbuilding"), getCatId(c, "Strategy"));
        createGame(g, "Arkham Horror", "Lovecraft.", 2, 1, getCatId(c, "Horror"), getCatId(c, "Thematic"));
        createGame(g, "Root", "Woodland war.", 3, 0, getCatId(c, "Wargame"), getCatId(c, "Strategy"));
        createGame(g, "Dixit", "Storytelling.", 5, 0, getCatId(c, "Party"), getCatId(c, "Family"));
        createGame(g, "King of Tokyo", "Dice monster.", 4, 1, getCatId(c, "Family"), getCatId(c, "Party"));
        createGame(g, "The Crew", "Space coop.", 6, 0, getCatId(c, "Sci-Fi"), getCatId(c, "Cooperative"));
        createGame(g, "Patchwork", "Quilts.", 3, 0, getCatId(c, "Abstract"), getCatId(c, "Puzzle"));
        createGame(g, "Spirit Island", "Spirits.", 3, 0, getCatId(c, "Cooperative"), getCatId(c, "Strategy"));
        createGame(g, "Brass: Birmingham", "Industry.", 2, 0, getCatId(c, "Eurogame"), getCatId(c, "Strategy"));
        createGame(g, "Dune: Imperium", "Dune.", 4, 0, getCatId(c, "Sci-Fi"), getCatId(c, "Strategy"));
        createGame(g, "Everdell", "Critters.", 3, 1, getCatId(c, "Family"), getCatId(c, "Strategy"));
        createGame(g, "Great Western Trail", "Cows.", 2, 0, getCatId(c, "Eurogame"), getCatId(c, "Strategy"));
        createGame(g, "Twilight Struggle", "Cold War.", 2, 0, getCatId(c, "Wargame"), getCatId(c, "Strategy"));
        createGame(g, "Star Wars: Rebellion", "Galactic war.", 1, 0, getCatId(c, "Sci-Fi"), getCatId(c, "Wargame"));
        createGame(g, "War of the Ring", "LotR.", 1, 0, getCatId(c, "Fantasy"), getCatId(c, "Wargame"));
        createGame(g, "Through the Ages", "Civ.", 2, 0, getCatId(c, "Strategy"), getCatId(c, "Eurogame"));
        createGame(g, "Concordia", "Rome.", 3, 0, getCatId(c, "Strategy"), getCatId(c, "Eurogame"));
        createGame(g, "Viticulture", "Wine.", 3, 0, getCatId(c, "Strategy"), getCatId(c, "Eurogame"));
        createGame(g, "Mansions of Madness", "Horror app.", 2, 1, getCatId(c, "Horror"), getCatId(c, "Thematic"));
        createGame(g, "Blood Rage", "Vikings.", 3, 0, getCatId(c, "Strategy"), getCatId(c, "Thematic"));
        createGame(g, "Mechs vs. Minions", "Programming.", 2, 0, getCatId(c, "Cooperative"), getCatId(c, "Thematic"));
        createGame(g, "Decrypto", "Deduction.", 4, 0, getCatId(c, "Party"));
        createGame(g, "Secret Hitler", "Social deduction.", 5, 1, getCatId(c, "Party"));
        createGame(g, "The Resistance", "Dystopian.", 5, 0, getCatId(c, "Party"));
        createGame(g, "Santorini", "Gods.", 3, 0, getCatId(c, "Abstract"), getCatId(c, "Strategy"));
        createGame(g, "Hive", "Bugs.", 3, 0, getCatId(c, "Abstract"));
        createGame(g, "Cascadia", "Nature.", 4, 0, getCatId(c, "Abstract"), getCatId(c, "Family"));
        createGame(g, "Lost Ruins of Arnak", "Adventure.", 3, 0, getCatId(c, "Strategy"), getCatId(c, "Adventure"));
        createGame(g, "Nemesis", "Alien horror.", 2, 1, getCatId(c, "Sci-Fi"), getCatId(c, "Horror"));
        createGame(g, "Heat: Pedal to the Metal", "Racing.", 4, 0, getCatId(c, "Family"), getCatId(c, "Thematic"));
        createGame(g, "Just One", "Coop words.", 5, 0, getCatId(c, "Party"), getCatId(c, "Cooperative"));
        createGame(g, "Crokinole", "Flicking.", 2, 0, getCatId(c, "Dexterity"));
        createGame(g, "Klask", "Magnets.", 2, 0, getCatId(c, "Dexterity"));

        createGame(g, "Starcraft: TBG", "Rare game, no items available.", 0, 1, getCatId(c, "Sci-Fi"), getCatId(c, "Wargame"));

        return g;
    }

    private void createGame(Map<String, Long> map, String name, String desc, int good, int bad, Long... catIds) {
        try {
            long id = boardGameService.createBoardGame(name, desc);
            map.put(name, id);
            for (Long c : catIds) {
                if (c != null) categoryService.addBoardGameToCategory(id, c);
            }

            for (int i = 1; i <= good; i++) boardGameItemService.addBoardGameItem(id, name.substring(0, Math.min(3, name.length())).toUpperCase() + String.format("%03d", i), BoardGameState.FOR_LOAN);
            for (int i = 1; i <= bad; i++) boardGameItemService.addBoardGameItem(id, name.substring(0, Math.min(3, name.length())).toUpperCase() + "-DMG-" + i, BoardGameState.NOT_FOR_LOAN);
        } catch (Exception e) {
        }
    }

    private void createItems(Map<String, Long> games) {
        if (games.containsKey("Catan")) {
            boardGameItemService.addBoardGameItem(games.get("Catan"), "CAT-SPEC-001", BoardGameState.FOR_LOAN);
            boardGameItemService.addBoardGameItem(games.get("Catan"), "CAT-SPEC-002", BoardGameState.NOT_FOR_LOAN);
        }
    }

    private void createLoans(Map<String, Long> games, List<RegisteredUser> users) {
        if (users.size() < 10) return;

        safeCreatePastLoan(users.get(0), games, "Catan", -60, -50, false);
        safeCreatePastLoan(users.get(1), games, "Carcassonne", -55, -48, false);
        safeCreatePastLoan(users.get(2), games, "Wingspan", -40, -35, false);
        safeCreatePastLoan(users.get(3), games, "Azul", -30, -20, false);
        safeCreatePastLoan(users.get(4), games, "Pandemic", -20, -10, false);

        safeCreatePastLoan(users.get(5), games, "Scythe", -100, -10, true);
        safeCreatePastLoan(users.get(6), games, "Root", -90, -5, true);
        safeCreatePastLoan(users.get(0), games, "Gloomhaven", -200, -150, true);

        safeCreateActiveLoan(users.get(3), games, "Terraforming Mars", -5);
        safeCreateActiveLoan(users.get(4), games, "Brass: Birmingham", -1);
        safeCreateActiveLoan(users.get(5), games, "Dune: Imperium", 0);
        safeCreateActiveLoan(users.get(0), games, "Azul", -2);

        safeCreatePendingLoan(users.get(6), games, "Arkham Horror");
        safeCreatePendingLoan(users.get(7), games, "Nemesis");
        safeCreatePendingLoan(users.get(8), games, "Twilight Struggle");

        safeCreateRejectedLoan(users.get(9), games, "Secret Hitler");
        safeCreateRejectedLoan(users.get(8), games, "Catan");
        safeCreateRejectedLoan(users.get(2), games, "Pandemic");
    }

    private void safeCreatePastLoan(RegisteredUser user, Map<String, Long> games, String gameName, int d1, int d2, boolean late) {
        if (games.containsKey(gameName)) createPastLoan(user, gameName, d1, d2, late);
    }

    private void safeCreateActiveLoan(RegisteredUser user, Map<String, Long> games, String gameName, int d1) {
        if (games.containsKey(gameName)) createActiveLoan(user, gameName, d1);
    }

    private void safeCreatePendingLoan(RegisteredUser user, Map<String, Long> games, String gameName) {
        if (games.containsKey(gameName)) createPendingLoan(user, gameName);
    }

    private void safeCreateRejectedLoan(RegisteredUser user, Map<String, Long> games, String gameName) {
        if (games.containsKey(gameName)) createRejectedLoan(user, gameName);
    }

    private void createReviews(Map<String, Long> games, List<RegisteredUser> users) {
        if (users.size() < 5) return;

        RegisteredUser u1 = users.get(0);
        RegisteredUser u2 = users.get(2);
        RegisteredUser u3 = users.get(4);

        if (games.containsKey("Catan")) reviewService.createReview(u1.getId(), games.get("Catan"), "Classic.", 5);
        if (games.containsKey("Gloomhaven")) reviewService.createReview(u1.getId(), games.get("Gloomhaven"), "Heavy but good.", 4);
        if (games.containsKey("Wingspan")) reviewService.createReview(u2.getId(), games.get("Wingspan"), "Relaxing.", 5);
        if (games.containsKey("Root")) reviewService.createReview(u2.getId(), games.get("Root"), "Hard.", 2);

        if (games.containsKey("Catan")) boardGameService.addGameToFavorites(u1, games.get("Catan"));
        if (games.containsKey("Scythe")) boardGameService.addGameToFavorites(u1, games.get("Scythe"));
        if (games.containsKey("Wingspan")) boardGameService.addGameToFavorites(u2, games.get("Wingspan"));
    }

    private void createPastLoan(RegisteredUser user, String gameName, int daysAgoBorrowed, int daysAgoReturned, boolean late) {
        try {
            List<String> g = Collections.singletonList(gameName);
            long id = loanService.createLoan(LocalDate.now().plusDays(1), g, user.getId());
            BoardGameLoan loan = loanService.getBoardGameLoan(id);
            loan.setBorrowedAt(LocalDate.now().plusDays(daysAgoBorrowed));
            loan.setDueDate(LocalDate.now().plusDays(daysAgoBorrowed + 14));
            loan.setReturnedAt(LocalDate.now().plusDays(daysAgoReturned));

            if (late) {
                loan.setStatus(Status.RETURNED_LATE);
                user.setKarma(user.getKarma() - 10);
            } else {
                loan.setStatus(Status.RETURNED_IN_TIME);
                user.setKarma(Math.min(100, user.getKarma() + 5));
            }
            userRepository.save(user);
            for (BoardGameItem item : loan.getItems()) {
                item.setState(BoardGameState.FOR_LOAN);
            }
            loanRepository.save(loan);
        } catch (Exception e) { }
    }

    private void createActiveLoan(RegisteredUser user, String gameName, int daysAgo) {
        try {
            List<String> g = Collections.singletonList(gameName);
            long id = loanService.createLoan(LocalDate.now().plusDays(14), g, user.getId());
            loanService.approveGameLoan(id);
            BoardGameLoan loan = loanService.getBoardGameLoan(id);
            loan.setBorrowedAt(LocalDate.now().plusDays(daysAgo));
            loanRepository.save(loan);
        } catch (Exception e) { }
    }

    private void createPendingLoan(RegisteredUser user, String gameName) {
        try {
            List<String> g = Collections.singletonList(gameName);
            loanService.createLoan(LocalDate.now().plusDays(7), g, user.getId());
        } catch (Exception e) { }
    }

    private void createRejectedLoan(RegisteredUser user, String gameName) {
        try {
            List<String> g = Collections.singletonList(gameName);
            long id = loanService.createLoan(LocalDate.now().plusDays(7), g, user.getId());
            loanService.rejectGameLoan(id);
        } catch (Exception e) { }
    }
}