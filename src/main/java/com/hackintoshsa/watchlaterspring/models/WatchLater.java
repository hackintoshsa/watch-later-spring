package com.hackintoshsa.watchlaterspring.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "watch-later")
public class WatchLater {

    private Integer movieId; // The ID from the Movie Database API
    private String title;
    private String overview;
    private String poster_path;
    private String backdrop_path;
    private String release_date;
    private String media_type;
    private Boolean video;
    private ObjectId userId;
    private Date createdAt = new Date();
    private Date updatedAt = new Date();
}
