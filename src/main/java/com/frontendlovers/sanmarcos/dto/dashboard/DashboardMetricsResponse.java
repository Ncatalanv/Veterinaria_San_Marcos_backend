package com.frontendlovers.sanmarcos.dto.dashboard;

import java.util.List;

public record DashboardMetricsResponse(
        long totalOrdenes,
        long totalSolicitudes,
        long totalUsuarios,
        long totalProductos,
        List<ProductoStockCriticoItem> productosStockCritico,
        List<UltimaOrdenItem> ultimasOrdenes,
        List<UltimaSolicitudItem> ultimasSolicitudes
) {
}
