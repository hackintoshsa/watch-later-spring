package com.hackintoshsa.watchlaterspring.repositories;

import com.hackintoshsa.watchlaterspring.models.WatchLater;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchLaterRepository extends MongoRepository<WatchLater, String> {
    WatchLater findByUserId(Object userId);

    void deleteById(String movieId);

    boolean existsByMovieId(Integer movieId); // Fix return type

    List<WatchLater> findAllByMovieId(String movieId); // Fix method name

    List<WatchLater> findAllByUserId(Object userId); // Fix method name

}
