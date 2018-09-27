package com.expedia.aquila.detect.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Willie Wheeler
 * @author Karan Shah
 */
@RestController
public class AliveController {
    
    @RequestMapping(value = "/alive", method = RequestMethod.GET)
    public String alive() {
        return "true";
    }
}
