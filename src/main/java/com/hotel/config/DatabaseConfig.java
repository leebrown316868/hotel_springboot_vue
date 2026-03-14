package com.hotel.config;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.boot.spi.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

@Configuration
public class DatabaseConfig {

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;
}
