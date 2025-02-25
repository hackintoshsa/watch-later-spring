package com.hackintoshsa.watchlaterspring.repositories;

import com.hackintoshsa.watchlaterspring.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findBy(String sub);
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailOrId(String email, String id);
}
