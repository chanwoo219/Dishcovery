package com.spring.dishcovery.mapper;

import com.spring.dishcovery.entity.CodeVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CodeMapper {

    public List<CodeVO> codeList(String codeHead);

}
