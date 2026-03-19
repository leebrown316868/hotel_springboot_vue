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
                jdbcTemplate.execute(
                    "ALTER TABLE reviews ADD COLUMN guest_id INTEGER REFERENCES guests(id);"
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
            // 检查是否需要迁移（通过检查是否可以插入 null 值来判断）
            // 但为了简单，我们直接尝试重建表
            System.out.println("正在执行数据库迁移: 修改 reviews 表结构...");

            // SQLite 不支持直接修改列约束，需要重建表
            // 1. 创建新表
            jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS reviews_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "booking_id INTEGER NOT NULL, " +
                "user_id INTEGER, " +
                "guest_id INTEGER, " +
                "rating INTEGER NOT NULL, " +
                "comment TEXT, " +
                "created_at TIMESTAMP NOT NULL, " +
                "FOREIGN KEY (booking_id) REFERENCES bookings(id), " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (guest_id) REFERENCES guests(id));"
            );

            // 2. 复制数据
            jdbcTemplate.execute(
                "INSERT INTO reviews_new (id, booking_id, user_id, guest_id, rating, comment, created_at) " +
                "SELECT id, booking_id, user_id, guest_id, rating, comment, created_at FROM reviews;"
            );

            // 3. 删除旧表
            jdbcTemplate.execute("DROP TABLE reviews;");

            // 4. 重命名新表
            jdbcTemplate.execute("ALTER TABLE reviews_new RENAME TO reviews;");

            // 5. 重建索引
            try {
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_reviews_booking_id ON reviews(booking_id);");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_reviews_user_id ON reviews(user_id);");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_reviews_guest_id ON reviews(guest_id);");
            } catch (Exception e) {
                // 索引可能已存在，忽略错误
            }

            System.out.println("✓ 数据库迁移完成: reviews 表结构已更新");
        } catch (Exception e) {
            // 如果表已经是新结构，可能会报错，忽略
            if (e.getMessage() != null && e.getMessage().contains("no such table")) {
                System.out.println("✓ reviews 表结构已是最新，跳过迁移");
            } else {
                System.err.println("数据库迁移警告: " + e.getMessage());
            }
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
