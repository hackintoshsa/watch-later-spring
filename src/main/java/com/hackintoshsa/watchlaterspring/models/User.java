package com.hackintoshsa.watchlaterspring.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id; //id
    private String name;
    private String email;
    private String picture;
    private String nickname;
    private Boolean verified = false;
    private List<Integer> watchLaterMovieIds;

}
