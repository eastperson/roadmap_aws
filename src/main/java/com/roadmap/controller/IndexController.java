package com.roadmap.controller;

import com.roadmap.config.AppProperties;
import com.roadmap.dto.member.CurrentUser;
import com.roadmap.model.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@Controller
@Log4j2
@RequiredArgsConstructor
public class IndexController {

    private final AppProperties appProperties;

    @GetMapping("/hello")
    @ResponseBody
    public String hello(){
        return "hello world. It is core";
    }

    @GetMapping("/")
    public String index(@CurrentUser Member member, Model model){
        if(member != null) {
            model.addAttribute(member);
        }

        return "index";
    }

    @RequestMapping(value = "/popup/jusoPopup", method = {RequestMethod.GET,RequestMethod.POST})
    public String jusoPopup(HttpServletRequest request, Model model) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");  //한글깨지면 주석제거
        String inputYn = request.getParameter("inputYn");

        String confmKey = appProperties.getJusoConfirmKey();
        String roadAddrPart1 = request.getParameter("roadAddrPart1");
        String addrDetail = request.getParameter("addrDetail");
        String siNm  = request.getParameter("siNm");
        String sggNm  = request.getParameter("sggNm");
        String emdNm  = request.getParameter("emdNm");

        model.addAttribute("roadAddrPart1",roadAddrPart1);
        model.addAttribute("addrDetail",addrDetail);
        model.addAttribute("siNm",siNm);
        model.addAttribute("sggNm",sggNm);
        model.addAttribute("emdNm",emdNm);


        model.addAttribute("confmKey", confmKey);
        model.addAttribute("inputYn", inputYn);

        return "popup/jusoPop";
    }
}