package com.spring.dishcovery.mapper;

import com.spring.dishcovery.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiMapper {

    public int countByUserId(String userId);

    public int saveSignupApi(UserEntity userVo);

}
