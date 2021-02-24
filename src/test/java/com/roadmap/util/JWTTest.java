package com.roadmap.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.roadmap.dto.roadmap.form.NodeAddForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JWTTest {

    private JWTUtil jwtUtil;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void testBefore(){
        System.out.println("testBefore.............");
        jwtUtil = new JWTUtil();
    }

    @Test
    void testEncode() throws Exception {
        String email = "kjuioqqq@naver.com";
        String str = jwtUtil.generateToken(email);
        System.out.println(str);
    }

    @Test
    void testValidate() throws Exception {
        String email = "kjuioq@nate.com";
        String str = jwtUtil.generateToken(email);
        Thread.sleep(5000);
        String resultEmail = jwtUtil.validateAndExtract(str);
        System.out.println(str);
        System.out.println(resultEmail);
    }

    @Test
    void testObjectMapperTest() throws JsonProcessingException {
        NodeAddForm nodeAddForm = new NodeAddForm();

        nodeAddForm.setNodeType("type");
        nodeAddForm.setTitle("title");
        nodeAddForm.setParentType("parentType");
        nodeAddForm.setParentId(1L);
        nodeAddForm.setId(1L);

        ObjectMapper objectMapper = new ObjectMapper();

        String str = objectMapper.writeValueAsString(nodeAddForm);

        System.out.println("str : "+str);


        NodeAddForm nodeAddForm1 = objectMapper.readValue(str, NodeAddForm.class);

        System.out.println("nodeAddForm1 : " + nodeAddForm1);

    }

}
