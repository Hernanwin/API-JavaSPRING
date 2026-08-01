package com.stockcomerce.stockapihb.services;

import com.stockcomerce.stockapihb.dto.DetalleVentaDTO;
import com.stockcomerce.stockapihb.dto.TiendaDTO;
import com.stockcomerce.stockapihb.dto.VentaDTO;
import com.stockcomerce.stockapihb.mapper.Mapper;
import com.stockcomerce.stockapihb.models.DetalleVenta;
import com.stockcomerce.stockapihb.models.Producto;
import com.stockcomerce.stockapihb.models.Tienda;
import com.stockcomerce.stockapihb.models.Venta;
import com.stockcomerce.stockapihb.repositories.ProductoRepository;
import com.stockcomerce.stockapihb.repositories.TiendaRepository;
import com.stockcomerce.stockapihb.repositories.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService implements IVentaService {

    @Autowired
    private VentaRepository ventareposi;
    @Autowired
    private ProductoRepository productoreposi;
    @Autowired
    private TiendaRepository tiendareposi;

    @Transactional(readOnly = true)
    @Override
    public List<VentaDTO> mostrarVentas() {
        List<Venta> ventas = ventareposi.findAll();
        List<VentaDTO> ventasDTO = new ArrayList<>();

        VentaDTO ventaDTO;
        for (Venta venta : ventas) {
            ventaDTO = Mapper.ventDTO(venta);
            ventasDTO.add(ventaDTO);
        }

        return ventasDTO;
    }

    //validaciones para que se pueda indicar una venta
    @Transactional
    @Override
    public VentaDTO crearVenta(VentaDTO ventaDTO) {
        if (ventaDTO == null) throw new RuntimeException("VentaDTO no existe");
        if (ventaDTO.getIdTienda() == null) throw new RuntimeException("Debe indicar la tienda");
        if (ventaDTO.getDetalle() == null || ventaDTO.getDetalle().isEmpty())
            throw new RuntimeException("Debe incluir al menos un producto");

        //buscar la tienda
        Tienda tienda = tiendareposi.findById(ventaDTO.getIdTienda()).orElse(null);
        if (tienda == null) {
            throw new RuntimeException("Tienda no localizada");
        }

        //crear la venta
        Venta venta = new Venta();
        venta.setFechaHora(ventaDTO.getFechaHora());
        venta.setEstado(ventaDTO.getEstado());
        venta.setTienda(tienda);
        venta.setTotal(ventaDTO.getTotal());

        //lista de detalles donde estan los productos
        List<DetalleVenta> detalles = new ArrayList<>();

        for(DetalleVentaDTO detalleVentaDTO : ventaDTO.getDetalle()){
            Producto p = productoreposi.findById(detalleVentaDTO.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            DetalleVenta detalle = new DetalleVenta();
            detalle.setVenta(venta);
            detalle.setProducto(p);
            detalle.setCantidad(detalleVentaDTO.getCantidadProd());
            detalle.setPrecioUnitario(detalleVentaDTO.getPrecioUnitario());
            detalle.setSubtotal(detalleVentaDTO.getPrecioUnitario()*detalleVentaDTO.getCantidadProd());

            detalles.add(detalle);
        }

        venta.setDetalle(detalles);

        venta = ventareposi.save(venta);

        VentaDTO ventadesalida = Mapper.ventDTO(venta);

        return ventadesalida;
    }

    @Transactional
    @Override
    public VentaDTO actualizarVenta(Long id, VentaDTO ventaDTO) {
        //Buscar la venta asi la actualizamos
        Venta venta = ventareposi.findById(id).orElse(null);
        if (venta == null) throw new RuntimeException("La venta no se encontro");

        if (ventaDTO.getIdTienda() != null) {
            venta.setFechaHora(ventaDTO.getFechaHora());
        }

        if (ventaDTO.getEstado() != null) {
            venta.setEstado(ventaDTO.getEstado());
        }

        if (ventaDTO.getTotal() != null) {
            venta.setTotal(ventaDTO.getTotal());
        }

        if (ventaDTO.getIdTienda() == null) {
            Tienda tienda = tiendareposi.findById(ventaDTO.getIdTienda()).orElse(null);
            if (tienda == null) throw new RuntimeException("Tienda no localizada");
            venta.setTienda(tienda);
        }

        ventareposi.save(venta);

        VentaDTO ventadesalida = Mapper.ventDTO(venta);

        return ventadesalida;
    }

    @Transactional
    @Override
    public void eliminarVenta(Long id) {
        Venta venta = ventareposi.findById(id).orElse(null);
        if (venta == null) throw new RuntimeException("La venta no existe");
        ventareposi.delete(venta);
    }
}