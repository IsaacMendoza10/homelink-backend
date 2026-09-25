package com.homelink.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

// Tabla intermedia N a N entre PerfilTrabajador y Categoria: un trabajador
// puede ofrecer varias categorias a la vez, cada una con su propia tarifa
// (reemplaza el antiguo campo unico PerfilTrabajador.categoria/tarifaReferencial).
@Entity
@Table(name = "trabajador_categorias",
        uniqueConstraints = @UniqueConstraint(columnNames = {"perfil_trabajador_id", "categoria_id"}))
public class TrabajadorCategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "perfil_trabajador_id", nullable = false)
    private PerfilTrabajador perfilTrabajador;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false)
    private BigDecimal tarifa;

    public TrabajadorCategoria() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PerfilTrabajador getPerfilTrabajador() {
        return perfilTrabajador;
    }

    public void setPerfilTrabajador(PerfilTrabajador perfilTrabajador) {
        this.perfilTrabajador = perfilTrabajador;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getTarifa() {
        return tarifa;
    }

    public void setTarifa(BigDecimal tarifa) {
        this.tarifa = tarifa;
    }
}
