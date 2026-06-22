package com.sagem.g2ii.Repository;

import com.sagem.g2ii.Entity.Intervention.ConfigurationGlobale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationGlobaleRepository extends JpaRepository<ConfigurationGlobale, Long> {
}