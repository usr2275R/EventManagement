package com.example.eventmanagement.emaillog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmailRepository extends JpaRepository<EmailLog, Long> {

    long countByStatus(String status);

    @Query("SELECT e FROM EmailLog e WHERE e.status= :status ORDER BY e.requestedAt DESC")
    List<EmailLog> findByStatus(@Param("status") String status);

   @Query("SELECT e FROM EmailLog e"+ " WHERE LOWER (e.subject) LIKE LOWER(CONCAT ('%', :subject,'%'))"+
   "ORDER BY e.requestedAt DESC")
    List<EmailLog> findBySubject(@Param("subject") String subject);

   @Query("SELECT avg(e.duration) FROM EmailLog e")
   Double findAvg();

}
