package com.hackintoshsa.watchlaterspring.services;

import com.hackintoshsa.watchlaterspring.models.User;
import com.hackintoshsa.watchlaterspring.models.WatchLater;
import com.hackintoshsa.watchlaterspring.repositories.UserRepository;
import com.hackintoshsa.watchlaterspring.repositories.WatchLaterRepository;
import lombok.extern.java.Log;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Log
public class WatchLaterService {
    @Autowired
    WatchLaterRepository watchLaterRepository;

    @Autowired
    UserRepository userRepository;


    public Map<String, Object> addMovieToWatchLater(String userId, WatchLater movie) {
        Map<String, Object> response = new HashMap<>();

        try{

            Optional<User> userOpt = userRepository.findById(userId);

//            User user = userRepository.findById(String.valueOf(new ObjectId(userId)))
//                    .orElseThrow(() -> new RuntimeException("User not found."));


            if (userOpt.isEmpty()) {
                response.put("status", 404);
                response.put("message", "User not found.");
                return response;
            }

            User user = userOpt.get();

            // Initialize the watchLaterMovies list if it's null
            if (user.getWatchLaterMovieIds() == null) {
                user.setWatchLaterMovieIds(new ArrayList<>());
            }


            for (Integer existingMovieId : user.getWatchLaterMovieIds()) {
                if (existingMovieId != null && existingMovieId.equals(movie.getMovieId())) {
                    response.put("status", 409);
                    response.put("message", "Movie already exists in your Watch Later list.");
                    return response;
                }
            }

//        if (user.getWatchLaterMovieIds().contains(movie.getMovieId())) {
//            response.put("status", 409);
//            response.put("message", "Movie already exists in your Watch Later list.");
//            return response;
//        }

            // Set movie details and persist to the database

            movie.setUserId(userId);
            movie.setCreatedAt(new Date());
            movie.setUpdatedAt(new Date());


            watchLaterRepository.save(movie);
            user.getWatchLaterMovieIds().add(movie.getMovieId());
            userRepository.save(user);

            List<WatchLater> watchLaterList = watchLaterRepository.findAllByUserId(userId);





            response.put("status", 200);
            response.put("message", "Movie added to Watch Later successfully.");
            response.put("data", watchLaterList);


        } catch (RuntimeException e) {
            response.put("status", "error");
            response.put("statusCode", 404);
            response.put("message", e.getMessage());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("statusCode", 500);
            response.put("message", "An error occurred while adding the movie.");
        }

        return response;
    }

    public Map<String, Object> listAllByUser(String userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            //Since we sub from Google oauth and its string we don't need to make it mongoObject
            //ObjectId userObjectId = new ObjectId(userId);

            // Fetch the watch later list from the repository using the user ID
           // List<WatchLater> watchLaterList = watchLaterRepository.listAllByUserId(userObjectId);
            List<WatchLater> watchLaterList = watchLaterRepository.findAllByUserId(userId);

            // Check if the list is empty and populate the response accordingly
            if (watchLaterList != null && !watchLaterList.isEmpty()) {
                response.put("status", "success");
                response.put("data", watchLaterList);
                response.put("message", "Successfully fetched all watch later items");
                response.put("statusCode", 200);
            } else {
                response.put("status", "not_found");
                response.put("StatusCode", 404);
                response.put("message", "No watch later items found for this user.");
            }
        } catch (IllegalArgumentException e) {
            response.put("status", "error");
            response.put("message", "Invalid user ID format.");
            response.put("statusCode", 400);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "An error occurred while fetching watch later items.");
            response.put("statusCode", 500);
        }

        return response;
    }

    public Map<String, Object> deleteMovieFromWatchLater(Integer movieId) {
        Map<String, Object> response = new HashMap<>();

        log.warning("Attempting to delete movie with ID: " + movieId);

        try {
            // Fetch all WatchLater entries with the given movieId
            Optional<WatchLater> movieList = watchLaterRepository.findAllByMovieId(movieId);

            log.warning("Found movies: " + movieList);

            if (movieList.isEmpty()) {
                response.put("status", "not_found");
                response.put("statusCode", 404);
                response.put("message", "Movie not found in Watch Later list.");
                return response;
            }

            // For simplicity, let's assume we're deleting the first occurrence of the movie.
            WatchLater movieToDelete = movieList.get();

            // Fetch user by ID associated with the movie
            Optional<User> userOpt = userRepository.findById(movieToDelete.getUserId());

            if (userOpt.isEmpty()) {
                response.put("status", "not_found");
                response.put("statusCode", 404);
                response.put("message", "User not found.");
                return response;
            }

            User user = userOpt.get();

            if (user.getWatchLaterMovieIds().contains(movieToDelete.getMovieId())) {
                user.getWatchLaterMovieIds().remove(movieToDelete.getMovieId());
                // Save the updated user entity
                userRepository.save(user);
            } else {
                response.put("status", "not_found");
                response.put("statusCode", 404);
                response.put("message", "Movie not found in user's Watch Later list.");
                return response;
            }

            // Remove the movie from the user's Watch Later movie list
            user.getWatchLaterMovieIds().remove(movieToDelete.getMovieId());

            // Save the updated user entity
            userRepository.save(user);

            // Delete the movie from WatchLater repository
            watchLaterRepository.deleteByMovieId(movieToDelete.getMovieId());

            response.put("status", "success");
            response.put("statusCode", 200);
            response.put("message", "Movie successfully removed from Watch Later list.");

        } catch (Exception e) {
            response.put("status", "error");
            response.put("statusCode", 500);
            response.put("message", "An error occurred while deleting the movie: " + e.getMessage());
        }

        return response;
    }


}
