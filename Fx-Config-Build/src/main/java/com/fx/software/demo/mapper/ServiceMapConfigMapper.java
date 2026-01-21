package com.fx.software.demo.mapper;

import com.fx.software.demo.config.ServiceMapConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ServiceMapConfigMapper {


    List<ServiceMapConfig> selectByMapServer(ServiceMapConfig serviceMapConfig);
}
