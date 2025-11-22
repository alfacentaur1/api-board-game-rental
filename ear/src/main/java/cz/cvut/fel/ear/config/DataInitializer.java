package cz.cvut.fel.ear.config;

import cz.cvut.fel.ear.dao.*;
import cz.cvut.fel.ear.dto.UserRegistrationDTO;
import cz.cvut.fel.ear.model.*;
import cz.cvut.fel.ear.service.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final BoardGameService boardGameService;
    private final BoardGameItemService boardGameItemService;
    private final CategoryService categoryService;
    private final LoanService loanService;
    private final ReviewService reviewService;
    private final BoardGameLoanRepository loanRepository;
    private final UserRepository userRepository;

    public DataInitializer(UserService userService, BoardGameService boardGameService,
                           BoardGameItemService boardGameItemService, CategoryService categoryService,
                           LoanService loanService, ReviewService reviewService,
                           BoardGameLoanRepository loanRepository, UserRepository userRepository) {
        this.userService = userService;
        this.boardGameService = boardGameService;
        this.boardGameItemService = boardGameItemService;
        this.categoryService = categoryService;
        this.loanService = loanService;
        this.reviewService = reviewService;
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("🚀 STARTING DATA INITIALIZATION...");

        // 1. Vytvoříme uživatele a získáme jejich seznam
        List<RegisteredUser> users = createRoles();

        // 2. Vytvoříme kategorie a hry
        Map<String, Long> categories = createCategories();
        Map<String, Long> games = createGames(categories);

        // 3. Vytvoříme kusy her
        createItems(games);

        // 4. Vytvoříme výpůjčky (používáme seznam uživatelů, aby to nespadlo)
        createLoans(games, users);

        // 5. Vytvoříme recenze
        createReviews(games, users);

        System.out.println("✅ MASSIVE DATA INITIALIZATION COMPLETE");
    }

    private List<RegisteredUser> createRoles() {
        List<RegisteredUser> createdUsers = new ArrayList<>();
        try {
            // Admini
            userService.registerAdmin(new UserRegistrationDTO("System Admin", "admin@ear.cz", "12345", "admin"));
            userService.registerAdmin(new UserRegistrationDTO("Head Librarian", "librarian@ear.cz", "12345", "librarian"));

            // Běžní uživatelé (vytvoříme jich 15)
            String[] names = {"John", "Jane", "Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Hank", "Ivy", "Jack", "Kevin", "Laura", "Mike"};

            for (int i = 0; i < names.length; i++) {
                String username = names[i].toLowerCase() + i; // např. john0, jane1...
                try {
                    userService.registerUser(new UserRegistrationDTO(
                            names[i] + " Doe",
                            username + "@example.com",
                            "12345",
                            username
                    ));
                } catch (Exception ignored) {
                    // Uživatel už možná existuje, nevadí
                }
                // Přidáme do seznamu pro další použití
                createdUsers.add((RegisteredUser) userService.getUserByUsername(username));
            }
        } catch (Exception e) {
            System.out.println("Error creating roles: " + e.getMessage());
        }
        return createdUsers;
    }

    private Map<String, Long> createCategories() {
        Map<String, Long> map = new HashMap<>();
        String[] cats = {"Strategy", "Family", "Party", "Abstract", "Thematic", "Wargame", "Eurogame", "Children", "Cooperative", "Deckbuilding", "Dexterity", "Puzzle", "Sci-Fi", "Fantasy", "Horror", "Adventure"};
        for (String c : cats) {
            try {
                map.put(c, categoryService.addCategory(c));
            } catch (Exception ignored) {}
        }
        return map;
    }

    private Map<String, Long> createGames(Map<String, Long> c) {
        Map<String, Long> g = new HashMap<>();

        createGame(g, "Catan", "Collect resources and build settlements.", 4, 2, c.get("Strategy"), c.get("Family"));
        createGame(g, "Carcassonne", "Tile-placement game of landscapes.", 5, 1, c.get("Family"), c.get("Strategy"));
        createGame(g, "Ticket to Ride", "Build train routes across the map.", 6, 0, c.get("Family"));
        createGame(g, "Pandemic", "Cooperative game to save the world from viruses.", 4, 1, c.get("Cooperative"), c.get("Strategy"));
        createGame(g, "Gloomhaven", "Tactical combat in a fantasy world.", 2, 0, c.get("Fantasy"), c.get("Strategy"), c.get("Thematic"));
        createGame(g, "Terraforming Mars", "Corporations transforming Mars.", 5, 2, c.get("Sci-Fi"), c.get("Strategy"), c.get("Eurogame"));
        createGame(g, "Scythe", "Alternate-history 1920s mech warfare.", 3, 0, c.get("Strategy"), c.get("Sci-Fi"));
        createGame(g, "Wingspan", "Bird-collection engine-building game.", 4, 0, c.get("Family"), c.get("Strategy"));
        createGame(g, "Azul", "Drafting beautiful tiles.", 5, 0, c.get("Abstract"), c.get("Family"));
        createGame(g, "Codenames", "Word association team game.", 6, 1, c.get("Party"));
        createGame(g, "7 Wonders", "Civilization building card game.", 4, 0, c.get("Strategy"), c.get("Family"));
        createGame(g, "Splendor", "Gem collection and development.", 5, 1, c.get("Family"), c.get("Strategy"));
        createGame(g, "Dominion", "The original deck-building game.", 4, 1, c.get("Deckbuilding"), c.get("Strategy"));
        createGame(g, "Arkham Horror", "Lovecraftian cooperative mystery.", 2, 1, c.get("Horror"), c.get("Thematic"), c.get("Cooperative"));
        createGame(g, "Root", "Asymmetric warfare in the woodlands.", 3, 0, c.get("Wargame"), c.get("Strategy"));
        createGame(g, "Dixit", "Imaginative storytelling with cards.", 5, 0, c.get("Party"), c.get("Family"));
        createGame(g, "King of Tokyo", "Dice game of monster combat.", 4, 1, c.get("Family"), c.get("Party"));
        createGame(g, "The Crew", "Cooperative trick-taking in space.", 6, 0, c.get("Sci-Fi"), c.get("Cooperative"));
        createGame(g, "Patchwork", "Two-player puzzle game.", 3, 0, c.get("Abstract"), c.get("Puzzle"));
        createGame(g, "Spirit Island", "Spirits defending their island from invaders.", 3, 0, c.get("Cooperative"), c.get("Strategy"));
        createGame(g, "Brass: Birmingham", "Industrial revolution economic strategy.", 2, 0, c.get("Eurogame"), c.get("Strategy"));
        createGame(g, "Dune: Imperium", "Deck-building and worker placement on Arrakis.", 4, 0, c.get("Sci-Fi"), c.get("Strategy"));
        createGame(g, "Everdell", "Building a city of critters.", 3, 1, c.get("Family"), c.get("Strategy"));
        createGame(g, "Great Western Trail", "Cattle herding across America.", 2, 0, c.get("Eurogame"), c.get("Strategy"));
        createGame(g, "Twilight Struggle", "Cold War strategy game.", 2, 0, c.get("Wargame"), c.get("Strategy"));
        createGame(g, "Star Wars: Rebellion", "Galactic civil war.", 1, 0, c.get("Sci-Fi"), c.get("Wargame"), c.get("Thematic"));
        createGame(g, "War of the Ring", "Lord of the Rings wargame.", 1, 0, c.get("Fantasy"), c.get("Wargame"));
        createGame(g, "Through the Ages", "Civilization building.", 2, 0, c.get("Strategy"), c.get("Eurogame"));
        createGame(g, "Concordia", "Trading in the Roman Empire.", 3, 0, c.get("Strategy"), c.get("Eurogame"));
        createGame(g, "Viticulture", "Winemaking strategy game.", 3, 0, c.get("Strategy"), c.get("Eurogame"));
        createGame(g, "Mansions of Madness", "App-driven Lovecraftian horror.", 2, 1, c.get("Horror"), c.get("Thematic"), c.get("Cooperative"));
        createGame(g, "Blood Rage", "Viking area control.", 3, 0, c.get("Strategy"), c.get("Thematic"));
        createGame(g, "Mechs vs. Minions", "Cooperative programming game.", 2, 0, c.get("Cooperative"), c.get("Thematic"));
        createGame(g, "Decrypto", "Team-based deduction game.", 4, 0, c.get("Party"));
        createGame(g, "Secret Hitler", "Social deduction and hidden roles.", 5, 1, c.get("Party"));
        createGame(g, "The Resistance", "Dystopian social deduction.", 5, 0, c.get("Party"));
        createGame(g, "Santorini", "Abstract strategy with gods.", 3, 0, c.get("Abstract"), c.get("Strategy"));
        createGame(g, "Hive", "Abstract bug-placement game.", 3, 0, c.get("Abstract"));
        createGame(g, "Cascadia", "Nature tile-laying game.", 4, 0, c.get("Abstract"), c.get("Family"));
        createGame(g, "Lost Ruins of Arnak", "Deck-building adventure.", 3, 0, c.get("Strategy"), c.get("Adventure"));
        createGame(g, "Nemesis", "Survival horror on a spaceship.", 2, 1, c.get("Sci-Fi"), c.get("Horror"));
        createGame(g, "Heat: Pedal to the Metal", "Racing game.", 4, 0, c.get("Family"), c.get("Thematic"));
        createGame(g, "Just One", "Cooperative party word game.", 5, 0, c.get("Party"), c.get("Cooperative"));
        createGame(g, "Crokinole", "Dexterity board game.", 2, 0, c.get("Dexterity"));
        createGame(g, "Klask", "Magnetic dexterity game.", 2, 0, c.get("Dexterity"));

        return g;
    }

    private void createGame(Map<String, Long> map, String name, String desc, int good, int bad, long... cats) {
        try {
            long id = boardGameService.createBoardGame(name, desc);
            map.put(name, id);
            for (long c : cats) categoryService.addBoardGameToCategory(id, c);

            for (int i = 1; i <= good; i++) boardGameItemService.addBoardGameItem(id, name.substring(0,3).toUpperCase() + String.format("%03d", i), BoardGameState.FOR_LOAN);
            for (int i = 1; i <= bad; i++) boardGameItemService.addBoardGameItem(id, name.substring(0,3).toUpperCase() + "-DMG-" + i, BoardGameState.NOT_FOR_LOAN);
        } catch (Exception ignored) {}
    }

    private void createItems(Map<String, Long> games) {
        if (games.containsKey("Catan")) {
            boardGameItemService.addBoardGameItem(games.get("Catan"), "CAT-SPEC-001", BoardGameState.FOR_LOAN);
            boardGameItemService.addBoardGameItem(games.get("Catan"), "CAT-SPEC-002", BoardGameState.NOT_FOR_LOAN);
        }
    }

    private void createLoans(Map<String, Long> games, List<RegisteredUser> users) {
        if (users.size() < 5) return; // Pojistka proti malému počtu uživatelů

        // History: Returned In Time
        if (games.containsKey("Catan")) createPastLoan(users.get(0), "Catan", -60, -50, false);
        if (games.containsKey("Carcassonne")) createPastLoan(users.get(1), "Carcassonne", -55, -48, false);
        if (games.containsKey("Wingspan")) createPastLoan(users.get(2), "Wingspan", -40, -35, false);
        if (games.containsKey("Azul")) createPastLoan(users.get(3), "Azul", -30, -20, false);
        if (games.containsKey("Pandemic")) createPastLoan(users.get(4), "Pandemic", -20, -10, false);

        // History: Returned Late
        if (users.size() > 6 && games.containsKey("Scythe")) createPastLoan(users.get(5), "Scythe", -100, -10, true);
        if (users.size() > 7 && games.containsKey("Root")) createPastLoan(users.get(6), "Root", -90, -5, true);
        if (games.containsKey("Gloomhaven")) createPastLoan(users.get(0), "Gloomhaven", -200, -150, true);

        // Currently Active (Approved)
        if (games.containsKey("Terraforming Mars")) createActiveLoan(users.get(0), "Terraforming Mars", -5);
        if (games.containsKey("Ticket to Ride")) createActiveLoan(users.get(1), "Ticket to Ride", -2);

        // Currently Pending
        if (users.size() > 8 && games.containsKey("Arkham Horror")) createPendingLoan(users.get(4), "Arkham Horror");
        if (games.containsKey("Nemesis")) createPendingLoan(users.get(5), "Nemesis");

        // Rejected
        if (games.containsKey("Secret Hitler")) createRejectedLoan(users.get(1), "Secret Hitler");
    }

    private void createReviews(Map<String, Long> games, List<RegisteredUser> users) {
        if (users.size() < 5) return;

        RegisteredUser u1 = users.get(0);
        RegisteredUser u2 = users.get(2);
        RegisteredUser u3 = users.get(4);

        if (games.containsKey("Catan")) reviewService.createReview(u1.getId(), games.get("Catan"), "The classic gateway game. Always good.", 5);
        if (games.containsKey("Gloomhaven")) reviewService.createReview(u1.getId(), games.get("Gloomhaven"), "Too heavy to setup, but great gameplay.", 4);
        if (games.containsKey("Wingspan")) reviewService.createReview(u2.getId(), games.get("Wingspan"), "Beautiful artwork and relaxing gameplay.", 5);
        if (games.containsKey("Azul")) reviewService.createReview(u3.getId(), games.get("Azul"), "Abstract masterpiece.", 5);

        if (games.containsKey("Catan")) boardGameService.addGameToFavorites(u1, games.get("Catan"));
        if (games.containsKey("Scythe")) boardGameService.addGameToFavorites(u1, games.get("Scythe"));
        if (games.containsKey("Wingspan")) boardGameService.addGameToFavorites(u2, games.get("Wingspan"));
    }

    private void createPastLoan(RegisteredUser user, String gameName, int daysAgoBorrowed, int daysAgoReturned, boolean late) {
        try {
            List<String> g = Collections.singletonList(gameName);
            long id = loanService.createLoan(LocalDateTime.now().plusDays(1), g, user.getId());
            BoardGameLoan loan = loanService.getBoardGameLoan(id);
            loan.setBorrowedAt(LocalDateTime.now().plusDays(daysAgoBorrowed));
            loan.setDueDate(LocalDateTime.now().plusDays(daysAgoBorrowed + 14));
            loan.setReturnedAt(LocalDateTime.now().plusDays(daysAgoReturned));

            if (late) {
                loan.setStatus(Status.returnedLate);
                user.setKarma(user.getKarma() - 10);
            } else {
                loan.setStatus(Status.returnedInTime);
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
            long id = loanService.createLoan(LocalDateTime.now().plusDays(14), g, user.getId());
            loanService.approveGameLoan(id);
            BoardGameLoan loan = loanService.getBoardGameLoan(id);
            loan.setBorrowedAt(LocalDateTime.now().plusDays(daysAgo));
            loanRepository.save(loan);
        } catch (Exception e) { }
    }

    private void createPendingLoan(RegisteredUser user, String gameName) {
        try {
            List<String> g = Collections.singletonList(gameName);
            loanService.createLoan(LocalDateTime.now().plusDays(7), g, user.getId());
        } catch (Exception e) { }
    }

    private void createRejectedLoan(RegisteredUser user, String gameName) {
        try {
            List<String> g = Collections.singletonList(gameName);
            long id = loanService.createLoan(LocalDateTime.now().plusDays(7), g, user.getId());
            loanService.rejectGameLoan(id);
        } catch (Exception e) { }
    }
}