package com.mynger.mychatapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mynger.mychatapp.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>{

    Optional<Message> findByAuthor(String author);
}
