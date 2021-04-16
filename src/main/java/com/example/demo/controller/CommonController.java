package com.example.demo.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class CommonController {
	protected static final Logger log= LoggerFactory.getLogger(CommonController.class);

    /**
     * 字串為空
     * @param value
     * @return
     */
    public static boolean isNullOrSpace(String value){

        if(value==null){
            return true;
        }
        else {
            if(value.equals("")){
                return true;
            }
            else {
                return false;
            }
        }
    }
}
