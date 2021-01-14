package com.fashion.elasticseach.interfaces;

import com.fashion.elasticseach.pojo.CodeSourceInfo;
import com.fashion.elasticseach.pojo.IdSourceInfo;

import java.util.List;

public interface CodeMapService {

    boolean addCodeMap();

    boolean deleteCodeMap();

    boolean addCode(IdSourceInfo code1, IdSourceInfo code2);

    List<IdSourceInfo> getRelateCode(IdSourceInfo code);
}
