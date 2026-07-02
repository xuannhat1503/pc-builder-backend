package com.backend.service;

import com.backend.dto.ai.CatalogComponentView;
import com.backend.entity.Cpu;
import com.backend.entity.Gpu;
import com.backend.entity.Mainboard;
import com.backend.entity.Psu;
import com.backend.entity.Ram;

import java.math.BigDecimal;
import java.util.List;

public interface ComponentFormatter {

    CatalogComponentView format(Cpu cpu, BigDecimal lowestPrice, String cheapestSource);

    CatalogComponentView format(Mainboard mainboard, BigDecimal lowestPrice, String cheapestSource);

    CatalogComponentView format(Ram ram, BigDecimal lowestPrice, String cheapestSource);

    CatalogComponentView format(Gpu gpu, BigDecimal lowestPrice, String cheapestSource);

    CatalogComponentView format(Psu psu, BigDecimal lowestPrice, String cheapestSource);

    String formatCatalog(List<CatalogComponentView> components);
}