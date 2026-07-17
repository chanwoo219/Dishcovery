package com.spring.dishcovery.entity;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeVO {

    private String codeHead;
    private String code;
    private String codeName;
    private String parentCode;
    private int codeSort;

}
