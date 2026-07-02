package com.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "gpus")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Gpu {

    @Id
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id")
    private BaseComponent baseComponent;

    @Column(name = "vram_size_gb", nullable = false)
    private Integer vramSizeGb;

    @Column(name = "tdp_wattage", nullable = false)
    private Integer tdpWattage;
}