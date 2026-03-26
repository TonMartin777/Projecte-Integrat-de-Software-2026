package edu.ub.pis2526.projecte.domain.repositories;

import edu.ub.pis2526.projecte.Event;

public interface EventRepository {
    void save(Event evento, OnSuccessListener onSuccess, OnFailureListener onFailure);

    interface OnSuccessListener {
        void onSuccess();
    }

    interface OnFailureListener {
        void onFailure(Exception e);
    }
}