package br.com.fichario.internal.usuario.repository;

import br.com.fichario.internal.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
