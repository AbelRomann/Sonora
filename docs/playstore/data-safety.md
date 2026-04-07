# Data Safety (Play Console) — Borrador de respuestas

**Fecha:** 6 de abril de 2026

Este documento sirve como guía para llenar el formulario **Data safety** de Google Play Console para `com.example.reproductor`.

## Supuestos actuales del proyecto

- La app es un reproductor **local/offline** de música del dispositivo.
- No hay evidencias de SDK de anuncios, analítica o login en la configuración actual.
- Los permisos declarados están orientados a lectura de audio local y reproducción en segundo plano.

## 1) Recolección de datos

### ¿La app recopila o comparte datos?

**Respuesta objetivo (si no agregas backend/SDKs):**
- **No** recopila datos personales para transmitirlos fuera del dispositivo.
- **No** comparte datos con terceros.

> Si en el futuro agregas crash reporting, analítica, autenticación o servicios en la nube, esta sección debe actualizarse.

## 2) Tipos de datos (guía)

Para la versión actual, puedes evaluar:

- **Audio files / música local:** se procesan en dispositivo para reproducción.
- **Actividad en la app (playlists/favoritos/historial):** uso local para funcionalidad.

Si no salen del dispositivo ni se comparten, normalmente se informan como **no recopilados** para Play Data Safety.

## 3) Prácticas de seguridad

En Play Console, valida y marca según corresponda:

- Cifrado de datos en tránsito: **No aplica** si no hay transmisión a servidores.
- Solicitud de eliminación de datos: **No aplica** para cuenta remota; el usuario puede borrar datos desde ajustes de Android.

## 4) Checklist antes de enviar Data Safety

1. Confirmar que no se agregaron SDKs que transmitan datos (ads, analytics, crash, auth social).
2. Revisar permisos en `AndroidManifest.xml`.
3. Alinear exactamente con la Política de Privacidad publicada.
4. Guardar evidencia interna (capturas/config) por si Play solicita revisión.

## 5) Riesgos comunes

- Marcar “no recopila” y después integrar Firebase/Ads sin actualizar Data Safety.
- Publicar política de privacidad genérica que contradiga permisos o conducta real de la app.
- No actualizar Data Safety tras nuevas versiones.

---

**Responsable de actualización:** equipo de release.
