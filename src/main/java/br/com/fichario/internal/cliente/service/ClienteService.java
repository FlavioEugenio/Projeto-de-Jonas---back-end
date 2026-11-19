package br.com.fichario.internal.cliente.service;

import br.com.fichario.internal.cliente.dto.ClienteRequest;
import br.com.fichario.internal.cliente.dto.ClienteResponse;
import br.com.fichario.internal.cliente.entity.Cliente;
import br.com.fichario.internal.cliente.mapper.ClienteMapper;
import br.com.fichario.internal.cliente.repository.ClienteRepository;
import br.com.fichario.shared.exception.RecursoNaoEncontradoException;
import br.com.fichario.shared.exception.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteResponse cadastrar(ClienteRequest request) {
        Cliente cliente = new Cliente(request.nome(), request.telefone());
        if (clienteRepository.existsByTelefone(cliente.getTelefone())) {
            throw new RegraDeNegocioException("Ja existe um cliente com esse telefone.");
        }
        clienteRepository.save(cliente);
        return ClienteMapper.paraResposta(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar() {
        return clienteRepository.findAllByOrderByNomeAsc().stream()
                .map(ClienteMapper::paraResposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return ClienteMapper.paraResposta(buscarEntidade(id));
    }

    public Cliente buscarEntidade(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }
}
