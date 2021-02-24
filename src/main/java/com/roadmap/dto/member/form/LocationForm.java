package com.roadmap.dto.member.form;

import lombok.Data;

@Data
public class LocationForm {

    private String addr;
    private String addrDetail;
    private String siNm;
    private String sggNm;
    private String emdNm;
    private Double lat;
    private Double lng;


}
