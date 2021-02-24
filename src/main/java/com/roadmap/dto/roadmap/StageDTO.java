package com.roadmap.dto.roadmap;

import lombok.Data;

@Data
public class StageDTO {

    private Long id;

    private String title;

    private int ord;

    private boolean complete;

    private boolean head;

    private boolean tail;


}
