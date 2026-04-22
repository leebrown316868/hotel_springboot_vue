package com.hotel.util;

import com.hotel.entity.Booking;
import com.hotel.entity.Guest;
import com.hotel.entity.NotificationType;
import com.hotel.entity.RoomType;
import com.hotel.entity.User;
import com.hotel.entity.UserRole;
import com.hotel.repository.UserRepository;
import com.hotel.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

/**
 * 通知工具类，封装通知创建逻辑和内容模板
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationHelper {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");

    /**
     * 发送预订创建通知
     */
    public void notifyBookingCreated(Booking booking) {
        try {
            String guestEmail = booking.getGuest().getEmail();
            String title = "预订成功";
            String message = String.format(
                "您的预订 %s 已创建，%s，%s入住，%s退房",
                booking.getBookingNumber(),
                getRoomTypeName(booking.getRoomType()),
                booking.getCheckInDate().format(DATE_FORMATTER),
                booking.getCheckOutDate().format(DATE_FORMATTER)
            );

            notificationService.createNotification(
                guestEmail,
                title,
                message,
                NotificationType.BOOKING,
                1,
                "/my-bookings"
            );

            log.info("Sent booking created notification to: {}", guestEmail);
        } catch (Exception e) {
            log.error("Failed to send booking created notification for: {}", booking.getBookingNumber(), e);
        }
    }

    /**
     * 发送支付成功待入住通知（给所有员工）
     */
    public void notifyPaymentConfirmed(Booking booking) {
        try {
            List<String> staffEmails = getStaffEmails();
            String title = "新订单待入住";
            String message = String.format(
                "客户 %s 已支付订单 %s，%s入住，房间 %s，请及时办理入住",
                booking.getGuest().getName(),
                booking.getBookingNumber(),
                booking.getCheckInDate().format(DATE_FORMATTER),
                booking.getRoom().getNumber()
            );

            for (String email : staffEmails) {
                notificationService.createNotification(
                    email, title, message,
                    NotificationType.BOOKING, 1,
                    "/staff-bookings"
                );
            }
            log.info("Sent payment confirmed notification to {} staff", staffEmails.size());
        } catch (Exception e) {
            log.error("Failed to send payment confirmed notification for: {}", booking.getBookingNumber(), e);
        }
    }

    /**
     * 发送预订取消通知
     */
    public void notifyBookingCancelled(Booking booking) {
        try {
            String guestEmail = booking.getGuest().getEmail();
            String title = "预订已取消";
            String message = String.format("您的预订 %s 已取消", booking.getBookingNumber());

            notificationService.createNotification(
                guestEmail,
                title,
                message,
                NotificationType.CANCELLATION,
                2,
                "/my-bookings"
            );

            log.info("Sent booking cancelled notification to: {}", guestEmail);
        } catch (Exception e) {
            log.error("Failed to send booking cancelled notification for: {}", booking.getBookingNumber(), e);
        }
    }

    /**
     * 发送入住通知（给所有员工）
     */
    public void notifyCheckIn(Booking booking) {
        String title = "客户入住";
        String message = String.format(
            "客户 %s 已入住 %s，预订号 %s",
            booking.getGuest().getName(),
            booking.getRoom().getNumber(),
            booking.getBookingNumber()
        );

        List<String> staffEmails = getStaffEmails();
        for (String email : staffEmails) {
            try {
                notificationService.createNotification(
                    email,
                    title,
                    message,
                    NotificationType.CHECK_IN,
                    1,
                    "/staff-bookings"
                );
            } catch (Exception e) {
                log.error("Failed to send check-in notification to: {}", email, e);
            }
        }

        log.info("Sent check-in notifications to {} staff members", staffEmails.size());
    }

    /**
     * 发送退房通知（给所有员工）
     */
    public void notifyCheckOut(Booking booking) {
        String title = "客户退房";
        String message = String.format(
            "客户 %s 已退房 %s，预订号 %s",
            booking.getGuest().getName(),
            booking.getRoom().getNumber(),
            booking.getBookingNumber()
        );

        List<String> staffEmails = getStaffEmails();
        for (String email : staffEmails) {
            try {
                notificationService.createNotification(
                    email,
                    title,
                    message,
                    NotificationType.CHECK_OUT,
                    1,
                    "/staff-bookings"
                );
            } catch (Exception e) {
                log.error("Failed to send check-out notification to: {}", email, e);
            }
        }

        log.info("Sent check-out notifications to {} staff members", staffEmails.size());
    }

    /**
     * 获取所有员工（STAFF 和 ADMIN）的邮箱列表
     */
    private List<String> getStaffEmails() {
        List<User> staff = userRepository.findByRole(UserRole.STAFF);
        List<User> admin = userRepository.findByRole(UserRole.ADMIN);
        List<String> emails = Stream.concat(staff.stream(), admin.stream())
                .map(User::getEmail)
                .collect(java.util.stream.Collectors.toList());

        if (emails.isEmpty()) {
            log.warn("No staff or admin users found for notification");
        }

        return emails;
    }

    /**
     * 获取房型中文名称
     */
    private String getRoomTypeName(RoomType type) {
        return type.getDisplayName();
    }
}
