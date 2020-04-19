package com.fashion.elasticseach.controller;


import com.fashion.elasticseach.interfaces.CodeMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeServiceController {

    @Autowired
    private CodeMapService codeMapService;

    @RequestMapping("/code/add")
    public String addCode(){
        codeMapService.addCodeMap();
        return  "添加成功";
    }

    @RequestMapping("/code/delete")
    public String deleteCode(){
        codeMapService.deleteCodeMap();
        return  "刪除成功";
    }

}
