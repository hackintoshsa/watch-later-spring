package com.hackintoshsa.watchlaterspring.services;

import com.hackintoshsa.watchlaterspring.models.User;
import com.hackintoshsa.watchlaterspring.models.WatchLater;
import com.hackintoshsa.watchlaterspring.repositories.UserRepository;
import com.hackintoshsa.watchlaterspring.repositories.WatchLaterRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WatchLaterService {
    @Autowired
    WatchLaterRepository watchLaterRepository;

    @Autowired
    UserRepository userRepository;


    public Map<String, Object> addMovieToWatchLater(String userId, WatchLater movie) {
        Map<String, Object> response = new HashMap<>();

        try{
            // Retrieve the user from the database using ObjectId
            Optional<User> userOpt = userRepository.findById(String.valueOf(new ObjectId(userId)));

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

            movie.setUserId(new ObjectId(userId));
            movie.setCreatedAt(new Date());
            movie.setUpdatedAt(new Date());


            watchLaterRepository.save(movie);
            user.getWatchLaterMovieIds().add(movie.getMovieId());
            userRepository.save(user);


            response.put("status", 200);
            response.put("message", "Movie added to Watch Later successfully.");
            response.put("data", movie);


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
            ObjectId userObjectId = new ObjectId(userId);

            // Fetch the watch later list from the repository using the user ID
           // List<WatchLater> watchLaterList = watchLaterRepository.listAllByUserId(userObjectId);
            List<WatchLater> watchLaterList = watchLaterRepository.findAllByUserId(userObjectId);

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

    public Map<String, Object> deleteMovieFromWatchLater(String movieId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Delete the movie from the WatchLater list based on the movieId
            //WatchLater deletedMovie = watchLaterRepository.deleteById(movieId);
            Optional<WatchLater> movieOptional = watchLaterRepository.findById(movieId);

            if (movieOptional.isEmpty()) {
                response.put("status", "not_found");
                response.put("statusCode", 404);
                response.put("message", "Movie not found in Watch Later list.");
                return response;
            }

            WatchLater movie = movieOptional.get();
            Optional<User> userOpt = userRepository.findById(movie.getUserId().toString());

            if (userOpt.isEmpty()) {
                response.put("status", "not_found");
                response.put("statusCode", 404);
                response.put("message", "User not found.");
                return response;
            }

            User user = userOpt.get();
            user.getWatchLaterMovieIds().remove(movie.getMovieId());

            userRepository.save(user);
            watchLaterRepository.deleteById(movieId);

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
