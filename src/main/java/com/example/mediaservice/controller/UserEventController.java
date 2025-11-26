package com.example.mediaservice.controller;

import com.example.mediaservice.entity.User;
import com.example.mediaservice.producer.UserProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-events")
public class UserEventController {

    @Autowired
    private UserProducerService userProducerService;


    @PostMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody User user) {
        try {
            userProducerService.send(user);
            return ResponseEntity.accepted().body("User update request accepted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process user update request");
        }
    }



    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody User user) {
        try {
            userProducerService.create(user);
            return ResponseEntity.accepted().body("User create request accepted");
        }
        catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process user create request");
        }
    }
}
