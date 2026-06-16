package com.haircut.backend.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.haircut.backend.entity.Appointment;
import com.haircut.backend.entity.AppointmentStatus;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
        // 1. Lịch sử booking của 1 khách
        List<Appointment> findByCustomerId(Long customerId);

        // 2. Tất cả booking của 1 thợ (dùng cho lịch tổng quát)
        List<Appointment> findByBarberId(Long barberId);

        // 3. Lịch thợ trong khoảng thời gian (vd cả ngày hôm nay)
        List<Appointment> findByBarberIdAndStartAtBetween(
                        Long barberId, OffsetDateTime from, OffsetDateTime to);

        // 4. Lọc theo status (vd PENDING cho dashboard)
        List<Appointment> findByStatus(AppointmentStatus status);

        @Query("""
                            SELECT a FROM Appointment a
                            WHERE a.barber.id = :barberId
                              AND a.startAt < :newEnd
                              AND a.endAt > :newStart
                              AND a.status NOT IN :excludedStatuses
                        """)
        List<Appointment> findConflicts(
                        @Param("barberId") Long barberId,
                        @Param("newStart") OffsetDateTime newStart,
                        @Param("newEnd") OffsetDateTime newEnd,
                        @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses);

        @Query("""
                            SELECT a FROM Appointment a
                            WHERE a.barber.id = :barberId
                              AND a.id <> :excludeId
                              AND a.startAt < :newEnd
                              AND a.endAt > :newStart
                              AND a.status NOT IN :excludedStatuses
                        """)
        List<Appointment> findConflictsExcluding(@Param("barberId") Long barberId,
                        @Param("excludeId") Long excludeId,
                        @Param("newStart") OffsetDateTime newStart,
                        @Param("newEnd") OffsetDateTime newEnd,
                        @Param("excludedStatuses") List<AppointmentStatus> excludedStatuses);
}