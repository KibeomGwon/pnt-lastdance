package com.pnt.pnt_spring.domain.members.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pnt.pnt_spring.domain.members.report.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}