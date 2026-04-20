package com.pet.mapper;

import com.pet.entity.SystemSetting;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface SystemSettingMapper {

    List<SystemSetting> findAll();

    SystemSetting findByKey(@Param("key") String key);

    int insert(SystemSetting setting);

    int update(SystemSetting setting);

    int saveOrUpdate(SystemSetting setting);
}