package com.spring.dishcovery.user.domain.mapper;

import com.spring.dishcovery.user.domain.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApiMapper {

    public int countByUserId(String userId);

    public int saveSignupApi(UserEntity userVo);

}
