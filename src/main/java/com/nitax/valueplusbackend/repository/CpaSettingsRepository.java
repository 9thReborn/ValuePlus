package com.nitax.valueplusbackend.repository;

import com.nitax.valueplusbackend.domain.CPASettings;
import com.nitax.valueplusbackend.domain.FlowType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface CpaSettingsRepository extends JpaRepository<CPASettings, Long> {
    CPASettings findCPASettingsByCpaId(String cpaId);

    @Modifying
    void deleteByCpaId(String cpaId);

    boolean existsByCpaId(String string);


    /*  @Query("""
              SELECT s FROM CPASettings s WHERE s.country LIKE CONCAT('%', :country, '%')
              OR s.flow LIKE CONCAT('%', :flow, '%') OR s.mobileNetworkOperator LIKE  CONCAT('%', :mno, '%')""")
      List<CPASettings> findCpaByFilters(String country, String flow, String mno);*/
    @Query("""
    SELECT s FROM CPASettings s 
    WHERE (COALESCE(:country, '') = '' OR s.country ILIKE CONCAT('%', :country, '%')) 
    AND (COALESCE(:flow, '') = '' OR s.flow ILIKE CONCAT('%', :flow, '%')) 
    AND (:flowType IS NULL OR s.flowType = :flowType)
     AND (COALESCE(:mno, '') = '' OR s.mobileNetworkOperator ILIKE CONCAT('%', :mno, '%'))
""")
    List<CPASettings> findCpaByFilters(@Param("country") String country,
                                       @Param("flow") String flow,
                                       @Param("mno") String mno,
                                       @Param("flowType") FlowType flowType);



}
