package com.homelink.backend.controller;

import com.homelink.backend.dto.PostulacionVista;
import com.homelink.backend.dto.SolicitudDisponibleView;
import com.homelink.backend.dto.TiempoUtil;
import com.homelink.backend.model.*;
import com.homelink.backend.security.CustomUserDetails;
import com.homelink.backend.service.CategoriaService;
import com.homelink.backend.service.PostulacionService;
import com.homelink.backend.service.SolicitudService;
import com.homelink.backend.service.TrabajadorService;
import com.homelink.backend.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// El id y el rol del usuario ya no se leen de la HttpSession: llegan
// garantizados por Spring Security a traves de CustomUserDetails (ver
// SecurityConfig, que exige autenticacion para todo /solicitudes/**).
@Controller
@RequestMapping("/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final CategoriaService categoriaService;
    private final UsuarioService usuarioService;
    private final TrabajadorService trabajadorService;
    private final PostulacionService postulacionService;

    public SolicitudController(SolicitudService solicitudService, CategoriaService categoriaService,
                                UsuarioService usuarioService, TrabajadorService trabajadorService,
                                PostulacionService postulacionService) {
        this.solicitudService = solicitudService;
        this.categoriaService = categoriaService;
        this.usuarioService = usuarioService;
        this.trabajadorService = trabajadorService;
        this.postulacionService = postulacionService;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal CustomUserDetails principal, Model model) {
        Long usuarioId = principal.getUsuario().getId();
        String rol = principal.getUsuario().getRol().name();
        List<Solicitud> solicitudes = "TRABAJADOR".equals(rol)
                ? solicitudService.listarPorTrabajador(usuarioId)
                : solicitudService.listarPorCliente(usuarioId);
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("rol", rol);

        if ("TRABAJADOR".equals(rol)) {
            cargarVistaTrabajador(usuarioId, model);
        } else if ("CLIENTE".equals(rol)) {
            cargarVistaCliente(usuarioId, model);
        }
        return "solicitudes/list";
    }

    // Para un trabajador aprobado: solicitudes abiertas de TODAS las categorias
    // que ofrece (con el contador X/7 y el tiempo restante ya visibles), sus
    // propias postulaciones enviadas (con si ya se pueden retirar), y las
    // invitaciones directas que le llegaron de algun cliente.
    private void cargarVistaTrabajador(Long trabajadorId, Model model) {
        List<SolicitudDisponibleView> disponibles = trabajadorService.buscarPorUsuarioId(trabajadorId)
                .filter(perfil -> perfil.getEstadoAprobacion() == EstadoAprobacion.APROBADO)
                .map(perfil -> {
                    List<SolicitudDisponibleView> vistas = new ArrayList<>();
                    for (TrabajadorCategoria tc : trabajadorService.listarCategoriasDe(perfil.getId())) {
                        for (Solicitud s : solicitudService.listarAbiertasPorCategoria(tc.getCategoria().getId())) {
                            vistas.add(new SolicitudDisponibleView(
                                    s,
                                    postulacionService.contarActivas(s.getId()),
                                    PostulacionService.MAX_POSTULANTES,
                                    TiempoUtil.formatoRestante(s.getFechaCreacion().plusHours(24)),
                                    postulacionService.yaPostulado(s.getId(), trabajadorId)));
                        }
                    }
                    return vistas;
                })
                .orElse(Collections.emptyList());
        model.addAttribute("solicitudesDisponibles", disponibles);

        List<PostulacionVista> misPostulaciones = postulacionService.listarEnviadasPorTrabajador(trabajadorId).stream()
                .map(p -> new PostulacionVista(
                        p,
                        TiempoUtil.formatoRestante(p.getFechaEnvio().plusMinutes(PostulacionService.MINUTOS_RESPUESTA_CLIENTE)),
                        minutosDesde(p) >= PostulacionService.MINUTOS_RETIRO_TRABAJADOR))
                .collect(Collectors.toList());
        model.addAttribute("misPostulaciones", misPostulaciones);

        List<PostulacionVista> invitaciones = postulacionService.listarInvitacionesPorTrabajador(trabajadorId).stream()
                .map(p -> new PostulacionVista(
                        p,
                        TiempoUtil.formatoRestante(p.getFechaEnvio().plusMinutes(PostulacionService.MINUTOS_RESPUESTA_CLIENTE)),
                        false))
                .collect(Collectors.toList());
        model.addAttribute("misInvitaciones", invitaciones);
    }

    // Para el cliente: cuantos postulantes activos tiene cada una de sus solicitudes
    // (se muestra como X/7 junto a las que siguen PENDIENTE).
    private void cargarVistaCliente(Long clienteId, Model model) {
        List<Solicitud> propias = solicitudService.listarPorCliente(clienteId);
        Map<Long, Integer> postulantesPorSolicitud = new HashMap<>();
        for (Solicitud s : propias) {
            postulantesPorSolicitud.put(s.getId(), postulacionService.contarActivas(s.getId()));
        }
        model.addAttribute("postulantesPorSolicitud", postulantesPorSolicitud);
        model.addAttribute("maxPostulantes", PostulacionService.MAX_POSTULANTES);
    }

    private long minutosDesde(Postulacion p) {
        return Duration.between(p.getFechaEnvio(), LocalDateTime.now()).toMinutes();
    }

    @GetMapping("/nueva")
    public String nuevaForm(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        return "solicitudes/form";
    }

    // Primer paso al crear una solicitud: si el cliente eligio "abierta", se crea
    // de una vez (igual que antes). Si eligio "elegir trabajador especifico", se
    // muestra la lista de trabajadores aprobados de esa categoria para que escoja
    // uno (ver crearDirecta), sin perder lo que ya habia escrito en el formulario.
    @PostMapping("/nueva")
    public String crear(@AuthenticationPrincipal CustomUserDetails principal, @RequestParam Long categoriaId,
                         @RequestParam String descripcion, @RequestParam String direccion,
                         @RequestParam(defaultValue = "ABIERTA") String modo, Model model) {
        if ("DIRECTA".equals(modo)) {
            model.addAttribute("categoriaId", categoriaId);
            model.addAttribute("descripcion", descripcion);
            model.addAttribute("direccion", direccion);
            model.addAttribute("trabajadores", trabajadorService.buscarDisponiblesPorCategoria(categoriaId));
            return "solicitudes/elegir-trabajador";
        }
        Usuario cliente = usuarioService.buscarPorId(principal.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Categoria categoria = categoriaService.buscarPorId(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        solicitudService.crear(cliente, categoria, descripcion, direccion);
        return "redirect:/solicitudes";
    }

    // Segundo paso del flujo directo: crea la solicitud y, de inmediato, una
    // invitacion (Postulacion origen CLIENTE) hacia el trabajador elegido.
    @PostMapping("/nueva/directa")
    public String crearDirecta(@AuthenticationPrincipal CustomUserDetails principal, @RequestParam Long categoriaId,
                                @RequestParam String descripcion, @RequestParam String direccion,
                                @RequestParam Long trabajadorId) {
        Usuario cliente = usuarioService.buscarPorId(principal.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Categoria categoria = categoriaService.buscarPorId(categoriaId)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada"));
        Usuario trabajador = usuarioService.buscarPorId(trabajadorId)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado"));
        Solicitud solicitud = solicitudService.crear(cliente, categoria, descripcion, direccion);
        postulacionService.invitar(solicitud, trabajador);
        return "redirect:/solicitudes";
    }

    // El trabajador se postula a una solicitud abierta de su categoria.
    @PostMapping("/{id}/postularse")
    public String postularse(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        Usuario trabajador = usuarioService.buscarPorId(principal.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Solicitud solicitud = solicitudService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        postulacionService.postularse(solicitud, trabajador);
        return "redirect:/solicitudes";
    }

    // El cliente ve a todos los que se han postulado (o fueron invitados) a una de sus solicitudes.
    @GetMapping("/{id}/postulantes")
    public String verPostulantes(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id, Model model) {
        Solicitud solicitud = solicitudService.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));
        if (!solicitud.getCliente().getId().equals(principal.getUsuario().getId())) {
            throw new IllegalArgumentException("Esta solicitud no es tuya.");
        }
        model.addAttribute("solicitud", solicitud);
        List<PostulacionVista> postulantes = postulacionService.listarPostulantes(id).stream()
                .map(p -> new PostulacionVista(
                        p,
                        TiempoUtil.formatoRestante(p.getFechaEnvio().plusMinutes(PostulacionService.MINUTOS_RESPUESTA_CLIENTE)),
                        false))
                .collect(Collectors.toList());
        model.addAttribute("postulantes", postulantes);
        return "solicitudes/postulantes";
    }

    @PostMapping("/postulaciones/{postulacionId}/aceptar")
    public String aceptarPostulacion(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long postulacionId) {
        Usuario cliente = usuarioService.buscarPorId(principal.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        postulacionService.aceptar(postulacionId, cliente);
        return "redirect:/solicitudes";
    }

    @PostMapping("/postulaciones/{postulacionId}/rechazar")
    public String rechazarPostulacion(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long postulacionId) {
        Usuario cliente = usuarioService.buscarPorId(principal.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        Long solicitudId = postulacionService.rechazar(postulacionId, cliente).getSolicitud().getId();
        return "redirect:/solicitudes/" + solicitudId + "/postulantes";
    }

    @PostMapping("/postulaciones/{postulacionId}/retirar")
    public String retirarPostulacion(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long postulacionId) {
        Usuario trabajador = usuarioService.buscarPorId(principal.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        postulacionService.retirar(postulacionId, trabajador);
        return "redirect:/solicitudes";
    }

    @PostMapping("/invitaciones/{postulacionId}/aceptar")
    public String aceptarInvitacion(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long postulacionId) {
        Usuario trabajador = usuarioService.buscarPorId(principal.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        postulacionService.aceptarInvitacion(postulacionId, trabajador);
        return "redirect:/solicitudes";
    }

    @PostMapping("/invitaciones/{postulacionId}/rechazar")
    public String rechazarInvitacion(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long postulacionId) {
        Usuario trabajador = usuarioService.buscarPorId(principal.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        postulacionService.rechazarInvitacion(postulacionId, trabajador);
        return "redirect:/solicitudes";
    }

    // El trabajador asignado marca que ya empezo el servicio (ACEPTADA -> EN_PROCESO).
    @PostMapping("/{id}/iniciar")
    public String iniciar(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        Usuario trabajador = usuarioService.buscarPorId(principal.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        solicitudService.iniciar(id, trabajador);
        return "redirect:/solicitudes";
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id) {
        solicitudService.finalizar(id);
        return "redirect:/solicitudes";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id) {
        solicitudService.cancelar(id);
        postulacionService.cancelarPendientesDeSolicitud(id);
        return "redirect:/solicitudes";
    }

    @PostMapping("/{id}/calificar")
    public String calificar(@PathVariable Long id, @RequestParam int puntuacion,
                             @RequestParam(required = false) String comentario) {
        solicitudService.calificar(id, puntuacion, comentario);
        return "redirect:/solicitudes";
    }
}
