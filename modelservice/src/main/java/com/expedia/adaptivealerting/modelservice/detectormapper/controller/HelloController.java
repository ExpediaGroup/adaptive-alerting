package com.expedia.adaptivealerting.modelservice.detectormapper.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example controller for /api requests.
 */
@Api(tags = "demo")
@RestController
@RequestMapping("/api")
@Slf4j
public class HelloController {

  @Value("${test.helloMsg}")
  String message;

  /**
   * Controller to say hello.
   *
   * @param message {@link String} a hello message.
   * @return {@link ResponseEntity} a response reflecting the message sent.
   */
  @RequestMapping(value = "hello", produces = "application/json", method = RequestMethod.GET)
  @ApiOperation(value = "Say Hello")
  public ResponseEntity<HelloMessage> hello(
      @ApiParam(value = "Hello message") @RequestParam(value = "message", required = false) String message) {
    final HelloMessage helloMessage = new HelloMessage();
    helloMessage.setMessage(message == null ? this.message : message);

    log.info("Saying " + this.message);

    return new ResponseEntity<>(helloMessage, HttpStatus.OK);
  }
}
