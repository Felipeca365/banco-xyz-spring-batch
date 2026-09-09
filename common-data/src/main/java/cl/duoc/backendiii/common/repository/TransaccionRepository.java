package cl.duoc.backendiii.common.repository;

import cl.duoc.backendiii.common.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
}