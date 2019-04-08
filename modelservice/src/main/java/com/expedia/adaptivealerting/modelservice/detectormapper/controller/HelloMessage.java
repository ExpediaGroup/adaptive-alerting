package com.expedia.adaptivealerting.modelservice.detectormapper.controller;

import io.swagger.annotations.ApiModelProperty;

import lombok.Data;

import java.util.Date;

@Data
public class HelloMessage {

  @ApiModelProperty(value = "The message returned")
  private String message;

  @ApiModelProperty(value = "The date returned")
  private final String date = new Date().toString();
}
