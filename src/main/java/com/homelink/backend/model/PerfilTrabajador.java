package com.homelink.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "perfiles_trabajador")
public class PerfilTrabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    private String zonaCobertura;

    @Column(length = 1000)
    private String descripcion;

    private BigDecimal tarifaReferencial;

    // Ruta/URL del documento de validacion (cedula, certificaciones).
    // No se valida tipo ni tamano de archivo en esta version.
    private String documentoUrl;

    @Enumerated(EnumType.STRING)
    private EstadoAprobacion estadoAprobacion = EstadoAprobacion.PENDIENTE;

    public PerfilTrabajador() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getZonaCobertura() {
        return zonaCobertura;
    }

    public void setZonaCobertura(String zonaCobertura) {
        this.zonaCobertura = zonaCobertura;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getTarifaReferencial() {
        return tarifaReferencial;
    }

    public void setTarifaReferencial(BigDecimal tarifaReferencial) {
        this.tarifaReferencial = tarifaReferencial;
    }

    public String getDocumentoUrl() {
        return documentoUrl;
    }

    public void setDocumentoUrl(String documentoUrl) {
        this.documentoUrl = documentoUrl;
    }

    public EstadoAprobacion getEstadoAprobacion() {
        return estadoAprobacion;
    }

    public void setEstadoAprobacion(EstadoAprobacion estadoAprobacion) {
        this.estadoAprobacion = estadoAprobacion;
    }
}
