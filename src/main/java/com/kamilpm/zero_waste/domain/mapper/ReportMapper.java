package com.kamilpm.zero_waste.domain.mapper;

import org.mapstruct.Mapper;

import com.kamilpm.zero_waste.domain.dto.ReportDto;
import com.kamilpm.zero_waste.domain.entity.Report;

@Mapper(componentModel = "spring", uses = { UserSummaryMapper.class })
public interface ReportMapper {
  ReportDto toDto(Report report);

}
