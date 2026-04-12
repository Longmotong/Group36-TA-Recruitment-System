package com.taapp.data;

import com.taapp.model.CurrentUser;
import com.taapp.model.CurrentUserHolder;
import com.taapp.model.Statistics;
import com.taapp.model.TA;

import java.util.List;

public class DataStore {
    private static final DataStore INSTANCE = new DataStore();

    private final CurrentUser currentUser;
    private final List<TA> tas;
    private final Statistics statistics;

    private DataStore() {
        this.currentUser = CurrentUserHolder.toAdminCurrentUser();
        this.tas = MockData.getMockTAs();
        this.statistics = MockData.getStatistics();
    }

    public static DataStore defaultStore() {
        return INSTANCE;
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public List<TA> getTAs() {
        return tas;
    }
}
