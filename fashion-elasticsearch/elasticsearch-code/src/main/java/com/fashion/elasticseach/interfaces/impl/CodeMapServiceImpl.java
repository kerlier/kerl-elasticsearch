package com.fashion.elasticseach.interfaces.impl;

import com.alibaba.fastjson.JSONObject;
import com.fashion.elasticseach.interfaces.CodeMapService;
import com.fashion.elasticseach.pojo.CodeMappedState;
import com.fashion.elasticseach.pojo.CodeSourceInfo;
import com.fashion.elasticseach.pojo.IdSourceInfo;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.util.*;

@Service
public class CodeMapServiceImpl implements CodeMapService {

    @Value("${index.name}")
    private String INDEX_NAME;

    @Value("${type.name}")
    private String TYPE_NAME;

    private Integer DEPTH = 10;
    private static CodeSourceInfo code1;

    private static CodeSourceInfo code2;

    private static Map<String, IdSourceInfo> codeIdMaps;

    static {
        codeIdMaps = new HashMap<>();
        code1 = new CodeSourceInfo();
        code1.setCode("I100");
        code1.setSource("2018");
        code1.setVersion("1");
        IdSourceInfo idSourceInfo1 = new IdSourceInfo();
        idSourceInfo1.setName("C000");
        idSourceInfo1.setSource("ICD100");
        idSourceInfo1.setVersion("aa");
        codeIdMaps.put("I100",idSourceInfo1);

        code2 = new CodeSourceInfo();
        code2.setCode("I200");
        code2.setSource("2019");
        code2.setVersion("1");
        IdSourceInfo idSourceInfo2 = new IdSourceInfo();
        idSourceInfo2.setName("C11");
        idSourceInfo2.setSource("ICD99");
        idSourceInfo2.setVersion("bb");
        codeIdMaps.put("I200",idSourceInfo2);
    }

    @Resource
    private TransportClient client;

    @Override
    public boolean addCodeMap() {
        boolean success = true;
        try {
            String mappedId = getCodeMappedId(code1, code2);

            IdSourceInfo idSourceInfo1= codeIdMaps.get(code1.getCode());
            IdSourceInfo idSourceInfo2= codeIdMaps.get(code2.getCode());

            idSourceInfo1.setCodeMappedId(mappedId);
            idSourceInfo2.setCodeMappedId(mappedId);

            BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
            //更新code1
            CodeMappedState codeMappedState1 = searchCodeMappedState(idSourceInfo1);
            updateCodeMappedState(bulkRequestBuilder,codeMappedState1,idSourceInfo1,idSourceInfo2);

            //更新code2
            CodeMappedState codeMappedState2 = searchCodeMappedState(idSourceInfo2);
            updateCodeMappedState(bulkRequestBuilder,codeMappedState2,idSourceInfo2,idSourceInfo1);

            bulkRequestBuilder.execute().actionGet();
        }catch (Exception e){
            success = false;
            e.printStackTrace();
        }
        return success;
    }

    @Override
    public boolean deleteCodeMap() {
        boolean success = true;
        try {
            BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
            String mappedId = getCodeMappedId(code1, code2);
            updateCodeMappedStateById(bulkRequestBuilder,mappedId);
            bulkRequestBuilder.execute().actionGet();
        }catch (Exception e){
            success = false;
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean addCode(IdSourceInfo code1, IdSourceInfo code2) {

        try {
            String mappedId = getCodeMappedId(code1.getName(), code2.getName());
            code1.setCodeMappedId(mappedId);
            code2.setCodeMappedId(mappedId);
            BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
            //更新code1
            CodeMappedState codeMappedState1 = searchCodeMappedState(code1);
            updateCodeMappedState(bulkRequestBuilder,codeMappedState1,code1,code2);

            //更新code2
            CodeMappedState codeMappedState2 = searchCodeMappedState(code2);
            updateCodeMappedState(bulkRequestBuilder,codeMappedState2,code2,code1);

            bulkRequestBuilder.execute().actionGet();
        }catch (Exception e){
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public List<IdSourceInfo> getRelateCode(IdSourceInfo code) {
        List<IdSourceInfo> relateCodes = new ArrayList<>();
        List<String> uniqueNames = new ArrayList<>();
        uniqueNames.add(code.getName());
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX_NAME).setTypes(TYPE_NAME);
        search(relateCodes,uniqueNames,searchRequestBuilder,code,0);
        return relateCodes;
    }

    private void search(List<IdSourceInfo> relateCodes,List<String> uniqueNames,
                        SearchRequestBuilder searchRequestBuilder,IdSourceInfo code,int depth){
        if(depth>=DEPTH){
            return ;
        }
        SearchResponse searchResponse = searchRequestBuilder.setQuery(
                QueryBuilders.termQuery("currentCode.name.keyword", code.getName()))
                .execute().actionGet();

        List<IdSourceInfo> thisLoopCode = new ArrayList<>();
        if(searchResponse.getHits().totalHits> 0) {
            SearchHit firstHits = searchResponse.getHits().getHits()[0];
            String json = firstHits.getSourceAsString();
            CodeMappedState mappedState = JSONObject.parseObject(json, CodeMappedState.class);
            List<IdSourceInfo> codes = mappedState.getRelatedCodes();
            for (IdSourceInfo idSourceInfo : codes) {
                if(!uniqueNames.contains(idSourceInfo.getName())){
                    relateCodes.add(idSourceInfo);
                    thisLoopCode.add(idSourceInfo);
                    uniqueNames.add(idSourceInfo.getName());
                }
            }
        }
        for (IdSourceInfo idSourceInfo : thisLoopCode) {
            search(relateCodes,uniqueNames,searchRequestBuilder,idSourceInfo,depth+1);
        }
    }

    private CodeMappedState searchCodeMappedState( IdSourceInfo idSourceInfo){
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX_NAME).setTypes(TYPE_NAME);
        SearchResponse searchResponse = searchRequestBuilder.setQuery(
                QueryBuilders.termQuery("currentCode.name.keyword", idSourceInfo.getName()))
                .execute().actionGet();
        if(searchResponse.getHits().totalHits> 0) {
            SearchHit firstHits = searchResponse.getHits().getHits()[0];
            String json = firstHits.getSourceAsString();
            CodeMappedState mappedState = JSONObject.parseObject(json, CodeMappedState.class);
            mappedState.setEsId(firstHits.getId());
            return mappedState;
        }

        return null;
    }

    private void updateCodeMappedStateById(BulkRequestBuilder bulkRequestBuilder,String mappedId){
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch(INDEX_NAME).setTypes(TYPE_NAME);

        NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("relatedCodes",
                QueryBuilders.termQuery("relatedCodes.codeMappedId", mappedId),
                ScoreMode.None);
        System.out.println(nestedQueryBuilder.toString());
        SearchResponse searchResponse = searchRequestBuilder.setQuery(nestedQueryBuilder)
                .execute().actionGet();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit:searchHits) {
            String json = searchHit.getSourceAsString();
            CodeMappedState mappedState = JSONObject.parseObject(json, CodeMappedState.class);
            List<IdSourceInfo> relatedCodes = mappedState.getRelatedCodes();
            relatedCodes.removeIf(x->Objects.equals(x.getCodeMappedId(),mappedId));

            UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate(INDEX_NAME, TYPE_NAME, searchHit.getId())
                    .setDoc(JSONObject.toJSONString(mappedState), XContentType.JSON);

            bulkRequestBuilder.add(updateRequestBuilder);
        }
    }

    private void updateCodeMappedState(BulkRequestBuilder bulkRequestBuilder,CodeMappedState codeMappedState,IdSourceInfo currentSourceInfo,IdSourceInfo relatedSourceInfo){
        if(null == codeMappedState){
            codeMappedState = new CodeMappedState();
            codeMappedState.setCurrentCode(currentSourceInfo);
            codeMappedState.getRelatedCodes().add(relatedSourceInfo);
            IndexRequest indexRequest = new IndexRequest();
            Object o = JSONObject.toJSON(codeMappedState);
            indexRequest.index(INDEX_NAME).type(TYPE_NAME).source(JSONObject.toJSONString(codeMappedState), XContentType.JSON);
            bulkRequestBuilder.add(indexRequest);
        }else{
            codeMappedState.getRelatedCodes().removeIf(x->Objects.equals(x.getCodeMappedId(),relatedSourceInfo.getCodeMappedId()));
            codeMappedState.getRelatedCodes().add(relatedSourceInfo);
            UpdateRequestBuilder updateRequestBuilder = client.prepareUpdate(INDEX_NAME, TYPE_NAME, codeMappedState.getEsId())
                    .setDoc(JSONObject.toJSONString(codeMappedState), XContentType.JSON);
            bulkRequestBuilder.add(updateRequestBuilder);
        }
    }

    public String getCodeMappedId(String name1, String name2)throws  Exception{
        StringBuffer originalCode = new StringBuffer();
        if(name1.compareTo(name2)>0){
            originalCode.append(name1).append(name2);
        }else{
            originalCode.append(name2).append(name1);
        }
        return  getMd5(originalCode.toString());
    }
    public String getCodeMappedId(CodeSourceInfo code1, CodeSourceInfo code2)throws  Exception{
        StringBuffer originalCode = new StringBuffer();
        if(code1.getCode().compareTo(code2.getCode())>0){
            originalCode.append(code1.toString()).append(code2.toString());
        }else{
            originalCode.append(code2.toString()).append(code1.toString());
        }
        return  getMd5(originalCode.toString());
    }



    public  String getMd5(String string)throws  Exception {
        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
        byte[] bs = digest.digest(string.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder(40);
        for (byte x : bs) {
            if ((x & 0xff) >> 4 == 0) {
                sb.append("0").append(Integer.toHexString(x & 0xff));
            } else {
                sb.append(Integer.toHexString(x & 0xff));
            }
        }
        return sb.toString();
    }
}
