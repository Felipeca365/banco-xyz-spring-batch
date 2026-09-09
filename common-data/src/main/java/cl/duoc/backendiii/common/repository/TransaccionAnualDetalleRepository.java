package cl.duoc.backendiii.common.repository;

import cl.duoc.backendiii.common.model.TransaccionAnualDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionAnualDetalleRepository extends JpaRepository<TransaccionAnualDetalle, Long> {
}