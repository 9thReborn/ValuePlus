package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.BulkSmsMessage;
import com.nitax.valueplusbackend.domain.SmsDeliveryStatus;
import com.nitax.valueplusbackend.dto.response.CampaignDeliveryRate;
import com.nitax.valueplusbackend.dto.response.HourlyDeliveryRate;
import com.nitax.valueplusbackend.dto.response.SMSDeliveryStatusRes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BulkSmsMessageRepository extends JpaRepository<BulkSmsMessage ,Long> {
    List<BulkSmsMessage> findByMessageIdIn(List<String> messageIDs);

    List<BulkSmsMessage> findByStatusIn(List<SmsDeliveryStatus> pending);

    Optional<BulkSmsMessage> findByMessageId(String messageId);



    @Query( value= """ 
SELECT
    m.phone_number AS targetNumber,
    m.status AS status,
    m.delivery_timestamp AS deliveryTimestamp,
    c.bulk_sms_campaign_id as campaignId,
    c.country AS country,
    c.processor AS route
FROM bulk_sms_campaign c
         join bulk_sms_message m on c.id = m.campaign_id
where c.created_date BETWEEN :startDate AND :endDate
""", nativeQuery = true)
    Page<SMSDeliveryStatusRes> getDeliveryStatusByCampaignCreatedDate(Pageable pageable, @Param("startDate") Instant startDate, @Param("endDate") Instant endDate);


}
