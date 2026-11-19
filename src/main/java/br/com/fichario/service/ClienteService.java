package br.com.fichario.service;

import br.com.fichario.domain.exception.RegraDeNegocioException;
import br.com.fichario.domain.model.Cliente;
import br.com.fichario.dto.ClienteRequest;
import br.com.fichario.dto.ClienteResponse;
import br.com.fichario.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clientes;

    public ClienteService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    @Transactional
    public ClienteResponse cadastrar(ClienteRequest dados) {
        String telefone = Cliente.normalizarTelefone(dados.telefone());
        if (clientes.existsByTelefone(telefone)) {
            throw new RegraDeNegocioException("Ja existe um cliente com o telefone " + telefone + ".");
        }
        Cliente cliente = new Cliente(dados.nome(), telefone);
        cliente.atualizarInstagram(dados.instagram());
        cliente.atualizarObservacoes(dados.observacoes());
        return ClienteResponse.de(clientes.save(cliente));
    }

    /** Sem termo, lista todos. Termo so com numeros busca pelo telefone; com letras, pelo nome. */
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscar(String termo) {
        List<Cliente> encontrados;
        if (termo == null || termo.isBlank()) {
            encontrados = clientes.findAllByOrderByNomeAsc();
        } else if (termo.matches("[\\d\\s()+-]+")) {
            encontrados = clientes.findByTelefoneContainingOrderByNomeAsc(termo.replaceAll("\\D", ""));
        } else {
            encontrados = clientes.findByNomeContainingIgnoreCaseOrderByNomeAsc(termo.strip());
        }
        return encontrados.stream().map(ClienteResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        return ClienteResponse.de(buscarEntidade(id));
    }

    Cliente buscarEntidade(Long id) {
        return clientes.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Cliente", id));
    }
}
