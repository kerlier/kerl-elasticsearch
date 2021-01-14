package com.fashion.elasticseach.controller;


import com.fashion.elasticseach.interfaces.CodeMapService;
import com.fashion.elasticseach.pojo.CodeRequest;
import com.fashion.elasticseach.pojo.IdSourceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/code/add2")
    public String addCode2(@RequestBody CodeRequest request){
        codeMapService.addCode(request.getCode1(),request.getCode2());
        return "添加成功" ;
    }

    @PostMapping("/code/relate")
    public List<IdSourceInfo> relateCode(@RequestBody IdSourceInfo code){
        return codeMapService.getRelateCode(code);
    }
}
