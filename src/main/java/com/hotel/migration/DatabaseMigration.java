package com.hotel.migration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

/**
 * 数据库迁移工具类
 * 用于在应用启动时检查并执行必要的数据库迁移
 */
@Component
@Order(1)
public class DatabaseMigration implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        migrateGuestsTableAddRoleAndPassword();
        migrateSystemSettingsAddDescription();
        migrateReviewsTableAddGuestId();
        migrateReviewsTableMakeUserIdNullable();
    }

    /**
     * 迁移: 添加 role 和 password 字段到 guests 表
     */
    private void migrateGuestsTableAddRoleAndPassword() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            // 检查 role 字段是否存在
            ResultSet roleColumns = metaData.getColumns(null, null, "guests", "role");
            boolean roleExists = roleColumns.next();

            // 检查 password 字段是否存在
            ResultSet passwordColumns = metaData.getColumns(null, null, "guests", "password");
            boolean passwordExists = passwordColumns.next();

            if (!roleExists) {
                System.out.println("正在执行数据库迁移: 添加 role 字段到 guests 表...");
                jdbcTemplate.execute(
                    "ALTER TABLE guests ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER';"
                );
                System.out.println("✓ 数据库迁移完成: role 字段已添加");
            } else {
                System.out.println("✓ guests 表已包含 role 字段，跳过迁移");
            }

            if (!passwordExists) {
                System.out.println("正在执行数据库迁移: 添加 password 字段到 guests 表...");
                jdbcTemplate.execute(
                    "ALTER TABLE guests ADD COLUMN password VARCHAR(255);"
                );
                System.out.println("✓ 数据库迁移完成: password 字段已添加");
            } else {
                System.out.println("✓ guests 表已包含 password 字段，跳过迁移");
            }
        } catch (Exception e) {
            System.err.println("数据库迁移失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库迁移失败", e);
        }
    }

    /**
     * 迁移: 添加 guest_id 字段到 reviews 表
     */
    private void migrateReviewsTableAddGuestId() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "reviews", "guest_id");

            if (!columns.next()) {
                System.out.println("正在执行数据库迁移: 添加 guest_id 字段到 reviews 表...");
                jdbcTemplate.execute("ALTER TABLE reviews ADD COLUMN guest_id INT;");
                jdbcTemplate.execute(
                    "ALTER TABLE reviews ADD CONSTRAINT fk_reviews_guest FOREIGN KEY (guest_id) REFERENCES guests(id);"
                );
                System.out.println("✓ 数据库迁移完成: guest_id 字段已添加");
            } else {
                System.out.println("✓ reviews 表已包含 guest_id 字段，跳过迁移");
            }
        } catch (Exception e) {
            System.err.println("数据库迁移失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库迁移失败", e);
        }
    }

    /**
     * 迁移: 使 reviews 表的 user_id 列可为空（SQLite 需要重建表）
     */
    private void migrateReviewsTableMakeUserIdNullable() {
    try {
        System.out.println("正在执行数据库迁移: 修改 reviews 表 user_id 列为可空...");
        jdbcTemplate.execute(
            "ALTER TABLE reviews MODIFY COLUMN user_id INT NULL"
        );
        System.out.println("✓ 数据库迁移完成: user_id 列已修改为可空");
    } catch (Exception e) {
        System.err.println("数据库迁移警告: " + e.getMessage());
    }
}

    /**
     * 迁移: 添加 description 字段到 system_settings 表
     */
    private void migrateSystemSettingsAddDescription() {
        try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "system_settings", "description");

            if (!columns.next()) {
                // 字段不存在，执行迁移
                System.out.println("正在执行数据库迁移: 添加 description 字段到 system_settings 表...");
                jdbcTemplate.execute(
                    "ALTER TABLE system_settings ADD COLUMN description VARCHAR(1000) DEFAULT '欢迎来到 GrandHotel 豪华酒店，我们致力于为您提供最舒适的住宿体验。';"
                );
                System.out.println("✓ 数据库迁移完成: description 字段已添加");
            } else {
                System.out.println("✓ system_settings 表已包含 description 字段，跳过迁移");
            }
        } catch (Exception e) {
            System.err.println("数据库迁移失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("数据库迁移失败", e);
        }
    }
}
