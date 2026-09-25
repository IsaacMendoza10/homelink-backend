package com.homelink.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "perfiles_trabajador")
public class PerfilTrabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    private String zonaCobertura;

    @Column(length = 1000)
    private String descripcion;

    // Ruta/URL del documento de validacion (cedula, certificaciones).
    // No se valida tipo ni tamano de archivo en esta version.
    private String documentoUrl;

    @Enumerated(EnumType.STRING)
    private EstadoAprobacion estadoAprobacion = EstadoAprobacion.PENDIENTE;

    // Categorias que ofrece este trabajador, cada una con su propia tarifa (ver
    // TrabajadorCategoria). Reemplaza el antiguo campo unico categoria/tarifaReferencial:
    // un trabajador ahora puede estar en varias categorias a la vez.
    @OneToMany(mappedBy = "perfilTrabajador", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrabajadorCategoria> categorias = new ArrayList<>();

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

    public List<TrabajadorCategoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<TrabajadorCategoria> categorias) {
        this.categorias = categorias;
    }
}
