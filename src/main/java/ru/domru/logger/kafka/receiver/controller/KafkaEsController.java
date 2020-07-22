package ru.domru.logger.kafka.receiver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class KafkaEsController {

    final Logger logger = LoggerFactory.getLogger(KafkaEsController.class);

    @GetMapping(value = {"/", "/{msg}"})
    public ResponseEntity<String> echo(@PathVariable(name = "msg", required = false) String msg) {
        if (msg == null) {
            msg = "-";
        }
        return new ResponseEntity<>("ok:" + msg, HttpStatus.OK);
    }
}
