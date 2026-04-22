package com.bikeshop.repository;

import com.bikeshop.entity.NotificacionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionLogRepository extends JpaRepository<NotificacionLog, Long> {

    List<NotificacionLog> findByEstadoAndIntentosLessThan(NotificacionLog.Estado estado, int maxIntentos);

    List<NotificacionLog> findByReferenciaIdAndTipoReferencia(Long referenciaId, NotificacionLog.TipoReferencia tipo);
}
