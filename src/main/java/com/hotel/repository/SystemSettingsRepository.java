package com.hotel.repository;

import com.hotel.entity.SystemSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemSettingsRepository extends JpaRepository<SystemSettings, Long> {

    // 系统设置是单例，总是返回第一条记录
    default SystemSettings getSingleton() {
        return findById(1L).orElseGet(this::createDefault);
    }

    private SystemSettings createDefault() {
        SystemSettings settings = new SystemSettings();
        return save(settings);
    }
}
