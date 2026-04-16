package edu.ub.pis2526.projecte.domain.repositories;

import java.util.List;

import edu.ub.pis2526.projecte.Event;

public interface EventRepository {
    void save(Event evento, OnSuccessListener onSuccess, OnFailureListener onFailure);
    void getAll(OnEventsLoadedListener onLoaded, OnFailureListener onFailure);

    interface OnSuccessListener {
        void onSuccess();
    }
    interface OnEventsLoadedListener {
        void onEventsLoaded(List<Event> eventos);
    }

    interface OnFailureListener {
        void onFailure(Exception e);
    }
}