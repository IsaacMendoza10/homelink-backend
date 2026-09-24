package com.homelink.backend.model;

// Quien inicio la postulacion: el propio trabajador (se postulo a una
// solicitud abierta) o el cliente (invito directamente a ese trabajador
// al crear la solicitud).
public enum OrigenPostulacion {
    TRABAJADOR,
    CLIENTE
}
