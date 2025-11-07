package cz.cvut.fel.ear.service;

import cz.cvut.fel.ear.model.LoanStatus;
import cz.cvut.fel.ear.model.RegisteredUser;
import cz.cvut.fel.ear.service.interfaces.UserServiceI;

public class UserService implements UserServiceI {
    private final int KARMA_UP = 10;
    private final int KARMA_DOWN = 5;
    private final int KARMA_MAX = 100;

    @Override
    public void updateKarma(RegisteredUser user, LoanStatus status) {
        if (status == LoanStatus.RETURNED_LATE && (user.getKarma() > 4)) {
            user.setKarma(user.getKarma() - KARMA_DOWN);
        } else if (status == LoanStatus.RETURNED_IN_TIME && (user.getKarma() < 91)) {
            user.setKarma(user.getKarma() + KARMA_UP);
        } else {
            user.setKarma(KARMA_MAX);
        }
    }
}
